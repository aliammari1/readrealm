import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Date, HydratedDocument, Types } from 'mongoose';

export type BookDocument = HydratedDocument<Book>;

@Schema()
export class Book {
  @Prop({ required: true })
  author: string;

  @Prop({ required: true })
  title: string;

  @Prop({ required: true, type: Date })
  dateDePublication: Date;

  @Prop({ required: true })
  numPages: number;
}

export const UserSchema = SchemaFactory.createForClass(Book);
