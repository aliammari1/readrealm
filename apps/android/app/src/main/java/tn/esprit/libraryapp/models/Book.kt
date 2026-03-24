package tn.esprit.libraryapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

data class BookmarkBook(
    val userId: String,
    val dateAdded: String,
    @SerializedName("_id") val id: String? = null, // Optional as it might not always be present
)

data class Book(
    val id: Int,
    val author: String,
    val title: String,
    val publicationYear: Int,
    val numOfPages: Int,
    val coverImage: String? = null,
    val genre: String,
    val link: String,
    val description: String? = null,
    val textData: String? = null,
    val bookmarks: List<BookmarkBook>? = emptyList(),
    val reviews: List<String>? = emptyList(),
    @SerializedName("_id") val mongoId: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val averageRating: Float? = null,
    val totalReviews: Int? = null,
)

@Entity(tableName = "book_progress")
data class BookProgress(
    @PrimaryKey val bookUrl: String,
    val lastReadPage: Int = 0,
    val totalPages: Int = 0,
    val readingProgress: Float = 0f,
    val lastReadTimestamp: Long = System.currentTimeMillis(),
)
