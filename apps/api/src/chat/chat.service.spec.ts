import { ChatService } from './chat.service';

describe('ChatService', () => {
  it('should be defined with a mocked message model', () => {
    const service = new ChatService({} as any);
    expect(service).toBeDefined();
  });
});
