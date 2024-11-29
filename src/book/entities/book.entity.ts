import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { randomInt } from 'crypto';
import { HydratedDocument, Types } from 'mongoose';

export type BookDocument = HydratedDocument<Book>;

@Schema()
export class Book {
  id: number = randomInt(0,1000000);

  @Prop({ required: true })
  author: string;

  @Prop({ required: true })
  title: string;

  @Prop({ required: true })
  publicationDate: number;

  @Prop({ required: true })
  numOfPages: number;

  @Prop({ required: true })
  coverImage: String;

  @Prop({ required: true })
  genre: String;
}

export const UserSchema = SchemaFactory.createForClass(Book);
