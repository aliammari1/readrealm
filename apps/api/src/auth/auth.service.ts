import {
  Injectable,
  BadRequestException,
  UnauthorizedException,
  NotFoundException,
  UnprocessableEntityException,
} from '@nestjs/common';
import { UserService } from '../user/user.service'; // Import UserService
import { v4 as uuidv4 } from 'uuid';
import * as bcrypt from 'bcryptjs';
import { SignupDto } from './dto/signUpDto';
import { loginDto } from './dto/loginDto';
import { JwtService } from '@nestjs/jwt'; // Proper import of JwtService
import { InjectConnection, InjectModel } from '@nestjs/mongoose';
import { Connection, Model, Types } from 'mongoose';
import { RefreshToken } from './dto/refresh-token.schema'; // Import your RefreshToken schema
import { MailService } from '../services/mail.service';
import { VerificationService } from '../verification/verification.service';

@Injectable()
export class AuthService {
  constructor(
    private readonly mailService: MailService,
    private readonly userService: UserService,
    private readonly jwtService: JwtService, // Inject JwtService properly
    private readonly verificationService: VerificationService,
    @InjectModel('RefreshToken')
    private readonly refreshTokenModel: Model<RefreshToken>,
    @InjectConnection()
    private readonly connection: Connection,
  ) {}

  async register(signupData: SignupDto) {
    const { email, password, username, profilePicture } = signupData;

    const emailInUse = await this.userService.findByEmail(email.toLowerCase());
    if (emailInUse) {
      throw new BadRequestException('Email already in use');
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    await this.userService.addUser({
      username,
      email: email.toLowerCase(),
      password: hashedPassword,
      profilePicture: profilePicture, // Add this line
    });

    return {
      message: 'User registered successfully',
    };
  }

  async login(credentiales: loginDto) {
    const { email, password } = credentiales;

    const user = await this.userService.findByEmail(email.toLowerCase());
    if (!user) {
      throw new UnauthorizedException('Wrong credentials');
    }

    const passwordMatch = await bcrypt.compare(password, user.password);
    if (!passwordMatch) {
      throw new UnauthorizedException('Wrong credentials');
    }

    const tokens = await this.generateUserTokens(user.id, user.email);
    return {
      ...tokens,
      userId: user.id,
    };
  }

  async generateUserTokens(userId: string, email: string) {
    const accessToken = this.jwtService.sign(
      { userId, email },
      { expiresIn: '1h' },
    );
    const refreshToken = uuidv4(); // Generate a UUID for the refresh token

    await this.storeRefreshToken(refreshToken, userId);

    return {
      accessToken,
      refreshToken,
    };
  }

  async storeRefreshToken(token: string, userId: string) {
    // Token should be a string
    const expiryDate = new Date();
    expiryDate.setDate(expiryDate.getDate() + 3);

    await this.refreshTokenModel.updateOne(
      { userId },
      { $set: { expiryDate, token } },
      { upsert: true }, // Insert if it doesn't exist, update if it does
    );
  }

  async refreshTokens(refreshToken: string) {
    const token = await this.refreshTokenModel.findOne({
      token: refreshToken,
      expiryDate: { $gte: new Date() },
    });

    if (!token) {
      throw new UnauthorizedException('Refresh token is invalid');
    }

    return this.generateUserTokens(token.userId, token.email);
  }
  async deleteAccount(userId: string) {
    const user = await this.userService.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found');
    }

    const reviews = this.connection.collection('reviews');
    const books = this.connection.collection('books');
    const messages = this.connection.collection('messages');
    const verifications = this.connection.collection('verifications');

    const userReviews = await reviews
      .find({ userId })
      .project({ _id: 1, bookId: 1 })
      .toArray();
    const reviewIds = userReviews.map((review) => review._id);
    const affectedBookIds = [
      ...new Set(
        userReviews
          .map((review) => review.bookId)
          .filter((bookId): bookId is number => typeof bookId === 'number'),
      ),
    ];

    await Promise.all([
      this.refreshTokenModel.deleteMany({ userId }).exec(),
      reviews.deleteMany({ userId }),
      messages.deleteMany({ userId }),
      books.updateMany(
        {},
        {
          $pull: {
            bookmarks: { userId },
            reviews: { $in: reviewIds },
          },
        } as any,
      ),
      Types.ObjectId.isValid(userId)
        ? verifications.deleteMany({ userId: new Types.ObjectId(userId) })
        : Promise.resolve(),
    ]);

    for (const bookId of affectedBookIds) {
      const ratingStats = await reviews
        .aggregate([
          { $match: { bookId } },
          {
            $group: {
              _id: null,
              averageRating: { $avg: '$rating' },
              totalReviews: { $sum: 1 },
            },
          },
        ])
        .toArray();

      await books.updateOne(
        { id: bookId },
        {
          $set: {
            averageRating: ratingStats[0]?.averageRating ?? 0,
            totalReviews: ratingStats[0]?.totalReviews ?? 0,
          },
        },
      );
    }

    await this.userService.remove(userId);

    return {
      message: 'Account and associated ReadRealm data deleted successfully',
    };
  }

