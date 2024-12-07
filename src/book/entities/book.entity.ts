import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { HydratedDocument } from 'mongoose';

@Schema({ _id: false })
class Review {
  @Prop({ required: true })
  userId: string;

  @Prop({ required: true })
  comment: string;

  @Prop({ required: true })
  rating: number;

  @Prop({ required: true })
  date: Date;
}

@Schema({ _id: false })
class Bookmark {
  @Prop({ required: true })
  userId: string;

  @Prop({ required: true })
  dateAdded: Date;

  @Prop()
  note?: string;
}

export type BookDocument = HydratedDocument<Book>;

@Schema()
export class Book {
  @Prop({ required: true, unique: true })
  id: number;

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

  @Prop({ type: [Review], default: [] })
  reviews: Review[];

  @Prop({ default: 0 })
  totalRating: number;

  @Prop({ default: 0 })
  numberOfRatings: number;

  @Prop({ type: [Bookmark], default: [] })
  bookmarks: Bookmark[];

  @Prop()
  textData: string;

  public getAverageRating(): number {
    return this.numberOfRatings > 0 ? this.totalRating / this.numberOfRatings : 0;
  }
}

export const BookSchema = SchemaFactory.createForClass(Book);

BookSchema.methods.getAverageRating = function() {
  return this.numberOfRatings ? this.totalRating / this.numberOfRatings : 0;
};
