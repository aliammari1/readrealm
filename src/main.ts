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

// Appwrite Function Handler - Updated for latest runtime
export default async (context: any) => {
  const startTime = Date.now();

  try {
    // Use context.log instead of console.log for better Appwrite integration
    context.log('Context keys:', Object.keys(context || {}));

    // Extract request and response from context
    const req = context.req;
    const res = context.res;

    // Ensure we have the necessary request data
    if (!req) {
      context.error('No request object found in context');
      return context.res.json({
        error: 'No request object found'
      }, 400);
    }

    // Set default values if missing
    req.method = req.method || 'GET';
    req.url = req.url || req.path || '/';
    req.headers = req.headers || {};
    
    // Parse body if it's a string
    if (typeof req.body === 'string') {
      try {
        req.body = JSON.parse(req.body);
      } catch (e) {
        req.body = {};
      }
    } else {
      req.body = req.body || {};
    }

    context.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);

    const nestApp = await createApp();
    const expressApp = nestApp.getHttpAdapter().getInstance();

    // Create a promise to handle the Express app execution
    return new Promise((resolve) => {
      // Create a custom response wrapper that works with Appwrite
      const responseWrapper = {
        statusCode: 200,
        headers: {},
        body: '',
        finished: false,

        status(code: number) {
          this.statusCode = code;
          return this;
        },

        json(data: any) {
          this.headers['Content-Type'] = 'application/json';
          this.body = JSON.stringify(data);
          this.finished = true;
          resolve(context.res.json(data, this.statusCode));
          return this;
        },

        send(data: any) {
          this.body = typeof data === 'string' ? data : JSON.stringify(data);
          this.finished = true;
          if (typeof data === 'object') {
            resolve(context.res.json(data, this.statusCode));
          } else {
            resolve(context.res.send(data, this.statusCode));
          }
          return this;
        },

        end(data?: any) {
          if (data) {
            this.body = typeof data === 'string' ? data : JSON.stringify(data);
          }
          this.finished = true;
          resolve(context.res.send(this.body || '', this.statusCode));
          return this;
        },

        setHeader(name: string, value: string) {
          this.headers[name] = value;
          return this;
        },

        getHeader(name: string) {
          return this.headers[name];
        },

        removeHeader(name: string) {
          delete this.headers[name];
          return this;
        },

        write(chunk: any) {
          this.body += chunk;
          return true;
        },

        writeHead(statusCode: number, headers?: any) {
          this.statusCode = statusCode;
          if (headers) {
            Object.assign(this.headers, headers);
          }
          return this;
        },

        get headersSent() {
          return this.finished;
        }
      };

      // Add a timeout to prevent hanging
      const timeout = setTimeout(() => {
        if (!responseWrapper.finished) {
          context.error('Function timeout after 30 seconds');
          resolve(context.res.json({ error: 'Gateway timeout' }, 504));
        }
      }, 30000);

      try {
        // Handle the request through Express
        expressApp(req, responseWrapper as any, (err: any) => {
          clearTimeout(timeout);
          if (err && !responseWrapper.finished) {
            context.error('Express error:', err);
            resolve(context.res.json({ error: 'Internal server error' }, 500));
          } else if (!responseWrapper.finished) {
            // If no response was sent, send a default 404
            resolve(context.res.json({ error: 'Not found' }, 404));
          }
        });
      } catch (expressError) {
        clearTimeout(timeout);
        context.error('Express handling error:', expressError);
        if (!responseWrapper.finished) {
          resolve(context.res.json({ error: 'Express handling failed' }, 500));
        }
      }
    });

  } catch (error) {
    const duration = Date.now() - startTime;
    context.error(`Function error after ${duration}ms:`, error);

    // Return a safe error response using Appwrite's response object
    return context.res.json({
      error: 'Internal server error',
      message: error.message,
      timestamp: new Date().toISOString(),
      duration: duration
    }, 500);
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