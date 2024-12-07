package tn.esprit.libraryapp.viewModel

import android.content.Context
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import tn.esprit.libraryapp.services.AudioStreamManager

class BookViewModel : ViewModel() {
    private val repository = BookRepository()
    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> = _books
    private val _bookDetails = MutableLiveData<Book>()
    val bookDetails: LiveData<Book> = _bookDetails

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableLiveData<List<Book>>()
    val searchResults: LiveData<List<Book>> = _searchResults

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _ebookUiState = MutableStateFlow(EbookUiState())
    val ebookUiState: StateFlow<EbookUiState> = _ebookUiState

    private var audioStreamManager: AudioStreamManager? = null

    data class EbookUiState(
        val text: String = "",
        val isPlaying: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    fun updateEbookText(text: String) {
        _ebookUiState.update { it.copy(text = text) }
    }

    fun fetchBooks(genre: Genre) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _books.value = repository.getBooks(genre)
                Log.d(
                    "BookViewModel",
                    "Fetched ${_books.value?.size} books for genre ${genre.value}"
                )
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
                _ebookUiState.update { it.copy(isLoading = true, error = null) }
                val book = repository.getBookDetails(bookId)
                val initialText = """
                    Title: ${book.title}
                    Author: ${book.author}
                    
                    This is a sample text for the book ${book.title}. 
                    You can modify this text or enter your own text to be read aloud.
                """.trimIndent()
                _bookDetails.value = book
            } catch (e: Exception) {
                _ebookUiState.update { it.copy(error = e.message) }
                Log.e("BookViewModel", "Error fetching book details", e)
            } finally {
                _ebookUiState.update { it.copy(isLoading = false) }
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

    fun initializeAudio(context: Context) {
        audioStreamManager = AudioStreamManager(context)
    }

    fun playBookAudio(title: String) {
        viewModelScope.launch {
            try {
                _ebookUiState.update { it.copy(isLoading = true, error = null) }
                val audioStream = repository.streamAudioBookByTitle(title)
                audioStreamManager?.playStream(audioStream) { isPlaying ->
                    _ebookUiState.update { it.copy(isPlaying = isPlaying) }
                }
            } catch (e: Exception) {
                _ebookUiState.update { it.copy(error = e.message) }
                Log.e("BookViewModel", "Error streaming audio", e)
            } finally {
                _ebookUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun stopAudio() {
        audioStreamManager?.stop()
        _ebookUiState.update { it.copy(isPlaying = false) }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
        audioStreamManager = null
    }
}