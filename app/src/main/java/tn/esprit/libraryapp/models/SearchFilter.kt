package tn.esprit.libraryapp.models

import tn.esprit.libraryapp.enums.Genre

data class SearchFilter(
    val genre: Genre? = null,
    val author: String? = null,
    val year: Int? = null,
    val sortBy: SortOption = SortOption.RELEVANCE
)

enum class SortOption(val displayName: String) {
    RELEVANCE("Most Relevant"),
    TITLE_ASC("Title A-Z"),
    TITLE_DESC("Title Z-A"),
    AUTHOR_ASC("Author A-Z"),
    AUTHOR_DESC("Author Z-A"),
    YEAR_NEW("Newest First"),
    YEAR_OLD("Oldest First")
}

data class SearchHistory(
    val query: String,
    val timestamp: Long = System.currentTimeMillis()
)
