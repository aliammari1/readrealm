package tn.esprit.libraryapp.api

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import tn.esprit.libraryapp.enums.Genre
import tn.esprit.libraryapp.models.Book

interface BookService {
    @GET("book/genre/{genre}")
    suspend fun getBooks(@Path("genre") genre: Genre): List<Book>

    @GET("book/details/{id}")
    suspend fun getBookDetails(@Path("id") id: Int): Book

    @GET("book/search")
    suspend fun searchBooks(@Query("query") query: String): List<Book>
}

