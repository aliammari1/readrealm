package tn.esprit.libraryapp.api

import retrofit2.http.GET
import retrofit2.http.Path
import tn.esprit.libraryapp.enums.Genre
import tn.esprit.libraryapp.models.Book

interface BookService {
    @GET("book/{genre}")
    suspend fun getBooks(@Path("genre") genre: Genre): List<Book>
}

