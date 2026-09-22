import {
  ConnectedSocket,
  OnGatewayConnection,
  OnGatewayDisconnect,
  SubscribeMessage,
  WebSocketGateway,
  WebSocketServer,
} from '@nestjs/websockets';
import { Logger } from '@nestjs/common';
import { Server, Socket } from 'socket.io';
import { SpeechRealtimeService } from './speech-realtime.service';

@WebSocketGateway({
  namespace: '/speech',
  cors: {
    origin: process.env.CORS_ORIGIN?.split(',') ?? ['http://localhost:3000'],
    methods: ['GET', 'POST'],
  },
  transports: ['websocket'],
  pingTimeout: 60000,
  pingInterval: 25000,
})
export class SocketGateway implements OnGatewayConnection, OnGatewayDisconnect {
  @WebSocketServer()
  server: Server;

  private readonly logger = new Logger(SocketGateway.name);

  constructor(private readonly speechRealtimeService: SpeechRealtimeService) {}

  handleConnection(socket: Socket) {
    this.logger.log(`Speech client connected: ${socket.id}`);
    socket.emit('connectionStatus', { connected: true });
  }

  handleDisconnect(socket: Socket) {
    this.logger.log(`Speech client disconnected: ${socket.id}`);
  }

  @SubscribeMessage('start')
  async handleStart(@ConnectedSocket() socket: Socket) {
    try {
      const signedUrl = await this.speechRealtimeService.getSignedUrl();
      socket.emit('elevenlabsSignedUrl', { signedUrl });
      socket.emit('sessionStatus', {
        active: true,
        transport: 'elevenlabs-direct',
      });
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : 'Unable to start voice session';
      this.logger.error(message);
      socket.emit('error', message);
    }
  }

  @SubscribeMessage('sendAudio')
  handleLegacyAudio(@ConnectedSocket() socket: Socket) {
    socket.emit('migrationRequired', {
      message:
        'Connect the client directly to the ElevenLabs signed WebSocket URL returned by the start event.',
    });
  }

  @SubscribeMessage('stop')
  handleStop(@ConnectedSocket() socket: Socket) {
    socket.emit('sessionStatus', { active: false });
  }
}
