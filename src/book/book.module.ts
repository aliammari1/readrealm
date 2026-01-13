import { Module } from '@nestjs/common';
import { BookService } from './book.service';
import { TTSService } from './tts.service';
import { EpubService } from './epub.service';
import { ReviewService } from './review.service';
import { BookmarkService } from './bookmark.service';
import { BookController } from './book.controller';
import { MongooseModule } from '@nestjs/mongoose';
import { Book, BookSchema } from './entities/book.entity';
import { Review, ReviewSchema } from './entities/review.entity';
import { HttpModule } from '@nestjs/axios';
import { ConfigModule } from '@nestjs/config';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: Book.name, schema: BookSchema },
      { name: Review.name, schema: ReviewSchema },
    ]),
    HttpModule,
    ConfigModule,
  ],
  controllers: [BookController],
  providers: [
    {
      provide: 'SERVICES',
      useFactory: (...services) => services,
      inject: [
        BookService,
        TTSService,
        EpubService,
        ReviewService,
        BookmarkService,
      ],
    },
    BookService,
    TTSService,
    EpubService,
    ReviewService,
    BookmarkService,
  ],
})
export class BookModule {}
