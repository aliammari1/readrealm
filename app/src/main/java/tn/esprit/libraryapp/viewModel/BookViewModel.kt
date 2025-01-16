package tn.esprit.libraryapp.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tn.esprit.libraryapp.enums.Genre
import tn.esprit.libraryapp.models.Book
import tn.esprit.libraryapp.models.BookmarkBook
import tn.esprit.libraryapp.models.Review
import tn.esprit.libraryapp.models.SearchFilter
import tn.esprit.libraryapp.models.SearchHistory
import tn.esprit.libraryapp.models.SortOption
import tn.esprit.libraryapp.repository.BookRepository
import tn.esprit.libraryapp.services.AudioStreamManager
import tn.esprit.libraryapp.services.TokenManagerProvider

class BookViewModel() : ViewModel() {
    private val repository = BookRepository()
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books

    private val _bookmarks = MutableStateFlow<List<Book>>(emptyList())
    val bookmarks: StateFlow<List<Book>> = _bookmarks.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _bookDetails = MutableStateFlow<Book?>(null)
    val bookDetails: StateFlow<Book?> = _bookDetails

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _searchResults = MutableStateFlow<List<Book>>(emptyList())
    val searchResults: StateFlow<List<Book>> = _searchResults

    private val _isPlaying = MutableStateFlow<Boolean>(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _isBookmarked = MutableStateFlow<Boolean>(false)
    val isBookmarked: StateFlow<Boolean>
        get() = _isBookmarked

    private var audioStreamManager: AudioStreamManager? = null

    private val _booksByGenre = MutableStateFlow<Map<Genre, List<Book>>>(emptyMap())
    val booksByGenre: StateFlow<Map<Genre, List<Book>>> = _booksByGenre

    private val _loadedGenres = MutableStateFlow(setOf<Genre>())
    val loadedGenres: StateFlow<Set<Genre>> = _loadedGenres

    private var currentJob: Job? = null

    private val _initialLoadDone = MutableStateFlow(false)
    val initialLoadDone = _initialLoadDone.asStateFlow()

    private val _searchFilters = MutableStateFlow(SearchFilter())
    val searchFilters = _searchFilters.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<SearchHistory>>(emptyList())
    val searchHistory = _searchHistory.asStateFlow()

    private val maxHistoryItems = 10

    // Add new state for error handling
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

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

    fun setLoading(loading: Boolean) {
        _isLoading.value = loading
    }

    fun addBookToGenre(genre: Genre, book: Book) {
        viewModelScope.launch {
            val currentBooks = _booksByGenre.value.toMutableMap()
            val genreBooks = currentBooks[genre]?.toMutableList() ?: mutableListOf()
            if (!genreBooks.any { it.id == book.id }) {
                genreBooks.add(book)
                currentBooks[genre] = genreBooks
                _booksByGenre.value = currentBooks

                // Update the main books list for compatibility
                val allBooks = _books.value.toMutableList()
                if (!allBooks.any { it.id == book.id }) {
                    allBooks.add(book)
                    _books.value = allBooks
                }

                // Mark this genre as loaded if it has at least one book
                if (genreBooks.isNotEmpty() && !_loadedGenres.value.contains(genre)) {
                    _loadedGenres.value = _loadedGenres.value + genre
                }

                Log.d("BookViewModel", "Added book ${book.title} to genre ${genre.value}")
                Log.d("BookViewModel", "Loaded genres: ${_loadedGenres.value}")
            }
        }
    }

    fun initializeBooks() {
        // Cancel any existing job
        currentJob?.cancel()
        _booksByGenre.value = emptyMap()
        _books.value = emptyList()
        _loadedGenres.value = emptySet()
        setLoading(true)
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query

        viewModelScope.launch {
            try {
                if (query.isEmpty()) {
                    _searchResults.value = emptyList()
                    return@launch
                }

                _isLoading.value = true

                // First get all books if we don't have them
                val allBooks = _books.value

                // Perform search locally first for immediate feedback
                val localResults =
                    allBooks.filter { book ->
                        book.title.contains(query, ignoreCase = true) ||
                            book.author.contains(query, ignoreCase = true)
                    }

                _searchResults.value = localResults

                // Then fetch from repository
                val filters = _searchFilters.value
                val results = repository.searchBooks(query)

                // Apply filters
                val filteredResults =
                    results.filter { book ->
                        var matches = true
                        filters.genre?.let {
                            matches = (matches && book.genre.equals(it.value, true))
                        }
                        filters.author?.let {
                            matches = (matches && book.author.contains(it, true))
                        }
                        filters.year?.let { matches = (matches && book.publicationYear == it) }
                        matches
                    }

                // Apply sorting
                val sortedResults =
                    when (filters.sortBy) {
                        SortOption.TITLE_ASC -> filteredResults.sortedBy { it.title }
                        SortOption.TITLE_DESC -> filteredResults.sortedByDescending { it.title }
                        SortOption.AUTHOR_ASC -> filteredResults.sortedBy { it.author }
                        SortOption.AUTHOR_DESC ->
                            filteredResults.sortedByDescending { it.author }
                        SortOption.YEAR_NEW ->
                            filteredResults.sortedByDescending { it.publicationYear }
                        SortOption.YEAR_OLD -> filteredResults.sortedBy { it.publicationYear }
                        else -> filteredResults
                    }

                _searchResults.value = sortedResults

                // Add to search history if query is substantial
                if (query.length >= 3) {
                    addToSearchHistory(query)
                }
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error searching books", e)
                // Keep the local results if network request fails
                if (_searchResults.value.isEmpty()) {
                    _searchResults.value = emptyList()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun stopAudio() {
        audioStreamManager?.stop()
        _isPlaying.value = false
    }

    fun toggleBookmark(book: Book) {
        viewModelScope.launch {
            try {
                val userId = TokenManagerProvider.getInstance().userId.first()
                Log.d("BookViewModel", "ToggleBookmark - UserId: $userId")
                Log.d("BookViewModel", "ToggleBookmark - Current book: $book")

                if (userId != null) {
                    val updatedBook = repository.toggleBookmark(userId, book)
                    Log.d("BookViewModel", "ToggleBookmark - Updated book: $updatedBook")
                    _bookDetails.value = updatedBook
                    _isBookmarked.value = updatedBook.bookmarks?.any { it.userId == userId } == true
                }
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error toggling bookmark", e)
            }
        }
    }

    fun setInitialBook(bookId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val book = repository.getBookDetails(bookId)
                _bookDetails.value = book
                // Initialize bookmark state
                initializeBookmarkState(book.bookmarks)
                // Also fetch reviews since they're now just IDs
                fetchReviews(bookId)
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error setting initial book", e)
                _error.value = e.message ?: "Unknown error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun initializeBookmarkState(bookmarks: List<BookmarkBook>?) {
        viewModelScope.launch {
            try {
                val userId = TokenManagerProvider.getInstance().userId.first()
                Log.d("BookViewModel", "InitializeBookmarkState - UserId: $userId")
                Log.d("BookViewModel", "InitializeBookmarkState - Bookmarks: ${bookmarks?.size}")
                Log.d("BookViewModel", "InitializeBookmarkState - Raw Bookmarks: $bookmarks")

                if (userId != null) {
                    val hasBookmark =
                        bookmarks?.any { bookmark ->
                            Log.d(
                                "BookViewModel",
                                "Checking bookmark: userId=${bookmark.userId} against currentUser=$userId",
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

    fun addReview(bookId: Int, rating: Int, comment: String?) {
        viewModelScope.launch {
            try {
                val userId = TokenManagerProvider.getInstance().userId.first()
                if (userId != null) {
                    // Ensure comment is never null by using empty string as fallback
                    val safeComment = comment ?: ""
                    val updatedBook = repository.addReview(bookId, userId, rating, safeComment)
                    _bookDetails.value = updatedBook
                    fetchReviews(bookId)
                }
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error adding review", e)
                _error.value = e.message ?: "Error adding review"
            }
        }
    }

    fun deleteReview(bookId: String, reviewId: String) {
        viewModelScope.launch {
            try {
                val userId = TokenManagerProvider.getInstance().userId.first()
                if (userId != null) {
                    val updatedBook = repository.removeReview(userId, bookId, reviewId)
                    _bookDetails.value = updatedBook
                    // Refetch reviews after deleting one
                    fetchReviews(bookId.toInt())
                }
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error deleting review", e)
            }
        }
    }

    fun fetchReviews(bookId: Int) {
        viewModelScope.launch {
            try {
                Log.d("BookViewModel", "Fetching reviews for book: $bookId")
                val fetchedReviews = repository.getReviews(bookId)
                Log.d("BookViewModel", "Fetched ${fetchedReviews.size} reviews")
                Log.d("BookViewModel", "Reviews: $fetchedReviews")
                _reviews.value = fetchedReviews
                Log.d(
                    "BookViewModel",
                    "Reviews state updated. Current size: ${_reviews.value.size}",
                )
            } catch (e: Exception) {
                Log.e("BookViewModel", "Error fetching reviews", e)
            }
        }
    }

    fun setInitialLoadDone() {
        _initialLoadDone.value = true
    }

    fun updateSearchFilters(filter: SearchFilter) {
        _searchFilters.value = filter
        // Rerun search with new filters
        onSearchQueryChange(_searchQuery.value)
    }

    fun addToSearchHistory(query: String) {
        viewModelScope.launch {
            val currentHistory = _searchHistory.value.toMutableList()
            // Remove if exists to avoid duplicates
            currentHistory.removeAll { it.query == query }
            // Add new query
            currentHistory.add(0, SearchHistory(query))
            // Keep only last N items
            _searchHistory.value = currentHistory.take(maxHistoryItems)
        }
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
    }

    fun removeFromSearchHistory(query: String) {
        _searchHistory.value = _searchHistory.value.filter { it.query != query }
    }

    override fun onCleared() {
        super.onCleared()
        currentJob?.cancel()
        stopAudio()
        audioStreamManager = null
    }
}
