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
}
