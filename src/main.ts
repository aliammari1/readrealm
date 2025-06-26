import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';
import { json } from 'express';
import { ExpressAdapter } from '@nestjs/platform-express';
import * as express from 'express';

let app: any = null;

const createApp = async () => {
  if (!app) {
    const expressApp = express();
    app = await NestFactory.create(AppModule, new ExpressAdapter(expressApp));

    app.enableCors({
      origin: '*',
      methods: ['GET', 'POST', 'PUT', 'DELETE', 'PATCH', 'OPTIONS'],
      allowedHeaders: ['Content-Type', 'Authorization'],
    });

    app.use(json({ limit: '100mb' }));

    await app.init();
  }
  return app;
};

// Appwrite Function Handler
export default async (req: any, res: any) => {
  try {
    const nestApp = await createApp();
    const expressApp = nestApp.getHttpAdapter().getInstance();
    return expressApp(req, res);
  } catch (error) {
    console.error('Function error:', error);
    return res.status(500).json({ error: 'Internal server error' });
  }
};

// Keep the original bootstrap for local development
if (require.main === module) {
  async function bootstrap() {
    const app = await NestFactory.create(AppModule);
    app.enableCors();
    app.use(json({ limit: '100mb' }));
    await app.listen(process.env.PORT ?? 3000);
  }
  bootstrap();
}