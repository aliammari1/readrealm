import { Controller, Get, UseGuards } from '@nestjs/common';
import { AuthGaurd } from '../guards/authentification.guards';
import { SpeechRealtimeService } from './speech-realtime.service';

@Controller('speech-realtime')
export class SpeechRealtimeController {
  constructor(private readonly speechRealtimeService: SpeechRealtimeService) {}

  @UseGuards(AuthGaurd)
  @Get('signed-url')
  async getSignedUrl() {
    return {
      signedUrl: await this.speechRealtimeService.getSignedUrl(),
    };
  }
}
