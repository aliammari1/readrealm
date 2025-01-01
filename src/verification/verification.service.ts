import { Injectable, UnprocessableEntityException } from '@nestjs/common';
import * as bcrypt from 'bcryptjs';
import { Verification } from './entities/verification.entity';
import { generateOtp } from './utils/otp.util';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { UserService } from 'src/user/user.service';

@Injectable()
export class VerificationService {
  private readonly minRequestIntervalMinutes = 1;
  private readonly tokenExpirationMinutes = 15;
  private readonly saltRounds = 10;

  constructor(
    @InjectModel(Verification.name)
    private VerificationModel: Model<Verification>,
    private readonly userService: UserService,
  ) {}

  async generateOtp(userId: number, size = 6): Promise<string> {
    const now = new Date();

    const recentToken = await this.VerificationModel.findOne({
      userId,
      createdAt: {
        $gt: new Date(
          now.getTime() - this.minRequestIntervalMinutes * 60 * 1000,
        ),
      },
    });

    if (recentToken) {
      throw new UnprocessableEntityException(
        'Please wait a minute before requesting a new token.',
      );
    }

    const otp = generateOtp(size);
    const hashedToken = await bcrypt.hash(otp, this.saltRounds);

    const tokenEntity = new this.VerificationModel({
      userId,
      token: hashedToken,
      expiresAt: new Date(
        now.getTime() + this.tokenExpirationMinutes * 60 * 1000,
      ),
    });

    await this.VerificationModel.deleteMany({ userId });

    await tokenEntity.save();

    return otp;
  }

  async validateOtp(email: string, token: string): Promise<boolean> {
    const user = await this.userService.findByEmail(email);
    // // console.log(user);
    const validToken = await this.VerificationModel.findOne({
      userId: user.id,
      expiresAt: { $gt: new Date() },
    });

    if (validToken && (await bcrypt.compare(token, validToken.token))) {
      await this.VerificationModel.deleteOne({ _id: validToken._id });
      return true;
    } else {
      return false;
    }
  }

  async cleanUpExpiredTokens() {
    await this.VerificationModel.deleteMany({ expiresAt: { $lt: new Date() } });
  }
}
