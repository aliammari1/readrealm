package tn.esprit.libraryapp.models

import androidx.compose.ui.graphics.Color

data class ReadingTheme(
    val name: String,
    val backgroundColor: Color,
    val textColor: Color,
    val accentColor: Color,
    val isDark: Boolean
)

data class BookmarkPage(
    val pageNumber: Int,
    val snippet: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ReadingStatistics(
    val timeSpentReading: Long = 0L,
    val pagesRead: Int = 0,
    val bookmarksCount: Int = 0,
    val averageReadingSpeed: Float = 0f,
    val lastReadTimestamp: Long = System.currentTimeMillis()
)
