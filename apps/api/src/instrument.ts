// Sentry must be initialized before anything else is imported, so this file is
// imported at the very top of main.ts (see `import './instrument';`).
//
// No-op when SENTRY_DSN is unset, so local/dev runs and CI need no Sentry
// account. Set SENTRY_DSN (and optionally SENTRY_TRACES_SAMPLE_RATE) in prod.
import * as Sentry from '@sentry/nestjs';

const dsn = process.env.SENTRY_DSN;

if (dsn) {
  Sentry.init({
    dsn,
    environment: process.env.NODE_ENV ?? 'development',
    tracesSampleRate: Number(process.env.SENTRY_TRACES_SAMPLE_RATE ?? '0.1'),
    // Don't leak request bodies (book text, chat prompts, credentials) to Sentry.
    sendDefaultPii: false,
  });
}
