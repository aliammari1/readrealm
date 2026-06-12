import { Controller, Post, Body, UseGuards, Put, Req } from '@nestjs/common';
import { ApiBearerAuth, ApiBody, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Throttle } from '@nestjs/throttler';
import { AuthService } from './auth.service';
import { SignupDto } from './dto/signUpDto';
import { AuthGaurd } from '../guards/authentification.guards';
import { ChangePasswordDto } from './dto/change-password.dto';
import { loginDto } from './dto/loginDto';
import { VerifyEmailDto } from './dto/verify-email.dto';

// Tight rate limit on the whole auth surface: 10 requests / minute / IP.
// Curbs credential stuffing, OTP brute-force and email-bombing of the
// verification/forgot-password endpoints.
@Throttle({ default: { ttl: 60_000, limit: 10 } })
@ApiTags('auth')
@Controller('auth')
export class AuthController {
  constructor(private readonly authService: AuthService) {}

  @ApiOperation({ summary: 'Register a new reader account' })
  @Post('register')
  // @UseGuards(RoleGaurd)
  // @Roles(Role.Admin)
  async register(@Body() credentials: SignupDto) {
    return this.authService.register(credentials);
  }

  @ApiOperation({ summary: 'Log in and receive a JWT access token' })
  @Post('login')
  async login(@Body() credentials: loginDto) {
    return this.authService.login(credentials);
  }

  @ApiOperation({ summary: 'Change the authenticated user password' })
  @ApiBearerAuth('access-token')
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

  @ApiOperation({ summary: 'Send an email-verification OTP for a user id' })
  @ApiBody({ schema: { properties: { userId: { type: 'string' } } } })
  @Post('generate-email-verification')
  async generateEmailVerification(@Body('userId') userId: string) {
    return this.authService.generateEmailVerification(userId);
  }

  @ApiOperation({ summary: 'Send an email-verification OTP for an email' })
  @ApiBody({ schema: { properties: { email: { type: 'string' } } } })
  @Post('generate-email')
  async generateEmailVerificationByEmailAddress(@Body('email') email: string) {
    return this.authService.generateEmailVerificationByEmailAddress(email);
  }

  @ApiOperation({ summary: 'Verify an email address with an OTP' })
  @ApiBody({ type: VerifyEmailDto })
  @Post('verify-email')
  async verifyEmailAddress(
    @Body('email') email: string,
    @Body('otp') otp: string,
  ) {
    return this.authService.verifyEmail(email, otp);
  }

  @ApiOperation({ summary: 'Reset a password after email verification' })
  @ApiBody({
    schema: {
      properties: { email: { type: 'string' }, password: { type: 'string' } },
    },
  })
  @Post('forgot-password')
  async forgotPassword(
    @Body('email') email: string,
    @Body('password') password: string,
  ) {
    return this.authService.forgotPassword(email, password);
  }
}
