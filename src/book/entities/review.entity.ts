import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';

@Schema({
  timestamps: true,
})
export class Review {
  @Prop({ required: true })
  userId: string;

  @Prop({ required: true })
  bookId: number;

  @Prop({ required: true, min: 1, max: 5 })
  rating: number;

  @Prop({ required: true })
  comment: string;

  @Prop({ required: false, default: '' })
  emotion: string;
}

export const ReviewSchema = SchemaFactory.createForClass(Review);
