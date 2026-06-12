import { Injectable } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { Message } from './entities/message.entity';

@Injectable()
export class ChatService {
  constructor(
    @InjectModel(Message.name) private messageModel: Model<Message>,
  ) {}

  async saveMessage(message: Partial<Message>): Promise<Message> {
    const newMessage = new this.messageModel(message);
    return newMessage.save();
  }

  async getRoomMessages(bookId: number): Promise<Message[]> {
    return this.messageModel
      .find({ bookId })
      .sort({ createdAt: -1 })
      .limit(50)
      .exec();
  }

  /**
   * Recent room messages in chronological (oldest-first) order, shaped for the
   * AI participant's conversation context.
   */
  async getRoomHistory(
    bookId: number,
    limit = 20,
  ): Promise<{ username: string; content: string; isAi: boolean }[]> {
    const messages = await this.messageModel
      .find({ bookId })
      .sort({ createdAt: -1 })
      .limit(limit)
      .exec();
    return messages.reverse().map((m) => ({
      username: m.username,
      content: m.content,
      isAi: !!m.isAi,
    }));
  }
}
