import { ValidationPipe } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { NestFactory } from '@nestjs/core';
import { json } from 'express';
import { AppModule } from './app.module';

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  const config = app.get(ConfigService);

  const configuredOrigins = config.get<string>('cors.origin') ?? '';
  const origins = configuredOrigins
    .split(',')
    .map((origin) => origin.trim())
    .filter(Boolean);

  app.enableCors({
    origin:
      origins.length === 0 || origins.includes('*')
        ? true
        : (origin, callback) => {
            if (!origin || origins.includes(origin)) {
              callback(null, true);
              return;
            }
            callback(new Error('Origin not allowed by CORS'), false);
          },
    credentials: true,
  });

  app.use(
    json({
      limit: config.get<string>('server.maxJsonBodySize') ?? '10mb',
    }),
  );

  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true,
      transform: true,
      forbidNonWhitelisted: true,
    }),
  );

  await app.listen(process.env.PORT ?? 3000);
}
bootstrap();
