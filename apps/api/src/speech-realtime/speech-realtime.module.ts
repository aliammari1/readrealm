import { Module } from '@nestjs/common';
import { SpeechRealtimeService } from './speech-realtime.service';
import { SocketGateway } from './socket.gateway';
import { ConfigModule, ConfigService } from '@nestjs/config';

@Module({
  imports: [ConfigModule],
  providers: [SpeechRealtimeService, SocketGateway, ConfigService],
  exports: [SpeechRealtimeService],
})
export class SpeechRealtimeModule {}
