import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { HydratedDocument } from 'mongoose';

export type UserDocument = HydratedDocument<User>;

@Schema()
export class User {
  @Prop({ required: true })
  username: string;

  @Prop({ required: true })
  email: string;

  @Prop({ required: true, maxlength: 100 })
  password: string;

  @Prop({
    required: false,
    length: 1000,
  })
  profilePicture: string;

  @Prop({ required: true, default: 'user' })
  role: string;

  @Prop({ required: false })
  emailVerifiedAt?: Date;
}

export const UserSchema = SchemaFactory.createForClass(User);
