# ReadRealm API on Cloudflare (config + scaffold — not deployed)

This folder holds the Cloudflare deployment design for the ReadRealm API. **Nothing
here is deployed** — these are the wrangler config and the Workers/Durable-Objects
source so the edge path is reviewable. No `wrangler deploy` is wired into CI; a
real deploy needs a Cloudflare account + secrets.

## The plan

| Workload | Cloudflare primitive | Status |
|---|---|---|
| NestJS HTTP API (REST) | **Workers** with `nodejs_compat` | scaffold — `worker-entry.ts` returns 501 for HTTP; see *NEEDS-PORT* |
| Realtime book chat (was Socket.IO) | **Durable Object** (`ChatRoom`), one per `book_<id>` room, WebSocket Hibernation API | **implemented** (`chat-room.do.ts`) |
| Database | **MongoDB Atlas** over the Workers TCP socket API | config note (keep Mongoose models) |

## Why these choices (engineering decisions)

- **Workers + `nodejs_compat` over a rewrite.** The API is a mature NestJS app
  with Mongoose models, guards, and multi-provider AI. `nodejs_compat` lets the
  existing Express-based app run on workerd without rewriting it in Hono. The
  one piece of real work is bridging Node's `http` req/res to the Workers
  fetch handler — left as a clearly-scoped NEEDS-PORT rather than shipping a
  half-working bridge that breaks the existing `apps/api` Express server.

- **Durable Objects for realtime, not Socket.IO.** Socket.IO's server cannot run
  on workerd (it needs a long-lived Node server + its own transport). The
  CF-native realtime primitive is a Durable Object that owns one WebSocket hub
  per room. `ChatRoom` is a full, self-contained implementation using the
  **Hibernation API** (idle rooms cost nothing; sockets survive eviction). The
  existing NestJS `ChatGateway` stays the source of truth for non-CF
  deployments — the DO is an **additive** port, so the existing gateway is not
  broken.

- **MongoDB Atlas over D1.** D1 is SQLite; adopting it means rewriting every
  Mongoose schema and query to SQL. To keep the data model intact we connect to
  **Atlas** from the Worker (TCP socket driver, or the Atlas Data API over HTTP
  if the TCP driver is too heavy on workerd). D1 remains an option *if/when* the
  data layer is intentionally migrated — documented, not done.

## NEEDS-PORT (remaining work before a deploy)

1. **NestJS-on-workerd HTTP bridge** in `worker-entry.ts` (the larger half).
2. **Persist DO messages** to the same store as `ChatService` (`saveMessage` in
   `chat-room.do.ts` currently echoes).
3. **AI participant on the edge** — mirror `ChatAiService`'s streaming Claude +
   tool-use loop inside the DO once the data layer is settled.
4. **Auth on the WebSocket upgrade** — validate the JWT before `acceptWebSocket`.
5. **`cloudflare/wrangler-action`** deploy job (gated on `CLOUDFLARE_API_TOKEN`),
   plus `@cloudflare/vitest-pool-workers` tests for the DO.

## Local check

```bash
# Type-check the edge sources against @cloudflare/workers-types (add as a devDep):
npx wrangler deploy --dry-run --config apps/api/cloudflare/wrangler.toml   # config validation only
```
