import { Module } from '@nestjs/common';
import { APP_FILTER, APP_GUARD } from '@nestjs/core';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { AuthModule } from './auth/auth.module';
import { MongooseModule } from '@nestjs/mongoose';
import { JwtModule } from '@nestjs/jwt';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { LoggerModule } from 'nestjs-pino';
import { SentryGlobalFilter, SentryModule } from '@sentry/nestjs/setup';
import { ThrottlerGuard, ThrottlerModule } from '@nestjs/throttler';
import { BookModule } from './book/book.module';
import config from './config/config';
import { SpeechRealtimeModule } from './speech-realtime/speech-realtime.module';
import { ChatModule } from './chat/chat.module';

@Module({
  imports: [
    // Sentry request/error instrumentation (no-op without SENTRY_DSN).
    SentryModule.forRoot(),
    ConfigModule.forRoot({
      isGlobal: true,
      cache: true,
      load: [config],
    }),
    // Structured JSON logging with per-request correlation ids. Redacts auth
    // headers and known secret-bearing fields so logs are safe to ship.
    LoggerModule.forRoot({
      pinoHttp: {
        level: process.env.LOG_LEVEL ?? 'info',
        autoLogging: true,
        redact: [
          'req.headers.authorization',
          'req.headers.cookie',
          'req.body.password',
          'req.body.oldPassword',
          'req.body.newPassword',
        ],
        transport:
          (process.env.NODE_ENV ?? 'development') !== 'production'
            ? { target: 'pino-pretty', options: { singleLine: true } }
            : undefined,
      },
    }),
    // Rate limiting. Applied globally via ThrottlerGuard below; auth and
    // AI/TTS routes tighten this further with per-route @Throttle decorators.
    ThrottlerModule.forRoot([{ name: 'default', ttl: 60_000, limit: 120 }]),
    JwtModule.registerAsync({
      imports: [ConfigModule],
      useFactory: async (config) => ({
        secret: config.get('jwt.secret'),
      }),
      global: true,
      inject: [ConfigService],
    }),
    MongooseModule.forRootAsync({
      imports: [ConfigModule],
      useFactory: async (config) => ({
        uri: config.get('database.connectionString'),
      }),
      inject: [ConfigService],
    }),
    AuthModule,
    BookModule,
    SpeechRealtimeModule,
    ChatModule,
  ],
  controllers: [AppController],
  providers: [
    AppService,
    // Reports unhandled exceptions to Sentry (no-op without SENTRY_DSN) while
    // preserving Nest's default HTTP error responses.
    { provide: APP_FILTER, useClass: SentryGlobalFilter },
    // Global rate-limit guard.
    { provide: APP_GUARD, useClass: ThrottlerGuard },
  ],
})
export class AppModule {}
