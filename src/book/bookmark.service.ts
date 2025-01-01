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

@Injectable()
export class BookmarkService {
    private readonly OPEN_LIBRARY_API_ENDPOINT = 'https://openlibrary.org';
    private readonly OPEN_LIBRARY_COVER_ENDPOINT = 'https://covers.openlibrary.org';
    private readonly GUTEDEX_API_ENDPOINT = 'https://gutendex.com';

    constructor(
        @InjectModel(Book.name) private bookModel: Model<BookDocument>,
    ) {}

    async getUserBookmarks(userId: string): Promise<BookDocument[]> {
        if (!userId) {
            throw new Error('User ID is required');
        }

        let books = await this.bookModel
            .find({
                'bookmarks.userId': userId,
            })
            .exec();
        books.forEach((book) => {
            book.reviews = [];
        });
        return books;
    }

    async toggleBookmark(bookId: number, userId: string) {
        const book = await this.bookModel.findOne({ id: bookId });
        if (!book) {
            throw new NotFoundException(`Book with ID ${bookId} not found`);
        }

        const bookmarkIndex = book.bookmarks.findIndex(
            (bookmark) => bookmark.userId === userId
        );

        if (bookmarkIndex === -1) {
            book.bookmarks.push({ userId, dateAdded: new Date() });
        } else {
            book.bookmarks.splice(bookmarkIndex, 1);
        }

        return await book.save();
    }
}
