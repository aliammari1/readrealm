package tn.esprit.libraryapp.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tn.esprit.libraryapp.models.BookProgress


@Dao
interface BookProgressDao {
    @Query("SELECT * FROM book_progress WHERE bookUrl = :url")
    fun getProgress(url: String): Flow<BookProgress?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: BookProgress)

    @Query(
        "UPDATE book_progress SET lastReadPage = :page, readingProgress = :progress, lastReadTimestamp = :timestamp WHERE bookUrl = :url"
    )
    suspend fun updateProgress(url: String, page: Int, progress: Float, timestamp: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveInitialProgress(progress: BookProgress)
}