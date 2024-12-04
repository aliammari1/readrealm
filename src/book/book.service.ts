import 'openai/shims/node';
import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateBookDto } from './dto/create-book.dto';
import { UpdateBookDto } from './dto/update-book.dto';
import { HttpService } from '@nestjs/axios';
import { map, mergeMap } from 'rxjs/operators';
import { Book, BookDocument } from './entities/book.entity';
import { forkJoin, merge, of, from } from 'rxjs';
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
          book.author = bookDetails.author_name[0];
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
  getBookByTitle(title: string) {
    const encodedTitle = encodeURIComponent(title);
    const url = `${this.GUTEDEX_API_ENDPOINT}/books?search=${encodedTitle}`;
    let booktext = '';
    return this.httpService.get(url).pipe(
      mergeMap((response) => {
        const books = response.data.results;
        const bookObservables = books.map((bookData) => {
          const book = new Book();
          book.id = bookData.id;
          book.title = bookData.title;
          book.author =
            bookData.authors.length > 0 ? bookData.authors[0].name : 'Unknown';
          book.publicationDate = bookData.download_count;
          book.coverImage = bookData.formats['image/jpeg'];
          book.genre =
            bookData.subjects.length > 0 ? bookData.subjects[0] : 'Unknown';

          const textUrl =
            bookData.formats['text/plain; charset=utf-8'] ||
            bookData.formats['text/plain; charset=us-ascii'] ||
            bookData.formats['text/plain'];
          if (textUrl) {
            return this.httpService.get(textUrl, { responseType: 'text' }).pipe(
              map((textResponse) => {
                booktext = textResponse.data;
                return book;
              }),
            );
          } else {
            booktext = 'Text not available';
            return of(book);
          }
        });
        return forkJoin(bookObservables);
      }),
    );
  }

  async getBookSummary(data: CreateBookDto) {
    // data = new CreateBookDto();
    if (!data.textData) {
      return 'No text data provided';
    }
    data.textData = data.textData.replace(/(\r\n|\n|\r)/gm, ' ');
    data.textData = data.textData.replace(/ +(?= )/g, '');
    const genAI = new GoogleGenerativeAI(this.geminiApiKey);
    const model = genAI.getGenerativeModel({ model: this.geminiModel });
    const prompt = 'summaize this book: ' + data.textData;

    const result = await model.generateContent(prompt);
    // console.log(result.response.text());

    return result;
  }

  async getBookTTS(data: CreateBookDto) {
    if (!data.textData) {
      return 'No text data provided';
    }

    data.textData = data.textData.replace(/(\r\n|\n|\r)/gm, ' ');
    data.textData = data.textData.replace(/ +(?= )/g, '');
    const endpoint = this.azureTtsEndpoint || '';
    const apiKey = this.azureTtsKey || '';
    const speechFilePath = 'audio.mp3';

    // Required Azure OpenAI deployment name and API version
    const deploymentName = this.azureTtsModel;
    const apiVersion = '2024-08-01-preview';

    const maxLength = 4096;
    const chunks = [];
    for (let i = 0; i < data.textData.length; i += maxLength) {
      chunks.push(data.textData.substring(i, i + maxLength));
    }
    const client = new AzureOpenAI({
      endpoint,
      apiKey,
      apiVersion,
      deployment: deploymentName,
    });
    let streamToRead;
    for (const chunk of chunks) {
      const response = await client.audio.speech.create({
        model: deploymentName,
        voice: 'alloy',
        input: chunk,
      });
      if (response.ok) streamToRead = response.body;
      else
        throw new Error(
          `Failed to generate audio stream: ${response.statusText}`,
        );

      // console.log(`Streaming response to ${speechFilePath}`);
      await writeFile(speechFilePath, streamToRead);
      // console.log('Finished streaming chunk');
    }
    return '';
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

  addBookmark(bookId: number, userId: string, note?: string) {
    return this.httpService
      .get(`${this.OPEN_LIBRARY_API_ENDPOINT}/works/OL${bookId}W.json`)
      .pipe(
        mergeMap(async (response) => {
          const bookData = response.data;
          const book = await this.bookModel.findOne({ id: bookId });
          
          const bookmark = {
            userId,
            dateAdded: new Date(),
            note
          };

          if (!book) {
            // Create new book if it doesn't exist
            const newBook = new this.bookModel({
              id: bookId,
              title: bookData.title,
              author: bookData.author_name?.[0],
              // ...other book properties...
              bookmarks: [bookmark]
            });
            await newBook.save();
            return { success: true, bookmark };
          }

          // Update existing book
          book.bookmarks.push(bookmark);
          await book.save();

          return { success: true, bookmark };
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
