import { Injectable, Inject } from '@nestjs/common';
import { LowLevelRTClient, SessionUpdateMessage } from 'rt-client';
import { SocketGateway } from './socket.gateway';
import { ConfigService } from '@nestjs/config';
import { unlink, writeFile } from 'fs/promises';
import { ffmpegPath, isFfmpegAvailable } from 'node-av/ffmpeg';
import { execFile } from 'child_process';
import { promisify } from 'util';
const execFileAsync = promisify(execFile);
import { join } from 'path';
import { mkdir } from 'fs/promises';

export enum InputState {
  Working,
  ReadyToStart,
  ReadyToStop,
}

@Injectable()
export class SpeechRealtimeService {
  private readonly uploadsDir = join(process.cwd(), 'uploads');
  private realtimeStreaming: LowLevelRTClient | null = null;
  private recordingActive = false;
  private bufferMap = new Map<string, Uint8Array>();
  private azureApiEndpoint: string;
  private azureApiKey: string;
  private azureDeploymentOrModel: string;
  private readonly sampleRate = 24000;
  private readonly minBufferSize = 4800;
  private silenceDetectionTimeout: NodeJS.Timeout | null = null;
  private readonly silenceThreshold = 2000; // 2 seconds of silence
  private audioResponseBuffer = new Map<string, string[]>();

  constructor(
    @Inject(SocketGateway)
    private readonly socketGateway: SocketGateway,
    private readonly configService: ConfigService,
  ) {
    this.azureApiEndpoint = this.configService.get<string>(
      'azure.realtime.endpoint',
    )!;
    this.azureApiKey = this.configService.get<string>('azure.realtime.key')!;
    this.azureDeploymentOrModel = this.configService.get<string>(
      'azure.realtime.model',
    )!;
  }

  async startRealtime(
    socketId: string,
    systemMessage: string,
    temperature: number,
  ) {
    this.realtimeStreaming = new LowLevelRTClient(
      new URL(this.azureApiEndpoint),
      { key: this.azureApiKey },
      { deployment: this.azureDeploymentOrModel },
    );

    try {
      // // console.log('Sending session config to Azure');
      await this.realtimeStreaming.send(
        this.createConfigMessage(systemMessage, temperature),
      );
      // // console.log('Session started with Azure');
      this.recordingActive = true;
      this.handleRealtimeMessages(socketId);
      this.socketGateway.server.to(socketId).emit('audio', 'Session start');
    } catch (error) {
      console.error('Error sending session config to Azure:', error);
      this.socketGateway.server
        .to(socketId)
        .emit('error', 'Connection error: Unable to send config.');
    }
  }

  async stopRealtime(socketId: string) {
    this.recordingActive = false;
    const buffer = this.bufferMap.get(socketId);

    if (buffer && buffer.length > 0) {
      // // console.log(`Sending final buffer to Azure: ${buffer.length} bytes`);
      // Fix: Convert Uint8Array to Buffer then to base64
      const base64Audio = Buffer.from(buffer).toString('base64');
      try {
        await this.realtimeStreaming!.send({
          type: 'input_audio_buffer.append',
          audio: base64Audio,
        });
        await this.realtimeStreaming!.send({
          type: 'input_audio_buffer.commit',
        });
        // // console.log('Final audio buffer sent to Azure');

        await this.saveAudioToMp3(buffer, `audio_${socketId}.mp3`);
      } catch (error) {
        console.error('Error sending final audio buffer to Azure:', error);
      }
    }

    this.realtimeStreaming = null;
    this.bufferMap.delete(socketId);
  }

  processAudioRecordingBuffer(data: Buffer, socketId: string) {
    try {
      const uint8Array = new Uint8Array(data);
      const buffer = this.bufferMap.get(socketId) || new Uint8Array(0);

      // Combine arrays exactly like reference code
      const newBuffer = new Uint8Array(buffer.length + uint8Array.length);
      newBuffer.set(buffer);
      newBuffer.set(uint8Array, buffer.length);

      // Fixed size chunks like reference code
      if (newBuffer.length >= this.minBufferSize) {
        const toSend = newBuffer.slice(0, this.minBufferSize);
        this.bufferMap.set(socketId, newBuffer.slice(this.minBufferSize));

        // Convert to base64 like reference code
        const base64Audio = Buffer.from(toSend).toString('base64');

        if (this.recordingActive) {
          this.realtimeStreaming?.send({
            type: 'input_audio_buffer.append',
            audio: base64Audio,
          });
        }
      } else {
        this.bufferMap.set(socketId, newBuffer);
      }
    } catch (error) {
      console.error('Error processing audio buffer:', error);
    }
  }

