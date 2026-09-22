import { ChatGateway } from './chat.gateway';

describe('ChatGateway', () => {
  it('should be defined with a mocked chat service', () => {
    const gateway = new ChatGateway({} as any);
    expect(gateway).toBeDefined();
  });
});
