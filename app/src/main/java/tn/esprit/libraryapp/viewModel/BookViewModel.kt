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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BookViewModel : ViewModel() {
    private val repository = BookRepository()
    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> = _books
    private val _bookDetails = MutableLiveData<Book>()
    val bookDetails: LiveData<Book> = _bookDetails

    // Add loading state
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableLiveData<List<Book>>()
    val searchResults: LiveData<List<Book>> = _searchResults

    fun fetchBooks(genre: Genre) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _books.value = repository.getBooks(genre)
                Log.d("BookViewModel", "Fetched ${_books.value?.size} books for genre ${genre.value}")
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error fetching books", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun fetchBookDetails(bookId: Int) {
        viewModelScope.launch {
            try {
                _bookDetails.value = repository.getBookDetails(bookId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _searchResults.value = repository.searchBooks(query)
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error searching books", e)
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
