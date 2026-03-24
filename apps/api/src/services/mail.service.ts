import * as nodemailer from 'nodemailer';
import { Injectable } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class MailService {
  private transporter: nodemailer.Transporter;

  constructor(private configService: ConfigService) {
    const user = configService.get<string>('mail.user');
    const pass = configService.get<string>('mail.pass');
    const host = configService.get<string>('mail.host');
    this.transporter = nodemailer.createTransport({
      host,
      port: 587,
      auth: {
        user,
        pass,
      },
    });
  }

  async sendPasswordResetEmail(to: string, otp: string) {
    const mailOptions = {
      from: 'Auth-backend service',
      to: to,
      subject: 'Password Reset Request',
      html: `<p>Your OTP is: <strong>${otp}</strong></p>`,
    };

    await this.transporter.sendMail(mailOptions);
  }
}
