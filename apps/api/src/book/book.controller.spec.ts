import { Test, TestingModule } from '@nestjs/testing';
import { BookController } from './book.controller';
import { BookService } from './book.service';
import { TTSService } from './tts.service';
import { ReviewService } from './review.service';
import { BookmarkService } from './bookmark.service';

describe('BookController', () => {
  let controller: BookController;
  const bookService = {
    findAll: jest.fn().mockResolvedValue([{ id: 1, title: '1984' }]),
    searchBooks: jest.fn(),
  };

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      controllers: [BookController],
      providers: [
        { provide: BookService, useValue: bookService },
        { provide: TTSService, useValue: {} },
        { provide: ReviewService, useValue: {} },
        { provide: BookmarkService, useValue: {} },
      ],
    }).compile();

    controller = module.get<BookController>(BookController);
  });

  it('should be defined', () => {
    expect(controller).toBeDefined();
  });

  it('findAll delegates to BookService', async () => {
    await expect(controller.findAll()).resolves.toEqual([
      { id: 1, title: '1984' },
    ]);
    expect(bookService.findAll).toHaveBeenCalled();
  });
});
