import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { ConfigModule } from '@nestjs/config';
import { ChatService } from './chat.service';
import { ChatGateway } from './chat.gateway';
import { ChatAiService } from './chat-ai.service';
import { Message, MessageSchema } from './entities/message.entity';
import { BookModule } from '../book/book.module';

@Module({
  imports: [
    MongooseModule.forFeature([{ name: Message.name, schema: MessageSchema }]),
    ConfigModule,
    BookModule,
  ],
  providers: [ChatGateway, ChatService, ChatAiService],
})
export class ChatModule {}
