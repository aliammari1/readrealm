package tn.esprit.libraryapp.api

import okhttp3.ResponseBody
import retrofit2.http.*
import tn.esprit.libraryapp.enums.Genre
import tn.esprit.libraryapp.models.*

interface BookService {
    @GET("book/genre/{genre}")
    suspend fun getBooks(@Path("genre") genre: Genre): List<Book>

    @GET("book/{id}")
    suspend fun getBookDetails(@Path("id") bookId: Int): Book

    @GET("book/search")
    suspend fun searchBooks(@Query("q") query: String): List<Book>

    @GET("book/stream/{title}")
    suspend fun streamAudioBookByTitle(@Path("title") title: String): ResponseBody

    @PUT("book/bookmark")
    suspend fun toggleBookmark(@Body request: ToggleBookmarkRequest): Book

    @GET("book/bookmarks/{userId}")
    suspend fun getBookmarks(@Path("userId") userId: String): List<Book>

    @POST("book/reviews/{bookId}")
    suspend fun addReview(
        @Path("bookId") bookId: Int,
        @Body request: ReviewRequest
    ): Book

    @DELETE("book/{bookId}/reviews/{reviewId}")
    suspend fun removeReview(@Body request: DeleteReviewRequest): Book

    @GET("book/reviews/{bookId}")
    suspend fun getReviews(@Path("bookId") bookId: Int): List<Review>
}
