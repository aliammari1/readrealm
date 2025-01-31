package tn.esprit.libraryapp.repository

import tn.esprit.libraryapp.dao.BookProgressDao
import tn.esprit.libraryapp.models.BookProgress

class BookProgressRepository(private val bookProgressDao: BookProgressDao) {
    fun getProgress(url: String) = bookProgressDao.getProgress(url)

    suspend fun updateProgress(url: String, currentPage: Int, totalPages: Int) {
        val progress = (currentPage.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f)
        bookProgressDao.updateProgress(
            url = url,
            page = currentPage,
            progress = progress,
            timestamp = System.currentTimeMillis(),
        )
    }

    suspend fun saveInitialProgress(url: String, totalPages: Int) {
        val progress = BookProgress(
            bookUrl = url,
            lastReadPage = 0,
            totalPages = totalPages,
            readingProgress = 0f,
            lastReadTimestamp = System.currentTimeMillis(),
        )
        bookProgressDao.saveInitialProgress(progress)
    }
}
