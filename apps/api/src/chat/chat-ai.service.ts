import Anthropic from '@anthropic-ai/sdk';
import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { BookService } from '../book/book.service';

/**
 * AI book-chat participant.
 *
 * This is the Anthropic provider in ReadRealm's unified multi-provider AI layer
 * (Google / OpenAI / HuggingFace / Azure already power summaries, TTS and
 * speech). It joins a book chat room as a participant: when a reader @-mentions
 * the assistant, it answers with streaming Claude (`claude-haiku-4-5` by
 * default) and can call two tools:
 *
 *   - summarize_chapter  -> a concise summary of a book / chapter
 *   - explain_passage    -> plain-language explanation of a quoted passage
 *
 * Streaming is surfaced to the gateway via an async callback so Socket.IO can
 * emit token deltas to the room in real time.
 */
export interface BookChatContext {
  bookId: number;
  bookTitle?: string;
}

const TOOLS: Anthropic.Tool[] = [
  {
    name: 'summarize_chapter',
    description:
      'Summarize a book or one of its chapters. Call this when the reader ' +
      'asks for a summary, recap, or "what is this book/chapter about". ' +
      'Pass the book title; pass the chapter only if the reader named one.',
    input_schema: {
      type: 'object',
      properties: {
        title: { type: 'string', description: 'The book title' },
        chapter: {
          type: 'string',
          description: 'Optional chapter name or number',
        },
      },
      required: ['title'],
    },
  },
  {
    name: 'explain_passage',
    description:
      'Explain a specific quoted passage in plain language. Call this when ' +
      'the reader quotes text and asks what it means, what is happening, or ' +
      'for an interpretation.',
    input_schema: {
      type: 'object',
      properties: {
        passage: { type: 'string', description: 'The passage to explain' },
        question: {
          type: 'string',
          description: "The reader's specific question, if any",
        },
      },
      required: ['passage'],
    },
  },
];

const SYSTEM_PROMPT =
  "You are ReadRealm's in-chat book companion, talking with readers inside a " +
  "book's discussion room. Be warm, concise, and spoiler-aware: if you might " +
  'reveal a major plot twist, warn first. Use the summarize_chapter tool for ' +
  'summaries and the explain_passage tool to interpret quoted text. When the ' +
  'answer is conversational, just reply directly. Keep replies short enough to ' +
  'read on a phone.';

@Injectable()
export class ChatAiService {
  private readonly logger = new Logger(ChatAiService.name);
  private readonly client: Anthropic | null;
  private readonly model: string;

  constructor(
    private readonly configService: ConfigService,
    private readonly bookService: BookService,
  ) {
    const apiKey = this.configService.get<string>('anthropic.key');
    this.model =
      this.configService.get<string>('anthropic.model') ?? 'claude-haiku-4-5';
    this.client = apiKey ? new Anthropic({ apiKey }) : null;
  }

  /** Whether the AI participant is configured (ANTHROPIC_API_KEY present). */
  isEnabled(): boolean {
    return this.client !== null;
  }

  /**
   * Stream an AI reply for one reader turn. `history` is the prior room
   * messages (oldest first). `onDelta` receives text chunks as they stream.
   * Returns the full assistant text.
   */
  async streamReply(
    userMessage: string,
    history: { username: string; content: string; isAi?: boolean }[],
    context: BookChatContext,
    onDelta: (text: string) => void,
  ): Promise<string> {
    if (!this.client) {
      const msg =
        'The AI book companion is not configured on this server ' +
        '(missing ANTHROPIC_API_KEY).';
      onDelta(msg);
      return msg;
    }

    const messages: Anthropic.MessageParam[] = [
      ...history.slice(-12).map<Anthropic.MessageParam>((m) => ({
        role: m.isAi ? 'assistant' : 'user',
        content: m.isAi ? m.content : `${m.username}: ${m.content}`,
      })),
      { role: 'user', content: userMessage },
    ];

    const system = context.bookTitle
      ? `${SYSTEM_PROMPT}\nThe current room is for the book "${context.bookTitle}".`
      : SYSTEM_PROMPT;

    let full = '';

    // Manual tool-use loop so we can stream deltas and run tools client-side.
    // Bounded to avoid runaway loops.
    for (let turn = 0; turn < 4; turn++) {
      const toolUses: Anthropic.ToolUseBlock[] = [];

      const stream = this.client.messages.stream({
        model: this.model,
        max_tokens: 1024,
        system,
        tools: TOOLS,
        messages,
      });

      stream.on('text', (delta) => {
        full += delta;
        onDelta(delta);
      });

      let message: Anthropic.Message;
      try {
        message = await stream.finalMessage();
      } catch (err) {
        this.logger.error('Anthropic stream failed', err as Error);
        const fallback =
          ' …sorry, I had trouble reaching the AI service just now.';
        onDelta(fallback);
        return full + fallback;
      }

      for (const block of message.content) {
        if (block.type === 'tool_use') {
          toolUses.push(block);
        }
      }

      if (message.stop_reason !== 'tool_use' || toolUses.length === 0) {
        break;
      }

      // Execute tools and feed results back.
      messages.push({ role: 'assistant', content: message.content });
      const toolResults: Anthropic.ToolResultBlockParam[] = [];
      for (const tu of toolUses) {
        const result = await this.runTool(tu.name, tu.input as object);
        toolResults.push({
          type: 'tool_result',
          tool_use_id: tu.id,
          content: result,
        });
      }
      messages.push({ role: 'user', content: toolResults });
    }

    return full;
  }

  private async runTool(name: string, input: object): Promise<string> {
    try {
      if (name === 'summarize_chapter') {
        const { title, chapter } = input as {
          title: string;
          chapter?: string;
        };
        const summary = await this.bookService.getBookSummary(title);
        return chapter
          ? `Summary of "${title}" (focus: ${chapter}):\n${summary}`
          : `Summary of "${title}":\n${summary}`;
      }
      if (name === 'explain_passage') {
        // The passage is the authoritative source; hand it back so the model
        // explains it directly rather than fetching external context.
        const { passage } = input as { passage: string; question?: string };
        return `Passage to explain (interpret this directly):\n"""${passage}"""`;
      }
      return `Unknown tool: ${name}`;
    } catch (err) {
      this.logger.error(`Tool ${name} failed`, err as Error);
      return `The "${name}" tool could not complete: ${(err as Error).message}`;
    }
  }
}
