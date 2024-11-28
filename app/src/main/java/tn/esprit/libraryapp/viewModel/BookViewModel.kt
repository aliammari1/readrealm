package tn.esprit.libraryapp.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import tn.esprit.libraryapp.enums.Genre
import tn.esprit.libraryapp.models.Book
import tn.esprit.libraryapp.repository.BookRepository

class BookViewModel : ViewModel() {
    private val repository = BookRepository()
    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> = _books

    fun fetchBooks(genre: Genre) {
        viewModelScope.launch {
            try {
                _books.value = repository.getBooks(genre)
                Log.d("books", _books.value.toString())
            } catch (e: Exception) {
            }
        }
    }
}