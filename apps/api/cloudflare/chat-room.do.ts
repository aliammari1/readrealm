/**
 * ChatRoom — Cloudflare Durable Object hosting one realtime book-chat room.
 *
 * This is the CF-native replacement for the Socket.IO `ChatGateway`
 * (apps/api/src/chat/chat.gateway.ts). Socket.IO does not run on workerd, so on
 * Cloudflare each `book_<id>` room is a single Durable Object that owns the
 * WebSocket fan-out, using the **WebSocket Hibernation API** (the DO can be
 * evicted between messages without dropping connections, which keeps idle rooms
 * free).
 *
 * Routing (from the Worker entry, see worker-entry.ts):
 *   GET /ws/book/<bookId>  ->  env.CHAT_ROOMS.idFromName("book_"+bookId)
 *
 * Wire protocol (mirrors the Socket.IO events so clients converge):
 *   client -> { type: "join",  username }
 *   client -> { type: "chat",  username, content }
 *   server -> { type: "userJoined" | "userLeft" | "newMessage", ... }
 *
 * STATUS: implemented + self-contained. NEEDS-PORT for production:
 *   1. Persist messages (Atlas/D1) — `saveMessage` here only echoes; wire it to
 *      the same store the NestJS ChatService uses.
 *   2. AI participant: when a message @-mentions the assistant, call the
 *      Anthropic streaming path (mirror ChatAiService) and stream deltas back
 *      over the socket. Kept out of this scaffold to avoid duplicating the
 *      tool-use loop before the data layer is decided.
 *   3. Auth: validate the JWT (passed as a query param or subprotocol) before
 *      accepting the upgrade.
 */

export interface Env {
  CHAT_ROOMS: DurableObjectNamespace;
  ANTHROPIC_API_KEY?: string;
  MONGO_URL?: string;
}

interface ClientMeta {
  username: string;
}

type ClientMessage =
  | { type: 'join'; username: string }
  | { type: 'chat'; username: string; content: string };

export class ChatRoom {
  private readonly state: DurableObjectState;
  private readonly env: Env;

  constructor(state: DurableObjectState, env: Env) {
    this.state = state;
    this.env = env;
  }

  async fetch(request: Request): Promise<Response> {
    const upgrade = request.headers.get('Upgrade');
    if (upgrade !== 'websocket') {
      return new Response('Expected a WebSocket upgrade', { status: 426 });
    }

    const pair = new WebSocketPair();
    const [client, server] = [pair[0], pair[1]];

    // Hibernation API: the DO can be evicted while sockets stay open.
    this.state.acceptWebSocket(server);

    return new Response(null, { status: 101, webSocket: client });
  }

  async webSocketMessage(
    ws: WebSocket,
    message: string | ArrayBuffer,
  ): Promise<void> {
    let data: ClientMessage;
    try {
      data = JSON.parse(
        typeof message === 'string' ? message : new TextDecoder().decode(message),
      );
    } catch {
      ws.send(JSON.stringify({ type: 'error', error: 'invalid JSON' }));
      return;
    }

    if (data.type === 'join') {
      ws.serializeAttachment({ username: data.username } satisfies ClientMeta);
      this.broadcast({
        type: 'userJoined',
        username: data.username,
        timestamp: new Date().toISOString(),
      });
      return;
    }

    if (data.type === 'chat') {
      const saved = await this.saveMessage(data);
      this.broadcast({ type: 'newMessage', ...saved });
      return;
    }
  }

  async webSocketClose(ws: WebSocket): Promise<void> {
    const meta = ws.deserializeAttachment() as ClientMeta | null;
    if (meta?.username) {
      this.broadcast({
        type: 'userLeft',
        username: meta.username,
        timestamp: new Date().toISOString(),
      });
    }
  }

  private broadcast(payload: unknown): void {
    const json = JSON.stringify(payload);
    for (const socket of this.state.getWebSockets()) {
      try {
        socket.send(json);
      } catch {
        // Socket already closed; the close handler will clean it up.
      }
    }
  }

  // NEEDS-PORT: persist to the shared store (Atlas/D1) the same way
  // apps/api/src/chat/chat.service.ts does. For now this echoes the message so
  // the realtime fan-out is fully functional in isolation.
  private async saveMessage(data: {
    username: string;
    content: string;
  }): Promise<{ username: string; content: string; createdAt: string }> {
    return {
      username: data.username,
      content: data.content,
      createdAt: new Date().toISOString(),
    };
  }
}
