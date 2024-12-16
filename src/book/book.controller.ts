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
} from '@nestjs/common';
import { BookService } from './book.service';
import { CreateBookDto } from './dto/create-book.dto';
import { UpdateBookDto } from './dto/update-book.dto';
import { Response } from 'express';
import { ToggleBookmarkDto } from './dto/toggle-bookmark.dto';
import { CreateReviewDto } from './dto/create-review.dto';

@Controller('book')
export class BookController {
  constructor(private readonly bookService: BookService) { }

  @Post()
  async create(@Body() createBookDto: CreateBookDto) {
    return await this.bookService.create(createBookDto);
  }

  @Get()
  async findAll() {
    return await this.bookService.findAll();
  }

  @Get('search')
  searchBooks(@Query('query') query: string) {
    return this.bookService.searchBooks(query);
  }

  @Get('details/:id')
  getBookDetails(@Param('id') id: number) {
    return this.bookService.getBookDetails(id);
  }

  @Get('summary/:title')
  async getBookSummary(@Param('title') title: string) {
    const response = await this.bookService.getBookSummary(title);
    return response;
  }

  @Post('ebook')
  async getEbook(@Body() createBookDto: CreateBookDto) {
    if (createBookDto.textData == '') createBookDto = new CreateBookDto();
    const response = await this.bookService.getBookTTS(createBookDto);
    return response;
  }

  @Get('tts/stream/:title')
  async streamBookTTSByTitle(
    @Param('title') title: string,
    @Res() response: Response
  ) {
    try {
      const audioStream = await this.bookService.getBookTTSByTitle(title);

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
          error: error.message || 'An unexpected error occurred'
        });
      }
    }
  }

  @Post(':id/toggle-bookmark')
  async toggleBookmark(
    @Param('id') id: string,
    @Body() toggleBookmarkDto: ToggleBookmarkDto,
  ) {
    return await this.bookService.toggleBookmark(parseInt(id, 10), toggleBookmarkDto.userId);
  }

  @Put('bookmark')
  async toggleBookmarkOld(
    @Body() data: { userId: string; book: CreateBookDto },
  ) {
    const newBook = await this.bookService.create(data.book);
    return await this.bookService.toggleBookmark(newBook.id, data.userId);
  }

  @Get('genre/:genre')
  findBooksByGenre(@Param('genre') genre: string) {
    return this.bookService.findBooksByGenre(genre);
  }

  @Get(':id')
  async findOne(@Param('id') id: number) {
    return await this.bookService.findOne(id);
  }

  @Patch(':id')
  async update(@Param('id') id: number, @Body() updateBookDto: UpdateBookDto) {
    return await this.bookService.update(id, updateBookDto);
  }

  @Delete(':id')
  async remove(@Param('id') id: number) {
    return await this.bookService.remove(id);
  }

  @Get('bookmarks/:userId')
  async getUserBookmarks(@Param('userId') userId: string) {
    return await this.bookService.getUserBookmarks(userId);
  }

  @Post('reviews/:id')
  createReview(
    @Param('id') bookId: number,
    @Body() createReviewDto: CreateReviewDto,
  ) {
    if (!bookId || isNaN(bookId)) {
      throw new BadRequestException('Invalid book ID');
    }
    createReviewDto.bookId = bookId;
    return this.bookService.createReview(createReviewDto);
  }

  @Get('reviews/:id')
  getBookReviews(@Param('id') bookId: number) {
    return this.bookService.getBookReviews(bookId);
  }


  @Get('user-reviews/:userId')
  getUserReviews(@Param('userId') userId: string) {
    if (!userId) {
      throw new BadRequestException('Invalid user ID');
    }
    return this.bookService.getUserReviews(userId);
  }
}
