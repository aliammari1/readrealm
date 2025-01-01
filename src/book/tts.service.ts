import 'openai/shims/node';
import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateBookDto } from './dto/create-book.dto';
import { UpdateBookDto } from './dto/update-book.dto';
import { HttpService } from '@nestjs/axios';
import { map, mergeMap, toArray } from 'rxjs/operators';
import { Book, BookDocument } from './entities/book.entity';
import { forkJoin, firstValueFrom, from } from 'rxjs';
import { GoogleGenerativeAI } from '@google/generative-ai';
import { AzureOpenAI } from 'openai';
import { ConfigService } from '@nestjs/config';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { Review } from './entities/review.entity';
import { CreateReviewDto } from './dto/create-review.dto';
import * as Sentiment from 'sentiment';
import { link } from 'fs';
import { BookService } from './book.service';

@Injectable()
export class TTSService {
    // 1. Constants and Configurations
    private readonly OPEN_LIBRARY_API_ENDPOINT = 'https://openlibrary.org';
    private readonly OPEN_LIBRARY_COVER_ENDPOINT = 'https://covers.openlibrary.org';
    private readonly GUTEDEX_API_ENDPOINT = 'https://gutendex.com';
    private azureTtsKey: string;
    private azureTtsEndpoint: string;
    private azureTtsModel: string;
    private geminiApiKey: string;
    private geminiModel: string;

    constructor(
        @InjectModel(Book.name) private bookModel: Model<BookDocument>,
        @InjectModel(Review.name) private reviewModel: Model<Review>,
        private readonly httpService: HttpService,
        private readonly configService: ConfigService,
        private readonly bookService: BookService,
    ) {
        this.azureTtsKey = configService.get<string>('azure.tts.key');
        this.azureTtsEndpoint = configService.get<string>('azure.tts.endpoint');
        this.azureTtsModel = configService.get<string>('azure.tts.model');
        this.geminiApiKey = configService.get<string>('gemini.key');
        this.geminiModel = configService.get<string>('gemini.model');
    }

    async getBookTTS(data: CreateBookDto) {
        if (!data.textData) {
            throw new NotFoundException('No text data provided');
        }

        // Clean up text
        data.textData = data.textData
            .replace(/(\r\n|\n|\r)/gm, ' ')
            .replace(/ +(?= )/g, '');

        const endpoint = this.azureTtsEndpoint || '';
        const apiKey = this.azureTtsKey || '';
        const deploymentName = this.azureTtsModel;
        const apiVersion = '2024-08-01-preview';

        const client = new AzureOpenAI({
            endpoint,
            apiKey,
            apiVersion,
            deployment: deploymentName,
        });

        const maxLength = 4096;
        const chunks = [];
        for (let i = 0; i < data.textData.length; i += maxLength) {
            chunks.push(data.textData.substring(i, i + maxLength));
        }

        // TODO: Implement handling of multiple chunks
        const response = await client.audio.speech.create({
            model: deploymentName,
            voice: 'alloy',
            input: chunks[0],
            response_format: 'mp3',
        });

        if (!response.ok) {
            throw new Error(
                `Failed to generate audio stream: ${response.statusText}`,
            );
        }

        return response.body;
    }

    async getBookTTSByTitle(title: string) {
        const decodedTitle = decodeURIComponent(title);
        console.log(`Getting TTS for book: ${decodedTitle}`);
        const bookData = (await this.bookService.getBookByTitle(title)) as any;

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
}