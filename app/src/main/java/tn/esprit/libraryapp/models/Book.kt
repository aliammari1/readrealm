package tn.esprit.libraryapp.models

import com.google.gson.annotations.SerializedName

data class Bookmark(
    @SerializedName("_id") val id: String? = null,
    val userId: String,
    @SerializedName("bookId") val bookId: String,
    val dateAdded: String
)


data class Book(
    @SerializedName("_id") val mongoId: String? = null,
    val id: Int,
    val author: String,
    val title: String,
    val publicationDate: Int,
    val numOfPages: Int,
    val coverImage: String? = null,
    val genre: String,
    val link: String,
    val textData: String? = null,
    @SerializedName("bookmarks") val bookmarks: List<Bookmark>? = emptyList(),
    @SerializedName("reviews") val reviews: List<Review>? = emptyList(),
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerializedName("__v") val version: Int = 0
)
