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
    // Log the context to understand its structure
    console.log('Context keys:', Object.keys(context || {}));

    // Extract request information from context
    const req = context.req || context.request || context;
    const res = context.res || context.response;

    // Ensure we have the necessary request data
    if (!req) {
      console.error('No request object found in context');
      return {
        statusCode: 400,
        body: JSON.stringify({ error: 'No request object found' }),
        headers: { 'Content-Type': 'application/json' }
      };
    }

    // Set default values if missing
    req.method = req.method || 'GET';
    req.url = req.url || req.path || '/';
    req.headers = req.headers || {};
    req.body = req.body || {};

    console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);

    const nestApp = await createApp();
    const expressApp = nestApp.getHttpAdapter().getInstance();

    // If we have a proper response object, use it
    if (res && typeof res.status === 'function') {
      return expressApp(req, res);
    }

    // Otherwise, handle as serverless function
    return new Promise((resolve, reject) => {
      // Create a proper mock response object that mimics Express response
      const mockRes = {
        statusCode: 200,
        _headers: {},
        _body: '',
        _headersSent: false,
        _finished: false,

        get headersSent() {
          return this._headersSent;
        },

        get finished() {
          return this._finished;
        },

        status(code: number) {
          this.statusCode = code;
          return this;
        },

        json(data: any) {
          this._headers['Content-Type'] = 'application/json';
          this._body = JSON.stringify(data);
          this._headersSent = true;
          this._finished = true;
          resolve({
            statusCode: this.statusCode,
            body: this._body,
            headers: this._headers
          });
          return this;
        },

        send(data: any) {
          this._body = typeof data === 'string' ? data : JSON.stringify(data);
          this._headersSent = true;
          this._finished = true;
          resolve({
            statusCode: this.statusCode,
            body: this._body,
            headers: this._headers
          });
          return this;
        },

        end(data?: any) {
          if (data) {
            this._body = typeof data === 'string' ? data : JSON.stringify(data);
          }
          this._headersSent = true;
          this._finished = true;
          resolve({
            statusCode: this.statusCode,
            body: this._body,
            headers: this._headers
          });
          return this;
        },

        setHeader(name: string, value: string) {
          this._headers[name] = value;
          return this;
        },

        getHeader(name: string) {
          return this._headers[name];
        },

        removeHeader(name: string) {
          delete this._headers[name];
          return this;
        },

        write(chunk: any) {
          this._body += chunk;
          return true;
        },

        writeHead(statusCode: number, headers?: any) {
          this.statusCode = statusCode;
          if (headers) {
            Object.assign(this._headers, headers);
          }
          return this;
        }
      };

      // Add a timeout to prevent hanging
      const timeout = setTimeout(() => {
        if (!mockRes._finished) {
          resolve({
            statusCode: 504,
            body: JSON.stringify({ error: 'Gateway timeout' }),
            headers: { 'Content-Type': 'application/json' }
          });
        }
      }, 30000); // 30 second timeout

      try {
        expressApp(req, mockRes as any, (err: any) => {
          clearTimeout(timeout);
          if (err && !mockRes._finished) {
            console.error('Express error:', err);
            resolve({
              statusCode: 500,
              body: JSON.stringify({ error: 'Internal server error' }),
              headers: { 'Content-Type': 'application/json' }
            });
          }
        });
      } catch (expressError) {
        clearTimeout(timeout);
        console.error('Express handling error:', expressError);
        if (!mockRes._finished) {
          resolve({
            statusCode: 500,
            body: JSON.stringify({ error: 'Express handling failed' }),
            headers: { 'Content-Type': 'application/json' }
          });
        }
      }
    });

  } catch (error) {
    const duration = Date.now() - startTime;
    console.error(`Function error after ${duration}ms:`, error);

    // Return a safe error response
    return {
      statusCode: 500,
      body: JSON.stringify({
        error: 'Internal server error',
        message: error.message,
        timestamp: new Date().toISOString(),
        duration: duration
      }),
      headers: { 'Content-Type': 'application/json' }
    };
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