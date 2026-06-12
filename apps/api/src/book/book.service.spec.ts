import { Test, TestingModule } from '@nestjs/testing';
import { getModelToken } from '@nestjs/mongoose';
import { HttpService } from '@nestjs/axios';
import { ConfigService } from '@nestjs/config';
import { BookService } from './book.service';
import { Book } from './entities/book.entity';
import { Review } from './entities/review.entity';
import { EpubService } from './epub.service';
import { ReviewService } from './review.service';
import { BookmarkService } from './bookmark.service';

describe('BookService', () => {
  let service: BookService;
  const bookModel = {
    find: jest.fn().mockReturnValue({ exec: jest.fn().mockResolvedValue([]) }),
    findOne: jest
      .fn()
      .mockReturnValue({ exec: jest.fn().mockResolvedValue(null) }),
  };

  beforeEach(async () => {
    const module: TestingModule = await Test.createTestingModule({
      providers: [
        BookService,
        { provide: getModelToken(Book.name), useValue: bookModel },
        { provide: getModelToken(Review.name), useValue: {} },
        { provide: HttpService, useValue: { get: jest.fn() } },
        {
          provide: ConfigService,
          useValue: { get: jest.fn().mockReturnValue('') },
        },
        { provide: EpubService, useValue: {} },
        { provide: ReviewService, useValue: {} },
        { provide: BookmarkService, useValue: {} },
      ],
    }).compile();

    service = module.get<BookService>(BookService);
  });

  it('should be defined', () => {
    expect(service).toBeDefined();
  });

  it('findAll queries the book model', async () => {
    await expect(service.findAll()).resolves.toEqual([]);
    expect(bookModel.find).toHaveBeenCalled();
  });
});
