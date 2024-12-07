import 'openai/shims/node';
import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateBookDto } from './dto/create-book.dto';
import { UpdateBookDto } from './dto/update-book.dto';
import { HttpService } from '@nestjs/axios';
import { map, mergeMap } from 'rxjs/operators';
import { Book, BookDocument } from './entities/book.entity';
import { forkJoin, merge, of, from, firstValueFrom } from 'rxjs';
import { randomInt } from 'crypto';
import { HfInference } from '@huggingface/inference';
import { GoogleGenerativeAI } from '@google/generative-ai';
import { writeFile } from 'fs/promises';
import { AzureOpenAI } from 'openai';
import { ConfigService } from '@nestjs/config';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';

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
    private readonly httpService: HttpService,
    private readonly configService: ConfigService,
  ) {
    this.azureTtsKey = configService.get<string>('azure.tts.key');
    this.azureTtsEndpoint = configService.get<string>('azure.tts.endpoint');
    this.azureTtsModel = configService.get<string>('azure.tts.model');
    this.geminiApiKey = configService.get<string>('gemini.key');
    this.geminiModel = configService.get<string>('gemini.model');
  }

  create(createBookDto: CreateBookDto) {
    return 'This action adds a new book';
  }

  findAll() {
    let books = this.httpService
      .get(`${this.OPEN_LIBRARY_API_ENDPOINT}/subjects/adventure.json`)
      .pipe(map((response) => response.data));
    return books;
  }

  searchBooks(title: string) {
    let titleEncoded = encodeURIComponent(title);
    let searchUrl = `${this.OPEN_LIBRARY_API_ENDPOINT}/search.json?q=${titleEncoded}&fields=*,availability&limit=10&lang=en`;
    let books = this.httpService.get(searchUrl).pipe(
      map((searchResponse) => {
        return searchResponse.data.docs.map((searchResult) => {
          let book = new Book();
          book.title = searchResult.title;
          book.author = searchResult.author_name[0];
          book.publicationDate = searchResult.first_publish_year;
          book.numOfPages = searchResult.number_of_pages_median;
          book.coverImage = `${this.OPEN_LIBRARY_COVER_ENDPOINT}/b/id/${searchResult.cover_i}-M.jpg`;
          // Add availability information
          // book.availability = searchResult.availability;
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
          let book = new Book();
          book.id = Number(bookDetails.key.split('/')[2].replace('OL', '').replace('W', ''));
          book.title = bookDetails.title;
          book.author = bookDetails.author_name?.[0];
          book.publicationDate = bookDetails.first_publish_year;
          book.numOfPages = bookDetails.number_of_pages_median;
          book.coverImage = `${this.OPEN_LIBRARY_COVER_ENDPOINT}/b/id/${bookDetails.cover_id}-M.jpg`;
          book.genre = bookDetails.subjects ? bookDetails.subjects.join(', ') : 'Unknown';
          return book;
        }),
      );
    return book;
  }

  findAllGenres() {
    const actionBooks = this.findBooksByGenre('action');
    const adventureBooks = this.findBooksByGenre('adventure');
    const fantasyBooks = this.findBooksByGenre('fantasy');
    let books = forkJoin({
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
    return books;
  }

  findBooksByGenre(genre: string) {
    if (genre.toLowerCase() === 'all') {
      return this.findAllGenres();
    }
    let books = this.httpService
      .get(`${this.OPEN_LIBRARY_API_ENDPOINT}/subjects/${genre}.json?limit=10`)
      .pipe(
        mergeMap((response) => {
          let works = response.data.works;
          let bookObservables = works.map((work) => {
            let titleEncoded = encodeURIComponent(work.title);
            let searchUrl = `${this.OPEN_LIBRARY_API_ENDPOINT}/search.json?q=${titleEncoded}&fields=*,availability&limit=1&lang=en`;
            return this.httpService.get(searchUrl).pipe(
              map((searchResponse) => {
                let searchResult = searchResponse.data.docs[0];
                let book = new Book();
                book.id = Number(searchResult.key.split('/')[2].replace('OL', '').replace('W', ''));
                book.title = work.title;
                book.author = searchResult.author_name[0];
                book.publicationDate = searchResult.first_publish_year;
                book.numOfPages = searchResult.number_of_pages_median;
                book.coverImage = `${this.OPEN_LIBRARY_COVER_ENDPOINT}/b/id/${work.cover_id}-M.jpg`;
                book.genre = genre.charAt(0).toUpperCase() + genre.slice(1);
                // Add availability information
                // book.availability = searchResult.availability;
                return book;
              }),
            );
          });
          return forkJoin(bookObservables);
        }),
      );
    return books;
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
      const book = new Book();
      book.id = bookData.id;
      book.title = bookData.title;
      book.author = bookData.authors.length > 0 ? bookData.authors[0].name : 'Unknown';
      book.publicationDate = bookData.download_count;
      book.coverImage = bookData.formats['image/jpeg'];
      book.genre = bookData.subjects.length > 0 ? bookData.subjects[0] : 'Unknown';

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

  addReview(bookId: number, userId: string, comment: string, rating: number) {
    return this.httpService
      .get(`${this.OPEN_LIBRARY_API_ENDPOINT}/works/OL${bookId}W.json`)
      .pipe(
        mergeMap(async (response) => {
          const bookData = response.data;
          const book = await this.bookModel.findOne({ id: bookId });

          const review = {
            userId,
            comment,
            rating,
            date: new Date()
          };

          if (!book) {
            // Create new book if it doesn't exist
            const newBook = new this.bookModel({
              id: bookId,
              title: bookData.title,
              author: bookData.author_name?.[0],
              // ...other book properties...
              reviews: [review],
              totalRating: rating,
              numberOfRatings: 1
            });
            await newBook.save();
            return { success: true, review, averageRating: rating };
          }

          // Update existing book
          book.reviews.push(review);
          book.totalRating = (book.totalRating || 0) + rating;
          book.numberOfRatings = (book.numberOfRatings || 0) + 1;
          await book.save();

          return {
            success: true,
            averageRating: book.getAverageRating(),
            review
          };
        })
      );
  }

  getBookRating(bookId: number) {
    return from(this.bookModel.findOne({ id: bookId })).pipe(
      map(book => {
        if (!book) return { averageRating: 0, numberOfRatings: 0, reviews: [] };
        return {
          averageRating: book.getAverageRating(),
          numberOfRatings: book.numberOfRatings || 0,
          reviews: book.reviews || []
        };
      })
    );
  }

  async addBookmark(bookId: number, userId: string, note?: string) {
    if (!bookId || !userId) {
      throw new NotFoundException('Book ID and User ID are required');
    }

    return this.httpService
      .get(`${this.OPEN_LIBRARY_API_ENDPOINT}/works/OL${bookId}W.json`)
      .pipe(
        mergeMap(async (response) => {
          const bookData = response.data;
          let book = await this.bookModel.findOne({ id: bookId });

          // Check if bookmark already exists
          if (book?.bookmarks?.some(bookmark => bookmark.userId === userId)) {
            return {
              success: false,
              message: 'Bookmark already exists for this user'
            };
          }

          const bookmark = {
            userId,
            dateAdded: new Date(),
            note: note || '',
            page: 0,
            lastAccessedDate: new Date(),
            status: 'active'
          };

          if (!book) {
            // Create new book if it doesn't exist
            book = new this.bookModel({
              id: bookId,
              title: bookData.title,
              author: bookData.authors?.name || bookData.author_name?.[0],
              publicationDate: bookData.first_publish_year,
              coverImage: bookData.covers ?
                `${this.OPEN_LIBRARY_COVER_ENDPOINT}/b/id/${bookData.covers[0]}-M.jpg` :
                null,
              bookmarks: [bookmark]
            });
          } else {
            // Update existing book
            book.bookmarks.push(bookmark);
          }

          try {
            await book.save();
            return {
              success: true,
              bookmark,
              message: 'Bookmark added successfully'
            };
          } catch (error) {
            throw new Error('Failed to save bookmark: ' + error.message);
          }
        })
      );
  }

  removeBookmark(bookId: number, userId: string) {
    return from(this.bookModel.findOneAndUpdate(
      { id: bookId },
      { $pull: { bookmarks: { userId } } },
      { new: true }
    )).pipe(
      map(book => ({ success: true }))
    );
  }

  getUserBookmarks(userId: string) {
    return from(this.bookModel.find({
      'bookmarks.userId': userId
    }));
  }

  findOne(id: number) {
    return `This action returns a #${id} book`;
  }

  update(id: number, updateBookDto: UpdateBookDto) {
    return `This action updates a #${id} book`;
  }

  remove(id: number) {
    return `This action removes a #${id} book`;
  }
}
