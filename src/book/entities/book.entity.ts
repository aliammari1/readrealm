import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { HydratedDocument } from 'mongoose';
import { Review } from './review.entity';

@Schema({ _id: false })
export class Bookmark {
  @Prop({ required: true })
  userId: string;

  @Prop({ required: true, default: Date.now })
  dateAdded: Date;
}

@Schema({
  timestamps: true,
  toJSON: { virtuals: true },
  toObject: { virtuals: true }
})
export class Book {
  @Prop({ required: true, unique: true, index: true })
  id: number;

  @Prop({ required: true, index: true })
  author: string;

  @Prop({ required: true, index: true })
  title: string;

  @Prop({ required: true })
  publicationDate: number;

  @Prop({ required: true })
  numOfPages: number;

  @Prop({ required: true })
  coverImage: string;

  @Prop({ required: true, index: true })
  genre: string;

  @Prop()
  textData: string;

  @Prop({ type: [Bookmark], default: [] })
  bookmarks: Bookmark[];

  @Prop({ type: [{ type: 'ObjectId', ref: 'Review' }], default: [] })
  reviews: Review[];

  @Prop({ type: Number, default: 0 })
  averageRating: number;

  @Prop({ type: Number, default: 0 })
  totalReviews: number;
}

export type BookDocument = HydratedDocument<Book>;
export const BookSchema = SchemaFactory.createForClass(Book);
