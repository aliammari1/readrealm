package tn.esprit.libraryapp.repository

import okhttp3.ResponseBody
import tn.esprit.libraryapp.api.RetrofitService
import tn.esprit.libraryapp.enums.Genre
import tn.esprit.libraryapp.models.Book

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
}

