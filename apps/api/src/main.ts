import { NestFactory } from '@nestjs/core';
import { DocumentBuilder, SwaggerModule } from '@nestjs/swagger';
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

async function bootstrap() {
  const app = await NestFactory.create(AppModule);

  // NOTE (security): CORS is wide-open ('*') for development convenience.
  // Restrict to the deployed client origins before production. Tracked in the
  // security-review notes (shared/docs/security.md).
  app.enableCors({
    origin: '*',
  });

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
