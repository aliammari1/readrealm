import { Test, TestingModule } from '@nestjs/testing';
import { ChatGateway } from './chat.gateway';
import { ChatService } from './chat.service';
import { ChatAiService } from './chat-ai.service';

describe('ChatGateway', () => {
  let gateway: ChatGateway;
  let chatService: jest.Mocked<
    Pick<ChatService, 'saveMessage' | 'getRoomMessages' | 'getRoomHistory'>
  >;
  let aiService: jest.Mocked<Pick<ChatAiService, 'isEnabled' | 'streamReply'>>;
  let emit: jest.Mock;
  let to: jest.Mock;

  beforeEach(async () => {
    chatService = {
      saveMessage: jest.fn().mockImplementation((m) => Promise.resolve(m)),
      getRoomMessages: jest.fn().mockResolvedValue([]),
      getRoomHistory: jest.fn().mockResolvedValue([]),
    };
    aiService = {
      isEnabled: jest.fn().mockReturnValue(true),
      streamReply: jest.fn().mockResolvedValue('AI answer'),
    };

    const module: TestingModule = await Test.createTestingModule({
      providers: [
        ChatGateway,
        { provide: ChatService, useValue: chatService },
        { provide: ChatAiService, useValue: aiService },
      ],
    }).compile();

    gateway = module.get<ChatGateway>(ChatGateway);

    emit = jest.fn();
    to = jest.fn().mockReturnValue({ emit });
    // @ts-expect-error — partial Socket.IO Server mock for tests
    gateway.server = { to };
  });

  it('should be defined', () => {
    expect(gateway).toBeDefined();
  });

  it('broadcasts a plain message without invoking the AI', async () => {
    await gateway.handleMessage({} as never, {
      bookId: 1,
      userId: 'u1',
      username: 'Jane',
      content: 'Loved this chapter!',
    });

    expect(chatService.saveMessage).toHaveBeenCalledTimes(1);
    expect(aiService.streamReply).not.toHaveBeenCalled();
    expect(emit).toHaveBeenCalledWith('newMessage', expect.anything());
  });

  it('summons the AI participant when mentioned', async () => {
    await gateway.handleMessage({} as never, {
      bookId: 1,
      userId: 'u1',
      username: 'Jane',
      content: '@ai can you summarize chapter 2?',
    });

    expect(aiService.streamReply).toHaveBeenCalledTimes(1);
    // User message + AI message both persisted.
    expect(chatService.saveMessage).toHaveBeenCalledTimes(2);
    expect(emit).toHaveBeenCalledWith('aiDone', { username: 'ReadRealm AI' });
  });
});
