import { Module } from '@nestjs/common';
import { AuthService } from './auth.service';
import { AuthController } from './auth.controller';
import { UserModule } from '../user/user.module'; // Import UserModule
import { RefreshTokenSchema } from './dto/refresh-token.schema';
import { JwtModule } from '@nestjs/jwt';
import { MongooseModule } from '@nestjs/mongoose';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { MailService } from '../services/mail.service';
import { VerificationModule } from '../verification/verification.module';

@Module({
  imports: [
    JwtModule.registerAsync({
      imports: [ConfigModule],
      useFactory: async (config) => ({
        secret: config.get('jwt.secret'),
      }),
      global: true,
      inject: [ConfigService],
    }),
    MongooseModule.forFeature([
      { name: 'RefreshToken', schema: RefreshTokenSchema },
    ]),
    UserModule,
    VerificationModule,
  ],
  providers: [AuthService, MailService],
  controllers: [AuthController],
})
export class AuthModule {}
