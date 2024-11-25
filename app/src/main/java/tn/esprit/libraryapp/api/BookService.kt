package tn.esprit.libraryapp.api

import retrofit2.http.GET
import tn.esprit.libraryapp.models.Book

interface BookService {
    @GET("books")
    suspend fun getBooks(): List<Book>
}