package tn.esprit.libraryapp.viewModel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import tn.esprit.libraryapp.enums.Genre
import tn.esprit.libraryapp.models.Book
import tn.esprit.libraryapp.models.Bookmark
import tn.esprit.libraryapp.models.Review
import tn.esprit.libraryapp.repository.BookRepository
import tn.esprit.libraryapp.services.AudioStreamManager
import tn.esprit.libraryapp.services.TokenManagerProvider

class BookViewModel() : ViewModel() {
    private val repository = BookRepository()
    private val _books = MutableLiveData<List<Book>>()
    val books: LiveData<List<Book>> = _books

    private val _bookmarks = MutableStateFlow<List<Book>>(emptyList())
    val bookmarks: StateFlow<List<Book>> get() = _bookmarks

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

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

    private val _isBookmarked = MutableLiveData<Boolean>()
    val isBookmarked: LiveData<Boolean> get() = _isBookmarked

    private var audioStreamManager: AudioStreamManager? = null

    data class EbookUiState(
        val text: String = "",
        val isPlaying: Boolean = false,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    fun loadBookmarks() {
        viewModelScope.launch {
            try {
                val userId = TokenManagerProvider.getInstance().userId.first()
                if (userId != null) _bookmarks.value = repository.getBookmarks(userId = userId)
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error fetching books", e)
            }
        }
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

    fun toggleBookmark(book: Book) {
        viewModelScope.launch {
            try {
                val userId = TokenManagerProvider.getInstance().userId.first()
                Log.d("BookViewModel", "ToggleBookmark - UserId: $userId")
                Log.d("BookViewModel", "ToggleBookmark - Current book: $book")

                if (userId != null) {
                    _ebookUiState.update { it.copy(isLoading = true, error = null) }
                    val updatedBook = repository.toggleBookmark(userId, book)
                    Log.d("BookViewModel", "ToggleBookmark - Updated book: $updatedBook")
                    _bookDetails.value = updatedBook

                    // Re-initialize bookmark state with updated bookmarks
                    initializeBookmarkState(updatedBook.bookmarks)
                } else {
                    _ebookUiState.update { it.copy(error = "User not logged in") }
                }
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error toggling bookmark", e)
                _ebookUiState.update { it.copy(error = e.message) }
            } finally {
                _ebookUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setInitialBookmarkState(isBookmarked: Boolean) {
        Log.d("BookViewModel", "Setting initial bookmark state: $isBookmarked")
        _isBookmarked.value = isBookmarked
    }

    fun initializeBookmarkState(bookmarks: List<Bookmark>?) {
        viewModelScope.launch {
            try {
                val userId = TokenManagerProvider.getInstance().userId.first()
                Log.d("BookViewModel", "InitializeBookmarkState - UserId: $userId")
                Log.d("BookViewModel", "InitializeBookmarkState - Bookmarks: ${bookmarks?.size}")
                Log.d("BookViewModel", "InitializeBookmarkState - Raw Bookmarks: $bookmarks")

                if (userId != null) {
                    val hasBookmark = bookmarks?.any { bookmark ->
                        Log.d(
                            "BookViewModel",
                            "Checking bookmark: userId=${bookmark.userId} against currentUser=$userId"
                        )
                        bookmark.userId == userId
                    } == true

                    Log.d("BookViewModel", "Setting isBookmarked to: $hasBookmark")
                    _isBookmarked.value = hasBookmark

                    // Verify the state was updated
                    Log.d("BookViewModel", "Current isBookmarked value: ${_isBookmarked.value}")
                } else {
                    Log.e("BookViewModel", "UserId is null")
                    _isBookmarked.value = false
                }
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error initializing bookmark state", e)
                Log.e("BookViewModel", "Stack trace:", e)
                // Don't reset isBookmarked here to preserve the initial state
            }
        }
    }

    fun addReview(bookId: Int, rating: Int, comment: String) {
        viewModelScope.launch {
            try {
                val userId = TokenManagerProvider.getInstance().userId.first()
                if (userId != null) {
                    _ebookUiState.update { it.copy(isLoading = true, error = null) }
                    val updatedBook = repository.addReview(bookId, userId, rating, comment)
                    _bookDetails.value = updatedBook
                    // Refetch reviews after adding a new one
                    fetchReviews(bookId)
                }
            } catch (e: Exception) {
                _ebookUiState.update { it.copy(error = e.message) }
                Log.e("BookViewModel", "Error adding review", e)
            } finally {
                _ebookUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun deleteReview(bookId: String, reviewId: String) {
        viewModelScope.launch {
            try {
                val userId = TokenManagerProvider.getInstance().userId.first()
                if (userId != null) {
                    _ebookUiState.update { it.copy(isLoading = true, error = null) }
                    val updatedBook = repository.removeReview(userId, bookId, reviewId)
                    _bookDetails.value = updatedBook
                    // Refetch reviews after deleting one
                    fetchReviews(bookId.toInt())
                }
            } catch (e: Exception) {
                _ebookUiState.update { it.copy(error = e.message) }
                Log.e("BookViewModel", "Error deleting review", e)
            } finally {
                _ebookUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun fetchReviews(bookId: Int) {
        viewModelScope.launch {
            try {
                Log.d("BookViewModel", "Fetching reviews for book: $bookId")
                _ebookUiState.update { it.copy(isLoading = true, error = null) }
                val fetchedReviews = repository.getReviews(bookId)
                Log.d("BookViewModel", "Fetched ${fetchedReviews.size} reviews")
                Log.d("BookViewModel", "Reviews: $fetchedReviews")
                _reviews.value = fetchedReviews
                Log.d(
                    "BookViewModel",
                    "Reviews state updated. Current size: ${_reviews.value.size}"
                )
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error fetching reviews", e)
                _ebookUiState.update { it.copy(error = e.message) }
            } finally {
                _ebookUiState.update { it.copy(isLoading = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopAudio()
        audioStreamManager = null
    }
}
