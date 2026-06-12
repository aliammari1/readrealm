import {
  Controller,
  Get,
  Post,
  Body,
  Patch,
  Param,
  Delete,
  Query,
  Res,
  Put,
  BadRequestException,
  UseInterceptors,
} from '@nestjs/common';
import { ApiHeader, ApiOperation, ApiTags } from '@nestjs/swagger';
import { Throttle } from '@nestjs/throttler';
import { IdempotencyInterceptor } from '../common/idempotency.interceptor';
import { BookService } from './book.service';
import { CreateBookDto } from './dto/create-book.dto';
import { UpdateBookDto } from './dto/update-book.dto';
import { Response } from 'express';
import { CreateReviewDto } from './dto/create-review.dto';
import { TTSService } from './tts.service';
import { ReviewService } from './review.service';
import { BookmarkService } from './bookmark.service';

@ApiTags('book')
@Controller('book')
export class BookController {
  constructor(
    private readonly bookService: BookService,
    private readonly ttsService: TTSService,
    private readonly reviewService: ReviewService,
    private readonly bookmarkService: BookmarkService,
  ) {}

  @ApiOperation({ summary: 'Create a book' })
  @Post()
  async create(@Body() createBookDto: CreateBookDto) {
    return await this.bookService.create(createBookDto);
  }

  @ApiOperation({ summary: 'List all stored books' })
  @Get()
  async findAll() {
    return await this.bookService.findAll();
  }

  @ApiOperation({ summary: 'Search Open Library by title/query' })
  @Get('search')
  searchBooks(@Query('q') query: string) {
    return this.bookService.searchBooks(query);
  }

  @ApiOperation({ summary: 'Get full book details by id (Open Library)' })
  @Get('details/:id')
  async getBookDetails(@Param('id') id: number) {
    return await this.bookService.getBookDetails(id);
  }

  // AI/TTS calls hit paid providers — throttle harder than the global default.
  @Throttle({ default: { ttl: 60_000, limit: 20 } })
  @ApiOperation({ summary: 'AI-generated 5-line summary for a book title' })
  @Get('summary/:title')
  async getBookSummary(@Param('title') title: string) {
    const response = await this.bookService.getBookSummary(title);
    return response;
  }

  // Expensive TTS generation: throttle, and make client retries safe via an
  // optional Idempotency-Key header (replays the cached audio result).
  @Throttle({ default: { ttl: 60_000, limit: 20 } })
  @UseInterceptors(IdempotencyInterceptor)
  @ApiHeader({
    name: 'Idempotency-Key',
    required: false,
    description:
      'Opaque client-generated id (e.g. a UUID). Retries with the same key ' +
      'replay the cached result instead of re-running TTS.',
  })
  @ApiOperation({ summary: 'Generate an audio (TTS) ebook from book text' })
  @Post('ebook')
  async getEbook(@Body() createBookDto: CreateBookDto) {
    if (createBookDto.textData == '') createBookDto = new CreateBookDto();
    const response = await this.ttsService.getBookTTS(createBookDto);
    return response;
  }

  @ApiOperation({ summary: 'Stream TTS audio (audio/mpeg) for a book title' })
  @Get('tts/stream/:title')
  async streamBookTTSByTitle(
    @Param('title') title: string,
    @Res() response: Response,
  ) {
    try {
      const audioStream = await this.ttsService.getBookTTSByTitle(title);

      if (!audioStream) {
        throw new Error('Failed to generate audio stream');
      }

      response.setHeader('Content-Type', 'audio/mpeg');
      response.setHeader('Transfer-Encoding', 'chunked');
      response.setHeader('Cache-Control', 'no-cache');
      response.setHeader('Content-Disposition', 'inline');

      audioStream.pipe(response);

      audioStream.on('end', () => {
        response.end();
      });

      audioStream.on('error', (error) => {
        console.error('Stream error:', error);
        if (!response.headersSent) {
          response.status(500).json({ error: 'Stream error occurred' });
        }
      });
    } catch (error) {
      console.error('Streaming error:', error);
      if (!response.headersSent) {
        response.status(error.status || 500).json({
          error: error.message || 'An unexpected error occurred',
        });
      }
    }
  }

