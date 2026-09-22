import { UserService } from './user.service';

describe('UserService', () => {
  it('should be defined with a mocked user model', () => {
    const service = new UserService({} as any);
    expect(service).toBeDefined();
  });
});
