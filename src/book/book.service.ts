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
import { EpubService } from './epub.service';
import { ReviewService } from './review.service';
import { BookmarkService } from './bookmark.service';

@Injectable()
export class BookService {
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
    private readonly epubService: EpubService,
    private readonly reviewService: ReviewService,
    private readonly bookmarkService: BookmarkService,
  ) {
    this.azureTtsKey = configService.get<string>('azure.tts.key');
    this.azureTtsEndpoint = configService.get<string>('azure.tts.endpoint');
    this.azureTtsModel = configService.get<string>('azure.tts.model');
    this.geminiApiKey = configService.get<string>('gemini.key');
    this.geminiModel = configService.get<string>('gemini.model');
  }

  // 2. CRUD Operations
  async create(createBookDto: CreateBookDto): Promise<Book> {
    const createdBook = new this.bookModel(createBookDto);
    return await createdBook.save();
  }

  async findAll(): Promise<Book[]> {
    return await this.bookModel.find().exec();
  }

  async findOne(id: number): Promise<Book> {
    const book = await this.bookModel.findOne({ id }).exec();
    if (!book) {
      throw new NotFoundException(`Book with ID ${id} not found`);
    }
    return book;
  }

  async update(id: number, updateBookDto: UpdateBookDto): Promise<Book> {
    const updatedBook = await this.bookModel
      .findOneAndUpdate({ id }, updateBookDto, { new: true })
      .exec();
    if (!updatedBook) {
      throw new NotFoundException(`Book with ID ${id} not found`);
    }
    return updatedBook;
  }

  async remove(id: number): Promise<{ deleted: boolean }> {
    const result = await this.bookModel.deleteOne({ id }).exec();
    if (result.deletedCount === 0) {
      throw new NotFoundException(`Book with ID ${id} not found`);
    }
    return { deleted: true };
  }

  // 3. Book Search and Details Methods
  searchBooks(title: string) {
    let titleEncoded = encodeURIComponent(title);
    let searchUrl = `${this.OPEN_LIBRARY_API_ENDPOINT}/search.json?q=${titleEncoded}&fields=*,availability&limit=12&lang=en`;
    let books = this.httpService.get(searchUrl).pipe(
      map((searchResponse) => {
        return searchResponse.data.docs.map((searchResult) => {
          const book = {
            title: searchResult.title,
            author: searchResult.author_name[0],
            publicationDate: searchResult.first_publish_year,
            numOfPages: searchResult.number_of_pages_median,
            coverImage: `${this.OPEN_LIBRARY_COVER_ENDPOINT}/b/id/${searchResult.cover_i}-M.jpg`,
          };
          return book;
        });
      }),
    );

    return books;
  }

  async getBookDetails(id: number): Promise<Book> {
    // First try to get from database
    try {
      const book = await this.bookModel.findOne({ id }).exec();
      if (book) {
        return book;
      }
    } catch (error) {
      console.error('Error fetching from database:', error);
    }

    // If not in database, fetch from OpenLibrary API
    try {
      const response = await firstValueFrom(
        this.httpService.get(`${this.OPEN_LIBRARY_API_ENDPOINT}/works/OL${id}W.json`)
      );

      const bookDetails = response.data;
      let epubLink = '';
      try {
        epubLink = await this.getBookEpubLinkByTitle(bookDetails.title);
      } catch (error) {
        console.log('Failed to fetch epub link:', error);
      }

      // Get book summary
      let description = '';
      try {
        description = await this.getBookSummary(bookDetails.title);
      } catch (error) {
        console.log('Failed to fetch book summary:', error);
        description = bookDetails.description?.value || bookDetails.description || '';
      }

      const book: Book = {
        id: Number(bookDetails.key.split('/')[2].replace('OL', '').replace('W', '')),
        title: bookDetails.title,
        author: bookDetails.authors?.[0]?.name || 'Unknown',
        publicationYear: bookDetails.first_publish_year || 1970,
        numOfPages: bookDetails.number_of_pages_median || 0,
        coverImage: bookDetails.covers
          ? `${this.OPEN_LIBRARY_COVER_ENDPOINT}/b/id/${bookDetails.covers[0]}-M.jpg`
          : null,
        genre: bookDetails.subjects
          ? bookDetails.subjects[0]
          : 'Unknown',
        textData: '',
        link: epubLink || '', // Ensure it's never undefined
        bookmarks: [],
        reviews: await this.reviewService.getBookReviews(id),
        averageRating: 0,
        totalReviews: 0,
        description: description, // Add this line
      };

      // Save to database
      try {
        const createdBook = new this.bookModel({
          ...book,
          link: book.link || '', // Ensure link is never undefined when saving
        });
        await createdBook.save();
      } catch (error) {
        console.error('Error saving to database:', error);
      }

      return book;
    } catch (error) {
      throw new NotFoundException(`Book with ID ${id} not found in external API ${error.message}`);
    }
  }

  async *findBooksByGenre(genre: string, offset = 0, limit = 10): AsyncGenerator<Book & { total: number, offset: number, limit: number }> {
    if (genre.toLowerCase() === 'all') {
      return;
    }

    try {
      const response = await this.httpService
        .get(
          `${this.OPEN_LIBRARY_API_ENDPOINT}/subjects/${genre}.json?limit=${limit}&offset=${offset}&details=true`,
        )
        .toPromise();

      const works = response.data.works;
      for (const work of works) {
        yield {
          id: Number(work.key.split('/')[2].replace('OL', '').replace('W', '')),
          title: work.title,
          author: work.authors?.[0]?.name || 'Unknown',
          publicationYear: work.first_publish_year,
          numOfPages: work.number_of_pages_median || 0,
          coverImage: work.cover_id
            ? `${this.OPEN_LIBRARY_COVER_ENDPOINT}/b/id/${work.cover_id}-M.jpg`
            : null,
          genre: genre.charAt(0).toUpperCase() + genre.slice(1),
          textData: '',
          link: await this.getBookEpubLinkByTitle(work.title),
          bookmarks: [],
          reviews: [],
          averageRating: 0,
          totalReviews: 0,
          total: response.data.work_count || 0, // Add total count
          description: work.description?.value || work.description || '',
          offset: offset,
          limit: limit
        };
      }
    } catch (error) {
      throw new Error(`Failed to fetch books for genre ${genre}: ${error.message}`);
    }
  }

  async getBookByTitle(title: string) {
    const encodedTitle = encodeURIComponent(title);
    const url = `${this.GUTEDEX_API_ENDPOINT}/books?search=${encodedTitle}`;

    try {
      const response = await firstValueFrom(this.httpService.get(url));
      const books = response.data.results;

      if (!books || books.length === 0) {
        throw new NotFoundException(`No books found with title: ${title}`);
      }

      const bookData = books[0];
      const book = {
        id: bookData.id,
        title: bookData.title,
        author:
          bookData.authors.length > 0 ? bookData.authors[0].name : 'Unknown',
        publicationDate: bookData.download_count,
        coverImage: bookData.formats['image/jpeg'],
        genre: bookData.subjects.length > 0 ? bookData.subjects[0] : 'Unknown',
        textData: '',
        link: bookData.formats['application/epub+zip'] || '', // Ensure it's never undefined
      };

      const textUrl =
        bookData.formats['text/plain; charset=utf-8'] ||
        bookData.formats['text/plain; charset=us-ascii'] ||
        bookData.formats['text/plain'];

      if (!textUrl) {
        book.textData = '';
        return [book];
      }

      try {
        const textResponse = await firstValueFrom(
          this.httpService.get(textUrl, { responseType: 'text' }),
        );
        console.log(`Fetched text data for book: ${book.title}`);
        book.textData = textResponse.data;
        return [book];
      } catch (error) {
        console.error(`Error fetching text for book ${book.title}:`, error);
        book.textData = '';
        return [book];
      }
    } catch (error) {
      throw new NotFoundException(`Error fetching books: ${error.message}`);
    }
  }

  async getBookEpubLinkByTitle(title: string) {
    return this.epubService.getBookEpubLinkByTitle(title);
  }

  async getBookSummary(title: string) {
    // data = new CreateBookDto();
    if (title == '') {
      return 'No text data provided';
    }
    let book = await this.getBookByTitle(title);
    if (!book || book.length == 0) {
      return 'Book not found';
    }
    let textData = book[0].textData;
    textData = textData.replace(/(\r\n|\n|\r)/gm, ' ');
    textData = textData.replace(/ +(?= )/g, '');
    const genAI = new GoogleGenerativeAI(this.geminiApiKey);
    const model = genAI.getGenerativeModel({ model: this.geminiModel });
    const prompt = 'summarize this book in 5 lines: ' + textData;

    const result = await model.generateContent(prompt);
    const response = result.response.text();
    console.log(response);
    return response;
  }
}
