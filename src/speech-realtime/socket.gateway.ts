import {
  WebSocketGateway,
  OnGatewayConnection,
  OnGatewayDisconnect,
  WebSocketServer,
  SubscribeMessage,
  MessageBody,
  ConnectedSocket,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { SpeechRealtimeService } from './speech-realtime.service';
import { Logger, Inject, forwardRef } from '@nestjs/common';

@WebSocketGateway({
  cors: {
    origin: '*',
    methods: ['GET', 'POST'],
  },
  transports: ['websocket'],
  pingTimeout: 60000,
  pingInterval: 25000,
  maxHttpBufferSize: 1e8, // 100MB max buffer
})
export class SocketGateway implements OnGatewayConnection, OnGatewayDisconnect {
  // Events handled:
  // 1. 'start': Initiates a new session
  // 2. 'sendAudio': Processes incoming audio chunks
  // 3. 'stop': Ends the session
  
  // Events emitted:
  // 1. 'connectionStatus': Connection established
  // 2. 'sessionStatus': Session state
  // 3. 'transcript': Text transcription
  // 4. 'audio': Audio response from AI
  // 5. 'error': Error messages
  // 6. 'done': Session completion

  @WebSocketServer()
  server: Server;

  private logger = new Logger('WebSocket');

  private activeSessions = new Map<string, {
    isActive: boolean;
    lastActivity: number;
  }>();

  constructor(
    @Inject(forwardRef(() => SpeechRealtimeService))
    private readonly speechRealtimeService: SpeechRealtimeService,
  ) {}

  handleConnection(socket: Socket) {
    this.logger.log(`Client connected: ${socket.id}`);
    this.activeSessions.set(socket.id, {
      isActive: false,
      lastActivity: Date.now()
    });
    socket.emit('connectionStatus', { connected: true });
  }

  handleDisconnect(socket: Socket) {
    this.logger.log(`Client disconnected: ${socket.id}`);
    if (this.activeSessions.get(socket.id)?.isActive) {
      this.speechRealtimeService.stopRealtime(socket.id);
    }
    this.activeSessions.delete(socket.id);
  }

  @SubscribeMessage('start')
  async handleStart(
    @ConnectedSocket() socket: Socket,
    @MessageBody() data: any,
  ) {
    try {
      this.logger.log(`Received start request with data:`, data);
      
      // Clear any existing session
      if (this.activeSessions.get(socket.id)?.isActive) {
        await this.speechRealtimeService.stopRealtime(socket.id);
      }

      const { systemMessage, temperature } = data;
      if (!systemMessage || temperature === undefined) {
        throw new Error('Invalid session parameters');
      }

      this.activeSessions.set(socket.id, {
        isActive: true,
        lastActivity: Date.now()
      });

      this.logger.log(`Starting session for client ${socket.id}`);
      await this.speechRealtimeService.startRealtime(
        socket.id,
        systemMessage,
        parseFloat(temperature),
      );

      socket.emit('sessionStatus', { active: true });

    } catch (error) {
      this.logger.error(`Start session error: ${error.message}`);
      this.activeSessions.set(socket.id, {
        isActive: false,
        lastActivity: Date.now()
      });
      socket.emit('error', error.message);
    }
  }

  @SubscribeMessage('sendAudio')
  handleSendAudio(
    @MessageBody() data: { audio: string },
    @ConnectedSocket() socket: Socket,
  ) {
    try {
      if (!this.activeSessions.get(socket.id)?.isActive) {
        throw new Error('No active session');
      }

      if (!data.audio) {
        throw new Error('Invalid audio data');
      }

      const session = this.activeSessions.get(socket.id)!;
      session.lastActivity = Date.now();

      const audioBuffer = Buffer.from(data.audio, 'base64');
      this.speechRealtimeService.processAudioRecordingBuffer(
        audioBuffer,
        socket.id,
      );

    } catch (error) {
      this.logger.error(`Audio processing error: ${error.message}`);
      socket.emit('error', error.message);
    }
  }

  @SubscribeMessage('message')
  handleMessage(
    @MessageBody() message: string,
    @ConnectedSocket() socket: Socket,
  ) {
    this.logger.log(`Message from client ${socket.id}: ${message}`);
    // Handle additional message types if necessary
  }

  @SubscribeMessage('stop')
  async handleStop(@ConnectedSocket() socket: Socket) {
    try {
      const session = this.activeSessions.get(socket.id);
      if (!session?.isActive) {
        throw new Error('No active session to stop');
      }

      this.logger.log(`Stopping session for client ${socket.id}`);
      await this.speechRealtimeService.stopRealtime(socket.id);
      
      session.isActive = false;
      socket.emit('sessionStatus', { active: false });

    } catch (error) {
      this.logger.error(`Stop session error: ${error.message}`);
      socket.emit('error', error.message);
    }
  }
}
