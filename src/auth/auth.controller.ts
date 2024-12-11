import {
  Controller,
  Get,
  Post,
  Body,
  Patch,
  Param,
  Delete,
  UseGuards,
  Put,
  Req,
} from '@nestjs/common';
import { AuthService } from './auth.service';
import { SignupDto } from './dto/signupDto';
import { AuthGaurd } from 'src/guards/authentification.guards';
import { ChangePasswordDto } from './dto/change-password.dto';
import { loginDto } from './dto/loginDto';
import { VerifyEmailDto } from './dto/verify-email.dto';

@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @Post('signup')
  // @UseGuards(RoleGaurd)
  // @Roles(Role.Admin)
  async register(@Body() credentials: SignupDto) {
    return this.authService.register(credentials);
  }

  @Post('login')
  async login(@Body() credentials: loginDto) {
    return this.authService.login(credentials);
  }

  // change password
  @UseGuards(AuthGaurd)
  @Put('change-password')
  async changePassword(
    @Body() changePasswordDto: ChangePasswordDto,
    @Req() req,
  ) {
    return this.authService.changePassword(
      req.userId,
      changePasswordDto.oldPassword,
      changePasswordDto.newPassword,
    );
  }

  @Post('generate-email-verification')
  async generateEmailVerification(@Body('userId') userId: string) {
    return this.authService.generateEmailVerification(userId);
  }

  @Post('generate-email')
  async generateEmailVerificationByEmailAddress(@Body('email') email: string) {
    return this.authService.generateEmailVerificationByEmailAddress(email);
  }

  @Post('verify-email')
  async verifyEmailAddress(
    @Body('email') email: string,
    @Body('otp') otp: string,
  ) {
    return this.authService.verifyEmail(email, otp);
  }

  @Post('forgot-password')
  async forgotPassword(
    @Body('email') email: string,
    @Body('password') password: string,
  ) {
    return this.authService.forgotPassword(email, password);
  }
}