  private createConfigMessage(
    systemMessage: string,
    temperature: number,
  ): SessionUpdateMessage {
    const configMessage: SessionUpdateMessage = {
      type: 'session.update',
      session: {
        turn_detection: { type: 'server_vad' },
        input_audio_transcription: { model: 'whisper-1' },
        instructions: systemMessage,
        temperature: temperature,
        voice: 'alloy',
      },
    };

    return configMessage;
  }

  private async handleRealtimeMessages(socketId: string) {
    try {
      // Initialize buffer for this session
      this.audioResponseBuffer.set(socketId, []);

      for await (const message of this.realtimeStreaming!.messages()) {
        // // console.log('Message type:', message.type);

        switch (message.type) {
          case 'session.created':
            this.socketGateway.server
              .to(socketId)
              .emit('state', InputState.ReadyToStop);
            this.socketGateway.server
              .to(socketId)
              .emit('transcript', '<< Session Started >>\n');
            break;

          case 'response.audio.delta':
            try {
              const buffer = this.audioResponseBuffer.get(socketId) || [];
              buffer.push(message.delta);
              this.audioResponseBuffer.set(socketId, buffer);

              // Send accumulated audio chunks
              this.socketGateway.server
                .to(socketId)
                .emit('audio', message.delta);
            } catch (error) {
              console.error('Error handling audio delta:', error);
            }
            break;

          case 'input_audio_buffer.speech_started':
            this.socketGateway.server
              .to(socketId)
              .emit('transcript', '<< Speech Started >>\n');
            this.socketGateway.server.to(socketId).emit('audio', 'clear');
            break;

          case 'response.audio_transcript.delta':
            this.socketGateway.server
              .to(socketId)
              .emit('transcript', message.delta);
            break;

          case 'conversation.item.input_audio_transcription.completed':
            this.socketGateway.server
              .to(socketId)
              .emit('transcript', `User: ${message.transcript}\n`);
            break;

          case 'response.done':
            // Clear audio buffer after response is complete
            this.audioResponseBuffer.delete(socketId);
            this.socketGateway.server.to(socketId).emit('transcript', '---\n');
            break;
        }
      }
    } catch (error) {
      console.error('Error handling messages:', error);
      this.audioResponseBuffer.delete(socketId);
    }
  }

  private async saveAudioToMp3(buffer: Uint8Array, filename: string) {
    try {
      // Ensure uploads directory exists
      await mkdir(this.uploadsDir, { recursive: true });

      // Create full file paths
      const filePath = join(this.uploadsDir, filename);
      const tempPcmPath = join(this.uploadsDir, `${filename}.pcm`);

      // Save raw PCM data to temporary file
      await writeFile(tempPcmPath, Buffer.from(buffer));

      // Use node-av provided ffmpeg binary
      if (!isFfmpegAvailable()) {
        throw new Error(
          'FFmpeg binary not available (node-av). Install node-av or ensure ffmpeg binaries are available.',
        );
      }

      const ff = ffmpegPath();
      const args = [
        '-f',
        's16le',
        '-ar',
        String(this.sampleRate),
        '-ac',
        '1',
        '-i',
        tempPcmPath,
        '-acodec',
        'libmp3lame',
        '-ab',
        '128k',
        '-y',
        filePath,
      ];

      try {
        await execFileAsync(ff, args);
        await unlink(tempPcmPath);
        return true;
      } catch (err) {
        console.error('Error converting to MP3:', err);
        try {
          await unlink(tempPcmPath);
        } catch (error) {
          console.error('Error deleting temp PCM file:', error);
        }
        throw err;
      }
    } catch (error) {
      console.error('Error saving audio:', error);
      throw error;
    }
  }
}
