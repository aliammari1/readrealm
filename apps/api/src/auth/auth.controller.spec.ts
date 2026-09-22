import { AuthController } from './auth.controller';

describe('AuthController', () => {
  it('should be defined with a mocked auth service', () => {
    const controller = new AuthController({} as any);
    expect(controller).toBeDefined();
  });
});
