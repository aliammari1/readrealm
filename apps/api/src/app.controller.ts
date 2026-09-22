import { Controller, Get } from '@nestjs/common';
import { ApiOperation, ApiTags } from '@nestjs/swagger';
import { AppService } from './app.service';

@ApiTags('app')
@Controller()
export class AppController {
  constructor(private readonly appService: AppService) {}

  @ApiOperation({ summary: 'Service metadata / hello' })
  @Get()
  async getHello() {
    return this.appService.getHello();
  }

  @ApiOperation({ summary: 'Service health check' })
  @Get('health')
  health() {
    return {
      status: 'ok',
      service: 'readrealm-api',
      timestamp: new Date().toISOString(),
    };
  }

  @ApiOperation({ summary: 'Service liveness check' })
  @Get('health/live')
  live() {
    return { status: 'ok' };
  }
}
