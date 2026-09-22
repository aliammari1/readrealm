import {
  WebSocketGateway,
  WebSocketServer,
  SubscribeMessage,
  MessageBody,
  ConnectedSocket,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { ChatService } from './chat.service';
import { ChatAiService } from './chat-ai.service';

/** Username the AI participant posts under. */
const AI_USERNAME = 'ReadRealm AI';
/** Mentions that summon the AI participant. */
const AI_MENTION = /@(ai|readrealm)\b/i;

@WebSocketGateway({
  cors: {
    // NOTE (security): wide-open for development. Restrict to the deployed
    // client origins before production. See shared/docs/security.md.
    origin: '*',
  },
})
export class ChatGateway {
  @WebSocketServer()
  server: Server;

  constructor(
    private readonly chatService: ChatService,
    private readonly chatAiService: ChatAiService,
  ) {}

  @SubscribeMessage('joinRoom')
  async handleJoinRoom(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { bookId: number; userId: string; username: string },
  ) {
    const roomId = `book_${data.bookId}`;
    client.join(roomId);

    // Get previous messages
    const messages = await this.chatService.getRoomMessages(data.bookId);
    client.emit('previousMessages', messages);

    // Notify others
    this.server.to(roomId).emit('userJoined', {
      username: data.username,
      timestamp: new Date(),
      aiEnabled: this.chatAiService.isEnabled(),
    });
  }

  @SubscribeMessage('leaveRoom')
  handleLeaveRoom(
    @ConnectedSocket() client: Socket,
    @MessageBody() data: { bookId: number; username: string },
  ) {
    const roomId = `book_${data.bookId}`;
    client.leave(roomId);
    this.server.to(roomId).emit('userLeft', {
      username: data.username,
      timestamp: new Date(),
    });
  }

  @SubscribeMessage('chatMessage')
  async handleMessage(
    @ConnectedSocket() client: Socket,
    @MessageBody()
    data: {
      bookId: number;
      userId: string;
      username: string;
      content: string;
      bookTitle?: string;
    },
  ) {
    const message = await this.chatService.saveMessage(data);
    const roomId = `book_${data.bookId}`;
    this.server.to(roomId).emit('newMessage', message);

    // Summon the AI participant when mentioned.
    if (AI_MENTION.test(data.content)) {
      await this.handleAiTurn(roomId, data);
    }
  }

  private async handleAiTurn(
    roomId: string,
    data: {
      bookId: number;
      username: string;
      content: string;
      bookTitle?: string;
    },
  ) {
    this.server.to(roomId).emit('aiTyping', { username: AI_USERNAME });

    const history = await this.chatService.getRoomHistory(data.bookId);

    try {
      const full = await this.chatAiService.streamReply(
        `${data.username}: ${data.content}`,
        history,
        { bookId: data.bookId, bookTitle: data.bookTitle },
        (delta) => {
          this.server.to(roomId).emit('aiMessageDelta', {
            username: AI_USERNAME,
            delta,
          });
        },
      );

      const saved = await this.chatService.saveMessage({
        bookId: data.bookId,
        userId: 'ai',
        username: AI_USERNAME,
        content: full,
        isAi: true,
      });
      this.server.to(roomId).emit('newMessage', saved);
    } finally {
      this.server.to(roomId).emit('aiDone', { username: AI_USERNAME });
    }
  }
}
