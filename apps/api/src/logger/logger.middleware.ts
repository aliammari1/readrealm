import { Injectable, Logger, NestMiddleware } from '@nestjs/common';
import { Request, Response, NextFunction } from 'express';

@Injectable()
export class LoggerMiddleware implements NestMiddleware {
  private logger = new Logger('HTTP');

  use(req: Request, res: Response, next: NextFunction) {
    const { method, originalUrl, body: requestBody } = req;
    const userAgent = req.get('user-agent') || '';

    // Capture the response body
    const oldSend = res.send;
    let responseBody: any;

    res.send = function (body) {
      responseBody = body;
      return oldSend.apply(res);
    };

    res.on('finish', () => {
      const { statusCode } = res;
      this.logger.log(
        `${method} ${originalUrl} ${statusCode} - ${userAgent} - Request Body: ${JSON.stringify(requestBody)} - Response Body: ${responseBody}`,
      );
    });

    next();
  }
}
