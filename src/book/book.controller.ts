import {
  Controller,
  Get,
  Post,
  Body,
  Patch,
  Param,
  Delete,
  Query,
} from '@nestjs/common';
import { BookService } from './book.service';
import { CreateBookDto } from './dto/create-book.dto';
import { UpdateBookDto } from './dto/update-book.dto';

@Controller('book')
export class BookController {
  constructor(private readonly bookService: BookService) { }

  @Post()
  create(@Body() createBookDto: CreateBookDto) {
    return this.bookService.create(createBookDto);
  }

  @Get()
  findAll() {
    return this.bookService.findAll();
  }

  @Get('search')
  searchBooks(@Query('query') query: string) {
    return this.bookService.searchBooks(query);
  }

  @Get('details/:id')
  getBookDetails(@Param('id') id: number) {
    return this.bookService.getBookDetails(id);
  }

  @Post('summary')
  async getBookSummary(@Body() createBookDto: CreateBookDto) {
    const response = await this.bookService.getBookSummary(createBookDto);
    return response;
  }

  @Post('ebook')
  async getEbook(@Body() createBookDto: CreateBookDto) {
    if (createBookDto.textData == '') createBookDto = new CreateBookDto();
    const response = await this.bookService.getBookTTS(createBookDto);
    return response;
  }

  @Get('genre/:genre')
  findBooksByGenre(@Param('genre') genre: string) {
    return this.bookService.findBooksByGenre(genre);
  }

  @Get(':id')
  findOne(@Param('id') id: string) {
    return this.bookService.findOne(+id);
  }

  @Patch(':id')
  update(@Param('id') id: string, @Body() updateBookDto: UpdateBookDto) {
    return this.bookService.update(+id, updateBookDto);
  }

  @Delete(':id')
  remove(@Param('id') id: string) {
    return this.bookService.remove(+id);
  }

  @Post(':id/review')
  addReview(
    @Param('id') id: number,
    @Body() reviewData: { userId: string; comment: string; rating: number }
  ) {
    return this.bookService.addReview(
      id,
      reviewData.userId,
      reviewData.comment,
      reviewData.rating
    );
  }

  @Get(':id/rating')
  getBookRating(@Param('id') id: number) {
    return this.bookService.getBookRating(id);
  }

  @Post(':id/bookmark')
  addBookmark(
    @Param('id') id: number,
    @Body() bookmarkData: { userId: string; note?: string }
  ) {
    return this.bookService.addBookmark(
      id,
      bookmarkData.userId,
      bookmarkData.note
    );
  }

  @Delete(':id/bookmark/:userId')
  removeBookmark(
    @Param('id') id: number,
    @Param('userId') userId: string
  ) {
    return this.bookService.removeBookmark(id, userId);
  }

  @Get('bookmarks/:userId')
  getUserBookmarks(@Param('userId') userId: string) {
    return this.bookService.getUserBookmarks(userId);
  }
}
