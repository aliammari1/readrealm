package tn.esprit.libraryapp.repository

import okhttp3.ResponseBody
import tn.esprit.libraryapp.api.RetrofitService
import tn.esprit.libraryapp.enums.Genre
import tn.esprit.libraryapp.models.*

// Update the imports to use the new Review model
import tn.esprit.libraryapp.models.Review

class BookRepository {
    private val bookService = RetrofitService.bookService

    suspend fun getBooks(genre: Genre): List<Book> {
        return bookService.getBooks(genre)
    }

    suspend fun getBookDetails(bookId: Int): Book {
        return bookService.getBookDetails(bookId)
    }

    suspend fun searchBooks(query: String): List<Book> {
        return bookService.searchBooks(query)
    }

    suspend fun streamAudioBookByTitle(title: String): ResponseBody {
        return bookService.streamAudioBookByTitle(title)
    }

    suspend fun toggleBookmark(userId: String, book: Book): Book {
        val request = ToggleBookmarkRequest(userId = userId, book = book)
        return bookService.toggleBookmark(request)
    }

    suspend fun getBookmarks(userId: String): List<Book> {
        return bookService.getBookmarks(userId)
    }

    suspend fun addReview(bookId: Int, userId: String, rating: Int, comment: String): Book {
        val request = ReviewRequest(userId = userId, rating = rating, comment = comment)
        return bookService.addReview(bookId, request)
    }

    suspend fun removeReview(userId: String, bookId: String, reviewId: String): Book {
        return bookService.removeReview(DeleteReviewRequest(userId, bookId, reviewId))
    }

    suspend fun getReviews(bookId: Int): List<Review> {
        return bookService.getReviews(bookId)
    }
}
