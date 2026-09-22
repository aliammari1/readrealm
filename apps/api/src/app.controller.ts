import { Controller, Get } from '@nestjs/common';
import { AppService } from './app.service';

@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @Get()
  async getHello() {
    return this.appService.getHello();
  }

  @Get('health')
  health() {
    return {
      status: 'ok',
      service: 'readrealm-api',
      timestamp: new Date().toISOString(),
    };
  }

  @Get('health/live')
  live() {
    return { status: 'ok' };
  }
}
