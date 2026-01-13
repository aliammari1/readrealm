import { Module } from '@nestjs/common';
import {
  Verification,
  VerificationSchema,
} from './entities/verification.entity';
import { VerificationService } from './verification.service';
import { MongooseModule } from '@nestjs/mongoose';
import { UserModule } from '../user/user.module';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: Verification.name, schema: VerificationSchema },
    ]),
    UserModule,
  ],
  providers: [VerificationService],
  exports: [VerificationService],
})
export class VerificationModule {}