  async changePassword(userId, oldPassword: string, newPassword: string) {
    if (!oldPassword || !newPassword) {
      throw new BadRequestException(
        'Old password and new password are required',
      );
    }

    const user = await this.userService.findById(userId);

    if (!user) {
      throw new NotFoundException('User not found');
    }

    if (!user.password) {
      throw new BadRequestException('User does not have a password set');
    }

    const passwordMatch = await bcrypt.compare(oldPassword, user.password);
    if (!passwordMatch) {
      throw new UnauthorizedException('Old password is incorrect');
    }

    const newHashedPassword = await bcrypt.hash(newPassword, 10);
    user.password = newHashedPassword;

    await user.save();

    return { message: 'Password changed successfully' };
  }

  async forgotPassword(email: string, password: string) {
    const user = await this.userService.findByEmail(email);
    const newHashedPassword = await bcrypt.hash(password, 10);
    user.password = newHashedPassword;

    // Save the updated user with the new password
    await user.save();

    return { message: 'Password changed successfully' };
  }

  async generateEmailVerification(userId: string) {
    const user = await this.userService.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found');
    }

    if (user.emailVerifiedAt) {
      throw new UnprocessableEntityException('Account already verified');
    }

    const otp = await this.verificationService.generateOtp(+userId);

    await this.mailService.sendPasswordResetEmail(
      user.email,
      `<p>Hi${user.username ? ' ' + user.username : ''},</p><p>Your OTP is: <strong>${otp}</strong></p>`,
    );

    return { message: 'OTP sent successfully' };
  }

  async generateEmailVerificationByEmailAddress(email: string) {
    const user = await this.userService.findByEmail(email);
    if (!user) {
      throw new NotFoundException('User not found');
    }

    // if (user.emailVerifiedAt) {
    //   throw new UnprocessableEntityException('Account already verified');
    // }

    const otp = await this.verificationService.generateOtp(user.id);

    await this.mailService.sendPasswordResetEmail(user.email, otp);

    return { message: 'OTP sent successfully' };
  }

  async verifyEmail(email: string, otp: string) {
    const invalidMessage = 'Invalid or expired OTP';

    const user = await this.userService.findByEmail(email);
    if (!user) {
      throw new UnprocessableEntityException(invalidMessage);
    }

    // if (user.emailVerifiedAt) {
    //   throw new UnprocessableEntityException('Account already verified');
    // }

    const isValid = await this.verificationService.validateOtp(email, otp);

    if (!isValid) {
      throw new UnprocessableEntityException(invalidMessage);
    }

    user.emailVerifiedAt = new Date();

    await user.save();

    return { message: 'Email verified successfully' };
  }
}
