import { HttpModule } from '@nestjs/axios';
import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { AuthGaurd } from '../guards/authentification.guards';
import { SpeechRealtimeController } from './speech-realtime.controller';
import { SpeechRealtimeService } from './speech-realtime.service';
import { SocketGateway } from './socket.gateway';

@Module({
  imports: [ConfigModule, HttpModule],
  controllers: [SpeechRealtimeController],
  providers: [SpeechRealtimeService, SocketGateway, AuthGaurd],
  exports: [SpeechRealtimeService],
})
export class SpeechRealtimeModule {}
