import { AuthService } from './auth.service';

describe('AuthService', () => {
  it('should be defined with mocked collaborators', () => {
    const service = new AuthService(
      {} as any,
      {} as any,
      {} as any,
      {} as any,
      {} as any,
    );

    expect(service).toBeDefined();
  });
});
