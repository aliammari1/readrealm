import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { HydratedDocument, SchemaTypes, Types } from 'mongoose';

export type UserDocument = HydratedDocument<User>;

@Schema()
export class User {
  @Prop({ required: true })
  username: string;

  @Prop({ required: true })
  email: string;

  @Prop({ required: true, maxlength: 100 })
  password: string;

  @Prop({ required: true, default: 'user' })
  role: string;

  @Prop({ required: false })
  emailVerifiedAt?: Date;
}

export const UserSchema = SchemaFactory.createForClass(User);
