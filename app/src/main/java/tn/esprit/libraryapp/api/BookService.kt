package tn.esprit.libraryapp.api

import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming
import tn.esprit.libraryapp.enums.Genre
import tn.esprit.libraryapp.models.Book

interface BookService {
    @GET("book/genre/{genre}")
    suspend fun getBooks(@Path("genre") genre: Genre): List<Book>

    @GET("book/details/{id}")
    suspend fun getBookDetails(@Path("id") id: Int): Book

    @GET("book/search")
    suspend fun searchBooks(@Query("query") query: String): List<Book>

    @Streaming
    @GET("book/tts/stream/{title}")
    suspend fun streamAudioBookByTitle(@Path("title") title: String): ResponseBody
}

