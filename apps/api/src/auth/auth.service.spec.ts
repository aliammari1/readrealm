import { Test, TestingModule } from '@nestjs/testing';
import { getModelToken } from '@nestjs/mongoose';
import { JwtService } from '@nestjs/jwt';
import { AuthService } from './auth.service';
import { UserService } from '../user/user.service';
import { MailService } from '../services/mail.service';
import { VerificationService } from '../verification/verification.service';

describe('AuthService', () => {
  let service: AuthService;
  const userService = { findByEmail: jest.fn().mockResolvedValue(null) };

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        AuthService,
        { provide: MailService, useValue: {} },
        { provide: UserService, useValue: userService },
        { provide: JwtService, useValue: { sign: jest.fn() } },
        { provide: VerificationService, useValue: {} },
        { provide: getModelToken('RefreshToken'), useValue: {} },
      ],
    }).compile();

    service = module.get<AuthService>(AuthService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });
});
