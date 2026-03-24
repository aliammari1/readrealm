import {
  WebSocketGateway,
  WebSocketServer,
  SubscribeMessage,
  MessageBody,
  ConnectedSocket,
} from '@nestjs/websockets';
import { Server, Socket } from 'socket.io';
import { ChatService } from './chat.service';

@WebSocketGateway({
  cors: {
    origin: '*',
  },
})
export class ChatGateway {
  @WebSocketServer()
  server: Server;

  constructor(private readonly chatService: ChatService) {}

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
    data: { bookId: number; userId: string; username: string; content: string },
  ) {
    const message = await this.chatService.saveMessage(data);
    const roomId = `book_${data.bookId}`;
    this.server.to(roomId).emit('newMessage', message);
  }
}
