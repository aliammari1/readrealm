/**
 * Cloudflare Worker entry point for the ReadRealm API (config/scaffold — NOT
 * deployed). Two responsibilities:
 *
 *   1. Route `GET /ws/book/<id>` to the per-room ChatRoom Durable Object — the
 *      CF-native realtime path (replaces Socket.IO on the edge).
 *   2. Hand every other request to the NestJS HTTP app running under
 *      `nodejs_compat`.
 *
 * The Durable Object class is re-exported so the `wrangler.toml` migration can
 * bind it.
 *
 * NEEDS-PORT (the larger half of the Workers port):
 *   Booting NestJS on workerd. NestExpress can run on `nodejs_compat`, but it
 *   must be created once per isolate and the Node `http` request/response has to
 *   be bridged to the Workers `Request`/`Response`. The cleanest production
 *   approach is to compile the Nest app to a single handler and adapt it with a
 *   Node-HTTP-to-fetch shim (e.g. the `@cloudflare/...` express adapter pattern).
 *   This is sizeable, so it is intentionally left as a clearly-marked stub here
 *   rather than shipping a half-working HTTP bridge that breaks the existing
 *   Express server. The Durable Object realtime path *is* fully implemented.
 */
import { ChatRoom, type Env } from './chat-room.do';

export { ChatRoom };

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const url = new URL(request.url);

    // Realtime book chat -> Durable Object (one instance per book room).
    const wsMatch = url.pathname.match(/^\/ws\/book\/([^/]+)$/);
    if (wsMatch) {
      const bookId = wsMatch[1];
      const id = env.CHAT_ROOMS.idFromName(`book_${bookId}`);
      const stub = env.CHAT_ROOMS.get(id);
      return stub.fetch(request);
    }

    // NEEDS-PORT: forward to the NestJS app (nodejs_compat). Until the Express
    // bridge lands, return a clear 501 so the realtime path can be deployed and
    // tested independently of the HTTP API port.
    return new Response(
      JSON.stringify({
        error: 'not_implemented',
        message:
          'The HTTP API on Workers is not wired yet. See apps/api/cloudflare/README.md ' +
          '(NestJS-on-workerd is NEEDS-PORT). The realtime chat Durable Object at ' +
          '/ws/book/<id> is implemented.',
      }),
      { status: 501, headers: { 'content-type': 'application/json' } },
    );
  },
};
