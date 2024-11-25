import { Schema, Prop, SchemaFactory } from '@nestjs/mongoose';
import { Document } from 'mongoose';

@Schema()
export class RefreshToken extends Document {
  @Prop({ required: true })
  userId: string; // L'ID de l'utilisateur auquel le token est associé

  @Prop()
  email: string;

  @Prop({ required: true })
  token: string; // Le token d'actualisation

  @Prop({ required: true })
  expiryDate: Date; // La date d'expiration du token
}

export const RefreshTokenSchema = SchemaFactory.createForClass(RefreshToken);
