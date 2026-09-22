import { HttpService } from '@nestjs/axios';
import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom } from 'rxjs';

export interface BookChatContext {
  bookId: number;
  bookTitle?: string;
}

const SYSTEM_PROMPT =
  "You are ReadRealm's in-chat book companion. Be concise, helpful, and spoiler-aware. " +
  'Never invent plot details. If the reader asks about events beyond their stated progress, warn before revealing them.';

@Injectable()
export class ChatAiService {
  private readonly logger = new Logger(ChatAiService.name);

  constructor(
    private readonly configService: ConfigService,
    private readonly httpService: HttpService,
  ) {}

  isEnabled(): boolean {
    return Boolean(this.configService.get<string>('ollama.url'));
  }

  async streamReply(
    userMessage: string,
    history: { username: string; content: string; isAi?: boolean }[],
    context: BookChatContext,
    onDelta: (text: string) => void,
  ): Promise<string> {
    const ollamaUrl =
      this.configService.get<string>('ollama.url') ?? 'http://localhost:11434';
    const model =
      this.configService.get<string>('ollama.model') ?? 'qwen3:8b';

    const transcript = history
      .slice(-12)
      .map((message) =>
        message.isAi
          ? `ReadRealm: ${message.content}`
          : `${message.username}: ${message.content}`,
      )
      .join('\n');

    const prompt = [
      SYSTEM_PROMPT,
      context.bookTitle
        ? `Current book room: "${context.bookTitle}".`
        : 'Current book title is unknown.',
      transcript ? `Recent conversation:\n${transcript}` : '',
      `Reader: ${userMessage}`,
      'ReadRealm:',
    ]
      .filter(Boolean)
      .join('\n\n');

    try {
      const response = await firstValueFrom(
        this.httpService.post(
          `${ollamaUrl.replace(/\/$/, '')}/api/generate`,
          {
            model,
            prompt,
            stream: false,
          },
          { timeout: 120000 },
        ),
      );

      const text =
        response.data?.response?.trim() ||
        'I could not generate a response just now.';
      onDelta(text);
      return text;
    } catch (error) {
      this.logger.error('Ollama book-chat request failed', error as Error);
      const fallback =
        'The local AI book companion is unavailable right now. Please try again later.';
      onDelta(fallback);
      return fallback;
    }
  }
}
