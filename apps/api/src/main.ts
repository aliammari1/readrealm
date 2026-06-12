// Sentry instrumentation must be the very first import (no-op without SENTRY_DSN).
import './instrument';

import { ValidationPipe } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
import { Logger } from 'nestjs-pino';
import helmet from 'helmet';
import { AppModule } from './app.module';
import { json } from 'express';

export function buildSwaggerConfig() {
  return new DocumentBuilder()
    .setTitle('ReadRealm API')
    .setDescription(
      'Backend for ReadRealm — an intelligent, collaborative digital library. ' +
        'One NestJS API serving the Android, iOS and Flutter clients: ' +
        'auth, books/EPUB, reviews, bookmarks, real-time book chat (Socket.IO) ' +
        'and multi-provider AI (Google / OpenAI / HuggingFace / Azure / Anthropic).',
    )
    .setVersion('1.0.0')
    .setLicense(
      'MIT',
      'https://github.com/aliammari1/readrealm/blob/main/LICENSE',
    )
    .addBearerAuth(
      { type: 'http', scheme: 'bearer', bearerFormat: 'JWT' },
      'access-token',
    )
    .addTag('auth', 'Registration, login, email verification and passwords')
    .addTag('book', 'Books, search, EPUB/TTS, reviews and bookmarks')
    .addTag('user', 'User profiles and management')
    .addTag('app', 'Health and service metadata')
    .build();
}

/**
 * Parse the CORS_ORIGIN env var into an `enableCors` origin value.
 * - unset / empty  -> CORS disabled (same-origin only) — the safe default
 * - '*'            -> reflect any origin (local demos only; warns)
 * - comma list     -> explicit allow-list of origins
 */
export function resolveCorsOrigin(
  raw: string | undefined,
): false | string | string[] {
  if (!raw || !raw.trim()) return false;
  const trimmed = raw.trim();
  if (trimmed === '*') return '*';
  return trimmed
    .split(',')
    .map((o) => o.trim())
    .filter(Boolean);
}

async function bootstrap() {
  const app = await NestFactory.create(AppModule, { bufferLogs: true });

  // Structured JSON logging (pino) with per-request correlation ids.
  app.useLogger(app.get(Logger));

  // Security headers (CSP, HSTS, X-Frame-Options, etc.). CSP is left off here
  // because the API serves Swagger UI assets; tighten per-route in front of a
  // proxy if the UI is exposed publicly.
  app.use(helmet({ contentSecurityPolicy: false }));

  // Lock CORS to an explicit allow-list (CORS_ORIGIN). Defaults to disabled
  // (same-origin only) rather than the previous wide-open '*'.
  const corsOrigin = resolveCorsOrigin(process.env.CORS_ORIGIN);
  if (corsOrigin === '*') {
    app
      .get(Logger)
      .warn(
        'CORS is wide-open (CORS_ORIGIN=*). Set explicit origins before production.',
      );
  }
  if (corsOrigin !== false) {
    app.enableCors({ origin: corsOrigin, credentials: true });
  }

  // Global input validation: strip unknown props, reject extras, auto-transform
  // payloads (and route params) into the typed DTOs.
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      forbidNonWhitelisted: true,
      transform: true,
      transformOptions: { enableImplicitConversion: true },
    }),
  );

  app.use(json({ limit: '100mb' }));

  const document = SwaggerModule.createDocument(app, buildSwaggerConfig());
  SwaggerModule.setup('api/docs', app, document, {
    swaggerOptions: { persistAuthorization: true },
  });

  await app.listen(process.env.PORT ?? 3000);
}

// Only start the HTTP server when run as the entrypoint, so this module can be
// imported by the OpenAPI generator (scripts/generate-openapi.ts) without
// opening a port.
if (require.main === module) {
  bootstrap();
}
