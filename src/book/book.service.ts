import 'openai/shims/node';
import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateBookDto } from './dto/create-book.dto';
import { UpdateBookDto } from './dto/update-book.dto';
import { HttpService } from '@nestjs/axios';
import { map, mergeMap } from 'rxjs/operators';
import { Book, BookDocument } from './entities/book.entity';
import { forkJoin, firstValueFrom } from 'rxjs';
import { GoogleGenerativeAI } from '@google/generative-ai';
import { AzureOpenAI } from 'openai';
import { ConfigService } from '@nestjs/config';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { Review } from './entities/review.entity';
import { CreateReviewDto } from './dto/create-review.dto';
import * as Sentiment from 'sentiment';

@Injectable()
export class BookService {
  private readonly OPEN_LIBRARY_API_ENDPOINT = 'https://openlibrary.org';
  private readonly OPEN_LIBRARY_COVER_ENDPOINT =
    'https://covers.openlibrary.org';
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
  ) {
    this.azureTtsKey = configService.get<string>('azure.tts.key');
    this.azureTtsEndpoint = configService.get<string>('azure.tts.endpoint');
    this.azureTtsModel = configService.get<string>('azure.tts.model');
    this.geminiApiKey = configService.get<string>('gemini.key');
    this.geminiModel = configService.get<string>('gemini.model');
  }

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
    const updatedBook = await this.bookModel.findOneAndUpdate(
      { id },
      updateBookDto,
      { new: true },
    ).exec();
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

  searchBooks(title: string) {
    let titleEncoded = encodeURIComponent(title);
    let searchUrl = `${this.OPEN_LIBRARY_API_ENDPOINT}/search.json?q=${titleEncoded}&fields=*,availability&limit=10&lang=en`;
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

  getBookDetails(id: number) {
    let book = this.httpService
      .get(`${this.OPEN_LIBRARY_API_ENDPOINT}/works/OL${id}W.json`)
      .pipe(
        map((response) => {
          let bookDetails = response.data;
          const book = {
            id: Number(bookDetails.key.split('/')[2].replace('OL', '').replace('W', '')),
            title: bookDetails.title,
            author: bookDetails.author_name?.[0],
            publicationDate: bookDetails.first_publish_year,
            numOfPages: bookDetails.number_of_pages_median,
            coverImage: `${this.OPEN_LIBRARY_COVER_ENDPOINT}/b/id/${bookDetails.cover_id}-M.jpg`,
            genre: bookDetails.subjects ? bookDetails.subjects.join(', ') : 'Unknown',
          };
          return book;
        }),
      );
    return book;
  }

  private genreCache = new Map<string, any>();
  private readonly CACHE_DURATION = 5 * 60 * 1000; // 5 minutes in milliseconds

  findBooksByGenre(genre: string) {
    if (genre.toLowerCase() === 'all') {
      return this.findAllGenres();
    }

    // Check cache first
    const cachedData = this.genreCache.get(genre);
    if (cachedData && (Date.now() - cachedData.timestamp < this.CACHE_DURATION)) {
      return cachedData.books;
    }

    // If not in cache or expired, fetch from API
    const books = this.httpService
      .get(`${this.OPEN_LIBRARY_API_ENDPOINT}/subjects/${genre}.json?limit=10&details=true`)
      .pipe(
        map((response) => {
          const works = response.data.works;
          return works.map((work) => ({
            id: Number(work.key.split('/')[2].replace('OL', '').replace('W', '')),
            title: work.title,
            author: work.authors?.[0]?.name || 'Unknown',
            publicationDate: work.first_publish_year,
            coverImage: work.cover_id ?
              `${this.OPEN_LIBRARY_COVER_ENDPOINT}/b/id/${work.cover_id}-M.jpg` : null,
            genre: genre.charAt(0).toUpperCase() + genre.slice(1),
          }));
        }),
        map((books) => {
          // Update cache
          this.genreCache.set(genre, {
            books,
            timestamp: Date.now()
          });
          return books;
        })
      );
    return books;
  }

  findAllGenres() {
    const actionBooks = this.findBooksByGenre('action');
    const adventureBooks = this.findBooksByGenre('adventure');
    const fantasyBooks = this.findBooksByGenre('fantasy');

    return forkJoin({
      actionBooks,
      adventureBooks,
      fantasyBooks,
    }).pipe(
      map((booksObj) => [
        ...(booksObj.actionBooks as any),
        ...(booksObj.adventureBooks as any),
        ...(booksObj.fantasyBooks as any),
      ]),
    );
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
        author: bookData.authors.length > 0 ? bookData.authors[0].name : 'Unknown',
        publicationDate: bookData.download_count,
        coverImage: bookData.formats['image/jpeg'],
        genre: bookData.subjects.length > 0 ? bookData.subjects[0] : 'Unknown',
        textData: '',
      };

      const textUrl = bookData.formats['text/plain; charset=utf-8'] ||
        bookData.formats['text/plain; charset=us-ascii'] ||
        bookData.formats['text/plain'];

      if (!textUrl) {
        book.textData = '';
        return [book];
      }

      try {
        const textResponse = await firstValueFrom(
          this.httpService.get(textUrl, { responseType: 'text' })
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

  async getBookSummary(title: string) {
    // data = new CreateBookDto();
    if (title == "") {
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
    const prompt = 'summarize this book in 100 lines: ' + textData;

    const result = await model.generateContent(prompt);
    const response = result.response.text();
    console.log(response);
    return response;
  }

  async getBookTTS(data: CreateBookDto) {
    if (!data.textData) {
      throw new NotFoundException('No text data provided');
    }

    // Clean up text
    data.textData = data.textData.replace(/(\r\n|\n|\r)/gm, ' ').replace(/ +(?= )/g, '');

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
      response_format: 'mp3'
    });

    if (!response.ok) {
      throw new Error(`Failed to generate audio stream: ${response.statusText}`);
    }

    return response.body;
  }

  async getBookTTSByTitle(title: string) {
    const decodedTitle = decodeURIComponent(title);
    console.log(`Getting TTS for book: ${decodedTitle}`);
    const bookData = await this.getBookByTitle(title) as any;

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

  private async ensureBookExists(bookId: number): Promise<BookDocument> {
    let book = await this.bookModel.findOne({ id: bookId }).exec();

    if (!book) {
      const bookDetailsObs = await this.getBookDetails(bookId);
      const bookDetails = await firstValueFrom(bookDetailsObs);
      if (!bookDetails) {
        throw new NotFoundException(`Book with ID ${bookId} not found in external API`);
      }

      book = await this.bookModel.create({
        id: bookId,
        author: bookDetails.author,
        title: bookDetails.title,
        publicationDate: bookDetails.publicationDate,
        numOfPages: bookDetails.numOfPages,
        coverImage: bookDetails.coverImage,
        genre: bookDetails.genre,
        textData: '',
        bookmarks: []
      });
    }

    return book;
  }

  async toggleBookmark(bookId: number, userId: string): Promise<BookDocument> {
    if (!userId) {
      throw new Error('User ID is required');
    }

    const book = await this.ensureBookExists(bookId);
    const hasBookmark = book.bookmarks.some(b => b.userId === userId);

    if (hasBookmark) {
      // Remove bookmark
      return await this.bookModel.findOneAndUpdate(
        { id: bookId },
        { $pull: { bookmarks: { userId } } },
        { new: true }
      );
    } else {
      // Add bookmark
      return await this.bookModel.findOneAndUpdate(
        { id: bookId },
        { $push: { bookmarks: { userId, dateAdded: new Date() } } },
        { new: true }
      );
    }
  }

  async getUserBookmarks(userId: string): Promise<BookDocument[]> {
    if (!userId) {
      throw new Error('User ID is required');
    }

    return await this.bookModel.find({
      'bookmarks.userId': userId
    }).exec();
  }

  async createReview(createReviewDto: CreateReviewDto) {
    try {
      let book = await this.bookModel.findOne({ id: createReviewDto.bookId });

      if (!book) {
        // Fetch book data from Open Library API using works endpoint
        const bookData = await firstValueFrom(
          this.httpService
            .get(`${this.OPEN_LIBRARY_API_ENDPOINT}/works/OL${createReviewDto.bookId}W.json`)
            .pipe(map((response) => response.data))
        ).catch(() => null);

        if (!bookData) {
          throw new NotFoundException(`Book with ID ${createReviewDto.bookId} not found in Open Library`);
        }

        // Create new book with proper data mapping
        const createBookDto = {
          id: createReviewDto.bookId,
          title: bookData.title,
          description: bookData.description?.value || bookData.description || '',
          authors: bookData.authors?.map(author => author.author?.key?.split('/').pop() || author.name) || [],
          subjects: bookData.subjects || [],
          publishDate: bookData.first_publish_date || bookData.publish_date,
          coverImage: bookData.covers?.[0] ?
            `${this.OPEN_LIBRARY_COVER_ENDPOINT}/id/${bookData.covers[0]}-L.jpg` : null
        };

        book = await this.bookModel.create(createBookDto);
      }

      // Create the review
      const review = await this.reviewModel.create({
        ...createReviewDto,
        createdAt: new Date(),
      });

      // Update book's review statistics using aggregation
      const stats = await this.reviewModel.aggregate([
        { $match: { bookId: createReviewDto.bookId } },
        {
          $group: {
            _id: null,
            averageRating: { $avg: '$rating' },
            totalReviews: { $sum: 1 }
          }
        }
      ]).exec();

      const { averageRating, totalReviews } = stats[0] || { averageRating: review.rating, totalReviews: 1 };

      await this.bookModel.updateOne(
        { id: createReviewDto.bookId },
        {
          $push: { reviews: review._id },
          $set: { averageRating, totalReviews }
        }
      );

      return review;
    } catch (error) {
      if (error instanceof NotFoundException) {
        throw error;
      }
      throw new Error(`Failed to create review: ${error.message}`);
    }
  }

  async getBookReviews(bookId: number) {
    const book = await this.bookModel.findOne({ id: bookId }).populate('reviews');
    if (!book) {
      throw new NotFoundException('Book not found');
    }
    return book.reviews;
  }

  async getUserReviews(userId: string): Promise<Review[]> {
    const reviews = await this.reviewModel
      .find({ userId })
      .populate('bookId')
      .sort({ createdAt: -1 })
      .exec();

    if (!reviews || reviews.length === 0) {
      throw new NotFoundException(`No reviews found for user ${userId}`);
    }

    const updatedReviews = reviews.map((review) => {
      review.emotion = this.detectEmotion(review.comment);
      return review;
    })

    return updatedReviews;
  }

  detectEmotion(text: string): string {
    const sentiment = new Sentiment();
    const result = sentiment.analyze(text);
    console.log('Sentiment analysis result:', result);
    if (result.score > 0) return 'positive';
    if (result.score < 0) return 'negative';
    return 'neutral';
  }

}
