package tn.esprit.libraryapp.repository

import tn.esprit.libraryapp.api.RetrofitService
import tn.esprit.libraryapp.enums.Genre
import tn.esprit.libraryapp.models.Book

class BookRepository {
    private val bookService = RetrofitService.bookService
    suspend fun getBooks(genre: Genre): List<Book> {
        return bookService.getBooks(genre)
    }
}
