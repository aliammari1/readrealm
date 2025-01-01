import 'openai/shims/node';
import { Injectable, NotFoundException } from '@nestjs/common';
import { CreateBookDto } from './dto/create-book.dto';
import { UpdateBookDto } from './dto/update-book.dto';
import { HttpService } from '@nestjs/axios';
import { map, mergeMap, toArray } from 'rxjs/operators';
import { Book, BookDocument } from './entities/book.entity';
import { forkJoin, firstValueFrom, from } from 'rxjs';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { Review } from './entities/review.entity';
import { CreateReviewDto } from './dto/create-review.dto';
import * as Sentiment from 'sentiment';

@Injectable()
export class ReviewService {
    private readonly OPEN_LIBRARY_API_ENDPOINT = 'https://openlibrary.org';
    private readonly OPEN_LIBRARY_COVER_ENDPOINT = 'https://covers.openlibrary.org';

    constructor(
        @InjectModel(Book.name) private bookModel: Model<BookDocument>,
        @InjectModel(Review.name) private reviewModel: Model<Review>,
        private readonly httpService: HttpService,
    ) {}

    async createReview(createReviewDto: CreateReviewDto) {
        try {
            let book = await this.bookModel.findOne({ id: createReviewDto.bookId });

            if (!book) {
                // Fetch book data from Open Library API using works endpoint
                const bookData = await firstValueFrom(
                    this.httpService
                        .get(
                            `${this.OPEN_LIBRARY_API_ENDPOINT}/works/OL${createReviewDto.bookId}W.json`,
                        )
                        .pipe(map((response) => response.data)),
                ).catch(() => null);

                if (!bookData) {
                    throw new NotFoundException(
                        `Book with ID ${createReviewDto.bookId} not found in Open Library`,
                    );
                }

                const createBookDto = {
                    id: createReviewDto.bookId,
                    title: bookData.title,
                    description:
                        bookData.description?.value || bookData.description || '',
                    authors:
                        bookData.authors?.map(
                            (author) => author.author?.key?.split('/').pop() || author.name,
                        ) || [],
                    subjects: bookData.subjects || [],
                    publishDate: bookData.first_publish_date || bookData.publish_date,
                    coverImage: bookData.covers?.[0]
                        ? `${this.OPEN_LIBRARY_COVER_ENDPOINT}/id/${bookData.covers[0]}-L.jpg`
                        : null,
                };

                book = await this.bookModel.create(createBookDto);
            }

            // Create the review
            const review = await this.reviewModel.create({
                ...createReviewDto,
                createdAt: new Date(),
            });

            // Update book's review statistics using aggregation
            const stats = await this.reviewModel
                .aggregate([
                    { $match: { bookId: createReviewDto.bookId } },
                    {
                        $group: {
                            _id: null,
                            averageRating: { $avg: '$rating' },
                            totalReviews: { $sum: 1 },
                        },
                    },
                ])
                .exec();

            const { averageRating, totalReviews } = stats[0] || {
                averageRating: review.rating,
                totalReviews: 1,
            };

            await this.bookModel.updateOne(
                { id: createReviewDto.bookId },
                {
                    $push: { reviews: review._id },
                    $set: { averageRating, totalReviews },
                },
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
        const book = await this.bookModel
            .findOne({ id: bookId })
            .populate('reviews');
        if (!book) {
            return [];
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
        });

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