  @ApiOperation({ summary: 'Toggle a bookmark for a user/book' })
  @Put('bookmark')
  async toggleBookmark(
    @Body() toggleBookmarkDto: { userId: string; book: CreateBookDto },
  ) {
    try {
      const book = await this.bookService.findOne(toggleBookmarkDto.book.id);
      if (book) {
        return await this.bookmarkService.toggleBookmark(
          toggleBookmarkDto.book.id,
          toggleBookmarkDto.userId,
        );
      }

      const newBook = await this.bookService.create(toggleBookmarkDto.book);
      return await this.bookmarkService.toggleBookmark(
        newBook.id,
        toggleBookmarkDto.userId,
      );
    } catch (error) {
      throw new BadRequestException(
        error.message || 'Failed to toggle bookmark',
      );
    }
  }

  @ApiOperation({ summary: 'Stream books by genre (SSE) with pagination' })
  @Get('genre/:genre')
  async findBooksByGenre(
    @Param('genre') genre: string,
    @Query('offset') offset = '0',
    @Query('limit') limit = '10',
    @Res() res: Response,
  ) {
    const parsedOffset = parseInt(offset, 10);
    const parsedLimit = Math.min(parseInt(limit, 10), 50); // Cap at 50 items

    // Set performance-oriented headers
    res.setHeader('Cache-Control', 'public, max-age=900'); // 15 minutes
    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Connection', 'keep-alive');
    // res.setHeader('X-Accel-Buffering', 'no'); // Disable nginx buffering
    // res.setHeader('Content-Encoding', 'gzip'); // Enable compression

    const now = new Date();
    res.setHeader('Last-Modified', now.toUTCString());
    res.setHeader('Expires', new Date(now.getTime() + 900000).toUTCString());

    try {
      let count = 0;
      for await (const book of this.bookService.findBooksByGenre(
        genre,
        parsedOffset,
        parsedLimit,
      )) {
        if (count >= parsedLimit) break;
        res.write(`data: ${JSON.stringify(book)}\n\n`);
        count++;
      }
    } catch (error) {
      console.error(`Error streaming books: ${error.message}`);
      if (!res.headersSent) {
        res.status(500).json({ error: 'Failed to fetch books' });
      }
    } finally {
      res.end();
    }
  }

  @ApiOperation({ summary: 'Get a stored book by id' })
  @Get(':id')
  async findOne(@Param('id') id: number) {
    return await this.bookService.findOne(id);
  }

  @ApiOperation({ summary: 'Update a book' })
  @Patch(':id')
  async update(@Param('id') id: number, @Body() updateBookDto: UpdateBookDto) {
    return await this.bookService.update(id, updateBookDto);
  }

  @ApiOperation({ summary: 'Delete a book' })
  @Delete(':id')
  async remove(@Param('id') id: number) {
    return await this.bookService.remove(id);
  }

  @ApiOperation({ summary: "List a user's bookmarked books" })
  @Get('bookmarks/:userId')
  async getUserBookmarks(@Param('userId') userId: string) {
    return await this.bookmarkService.getUserBookmarks(userId);
  }

  @ApiOperation({ summary: 'Create a review for a book' })
  @Post('reviews/:id')
  createReview(
    @Param('id') bookId: number,
    @Body() createReviewDto: CreateReviewDto,
  ) {
    if (!bookId || isNaN(bookId)) {
      throw new BadRequestException('Invalid book ID');
    }
    createReviewDto.bookId = bookId;
    return this.reviewService.createReview(createReviewDto);
  }

  @ApiOperation({ summary: 'List reviews for a book' })
  @Get('reviews/:id')
  getBookReviews(@Param('id') bookId: number) {
    return this.reviewService.getBookReviews(bookId);
  }

  @ApiOperation({ summary: "List a user's reviews" })
  @Get('user-reviews/:userId')
  getUserReviews(@Param('userId') userId: string) {
    if (!userId) {
      throw new BadRequestException('Invalid user ID');
    }
    return this.reviewService.getUserReviews(userId);
  }
}
