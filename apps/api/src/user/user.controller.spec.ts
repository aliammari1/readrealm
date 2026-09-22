import { UserController } from './user.controller';

describe('UserController', () => {
  it('should be defined with a mocked user service', () => {
    const controller = new UserController({} as any);
    expect(controller).toBeDefined();
  });
});
