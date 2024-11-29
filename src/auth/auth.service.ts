import {
  Injectable,
  BadRequestException,
  UnauthorizedException,
  NotFoundException,
  UnprocessableEntityException,
} from '@nestjs/common';
import { UserService } from 'src/user/user.service'; // Import UserService
import { v4 as uuidv4 } from 'uuid';
import * as bcrypt from 'bcryptjs';
import { SignupDto } from './dto/signUpDto';
import { loginDto } from './dto/loginDto';
import { JwtService } from '@nestjs/jwt'; // Proper import of JwtService
import { InjectModel } from '@nestjs/mongoose'; // Mongoose injection
import { Model } from 'mongoose';
import { RefreshToken } from './dto/refresh-token.schema'; // Import your RefreshToken schema
import { MailService } from 'src/services/mail.service';
import { VerificationService } from 'src/verification/verification.service';

@Injectable()
export class AuthService {
  constructor(
    private readonly mailService: MailService,
    private readonly userService: UserService,
    private readonly jwtService: JwtService, // Inject JwtService properly
    private readonly verificationService: VerificationService,
    @InjectModel('RefreshToken')
    private readonly refreshTokenModel: Model<RefreshToken>, // Inject Mongoose model
  ) {}

  async signup(signupData: SignupDto) {
    const { email, password, username } = signupData;

    const emailInUse = await this.userService.findByEmail(email);
    if (emailInUse) {
      throw new BadRequestException('Email already in use');
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    const newUser = await this.userService.create({
      username,
      email,
      password: hashedPassword,
    });

    return {
      message: 'User registered successfully',
      user: { email: newUser.email },
    };
  }

  async login(credentiales: loginDto) {
    const { email, password } = credentiales;

    const user = await this.userService.findByEmail(email);
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
