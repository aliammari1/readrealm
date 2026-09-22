import { Test, TestingModule } from '@nestjs/testing';
import { JwtService } from '@nestjs/jwt';
import { AuthController } from './auth.controller';
import { AuthService } from './auth.service';
import { AuthGaurd } from '../guards/authentification.guards';

describe('AuthController', () => {
  let controller: AuthController;
  const authService = {
    register: jest.fn().mockResolvedValue({ id: 'u1' }),
    login: jest.fn().mockResolvedValue({ accessToken: 'jwt' }),
    changePassword: jest.fn(),
    generateEmailVerification: jest.fn(),
    generateEmailVerificationByEmailAddress: jest.fn(),
    verifyEmail: jest.fn(),
    forgotPassword: jest.fn(),
  };

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [AuthController],
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: JwtService, useValue: { verify: jest.fn() } },
      ],
    })
      .overrideGuard(AuthGaurd)
      .useValue({ canActivate: () => true })
      .compile();

    controller = module.get<AuthController>(AuthController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });

  it('login delegates to AuthService', async () => {
    await controller.login({ email: 'a@b.com', password: 'secret1' });
    expect(authService.login).toHaveBeenCalledWith({
      email: 'a@b.com',
      password: 'secret1',
    });
  });
});
