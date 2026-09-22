import {
  BadRequestException,
  Injectable,
  NotFoundException,
  ServiceUnavailableException,
} from '@nestjs/common';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom } from 'rxjs';
import { PassThrough } from 'stream';
import { CreateBookDto } from './dto/create-book.dto';
import { BookService } from './book.service';

@Injectable()
export class TTSService {
  private readonly elevenLabsBaseUrl = 'https://api.elevenlabs.io/v1';
  private readonly chunkSize = 4500;

  constructor(
    private readonly httpService: HttpService,
    private readonly configService: ConfigService,
    private readonly bookService: BookService,
  ) {}

  async getBookTTS(data: CreateBookDto) {
    if (!data.textData?.trim()) {
      throw new NotFoundException('No text data provided');
    }

    const apiKey = this.configService.get<string>('elevenlabs.apiKey');
    const voiceId = this.configService.get<string>('elevenlabs.voiceId');
    const modelId =
      this.configService.get<string>('elevenlabs.modelId') ??
      'eleven_multilingual_v2';

    if (!apiKey || !voiceId) {
      throw new ServiceUnavailableException(
        'ElevenLabs is not configured. Set ELEVENLABS_API_KEY and ELEVENLABS_VOICE_ID.',
      );
    }

    const cleanedText = data.textData
      .replace(/(\r\n|\n|\r)/gm, ' ')
      .replace(/\s+/g, ' ')
      .trim();

    if (!cleanedText) {
      throw new BadRequestException('Text is empty after normalization');
    }

    const chunks = this.chunkText(cleanedText);
    const output = new PassThrough();

    void this.streamChunks(chunks, output, apiKey, voiceId, modelId).catch(
      (error) => output.destroy(error),
    );

    return output;
  }

  async getBookTTSByTitle(title: string) {
    const decodedTitle = decodeURIComponent(title);
    const bookData = (await this.bookService.getBookByTitle(decodedTitle)) as any;

    if (!bookData || !Array.isArray(bookData) || bookData.length === 0) {
      throw new NotFoundException('Book not found');
    }

    const bookText = bookData[0].textData;
    if (!bookText) {
      throw new NotFoundException('No text content available for this book');
    }

    const createBookDto = new CreateBookDto();
    createBookDto.textData = bookText;

    return this.getBookTTS(createBookDto);
  }

  private chunkText(text: string): string[] {
    const chunks: string[] = [];
    let remaining = text;

    while (remaining.length > this.chunkSize) {
      let splitAt = remaining.lastIndexOf('. ', this.chunkSize);
      if (splitAt < this.chunkSize * 0.6) {
        splitAt = remaining.lastIndexOf(' ', this.chunkSize);
      }
      if (splitAt <= 0) {
        splitAt = this.chunkSize;
      }

      chunks.push(remaining.slice(0, splitAt + 1).trim());
      remaining = remaining.slice(splitAt + 1).trim();
    }

    if (remaining) {
      chunks.push(remaining);
    }

    return chunks;
  }

  private async streamChunks(
    chunks: string[],
    output: PassThrough,
    apiKey: string,
    voiceId: string,
    modelId: string,
  ) {
    for (const text of chunks) {
      const response = await firstValueFrom(
        this.httpService.post(
          `${this.elevenLabsBaseUrl}/text-to-speech/${voiceId}/stream`,
          {
            text,
            model_id: modelId,
          },
          {
            headers: {
              'Content-Type': 'application/json',
              'xi-api-key': apiKey,
            },
            params: {
              output_format: 'mp3_44100_128',
            },
            responseType: 'stream',
          },
        ),
      );

      await new Promise<void>((resolve, reject) => {
        response.data.once('error', reject);
        response.data.once('end', resolve);
        response.data.pipe(output, { end: false });
      });
    }

    output.end();
  }
}
