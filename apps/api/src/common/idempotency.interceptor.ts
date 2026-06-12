import {
  CallHandler,
  ExecutionContext,
  Injectable,
  NestInterceptor,
} from '@nestjs/common';
import { Observable, of } from 'rxjs';
import { tap } from 'rxjs/operators';

interface CacheEntry {
  expiresAt: number;
  response: unknown;
}

/**
 * Idempotency for expensive/paid POSTs (AI summaries, TTS generation).
 *
 * Clients send an `Idempotency-Key` header (any opaque, client-generated id,
 * e.g. a UUID per logical request). The first call runs normally and its
 * response is cached; retries with the same key replay the cached response
 * instead of re-billing the AI/TTS provider — making client retries safe.
 *
 * Storage is a process-local TTL map: zero-dependency and correct for a single
 * instance. For a horizontally-scaled deploy, back this with Redis/Mongo (the
 * interceptor surface stays the same). Requests without the header pass through
 * unchanged, so it is fully opt-in.
 */
@Injectable()
export class IdempotencyInterceptor implements NestInterceptor {
  private static readonly TTL_MS = 24 * 60 * 60 * 1000; // 24h
  private readonly store = new Map<string, CacheEntry>();

  intercept(context: ExecutionContext, next: CallHandler): Observable<unknown> {
    const req = context.switchToHttp().getRequest();
    const key = req.headers['idempotency-key'] as string | undefined;

    if (!key) {
      return next.handle();
    }

    this.evictExpired();

    const cacheKey = `${req.method}:${req.originalUrl}:${key}`;
    const cached = this.store.get(cacheKey);
    if (cached && cached.expiresAt > Date.now()) {
      return of(cached.response);
    }

    return next.handle().pipe(
      tap((response) => {
        this.store.set(cacheKey, {
          response,
          expiresAt: Date.now() + IdempotencyInterceptor.TTL_MS,
        });
      }),
    );
  }

  private evictExpired(): void {
    const now = Date.now();
    for (const [k, v] of this.store) {
      if (v.expiresAt <= now) this.store.delete(k);
    }
  }
}
