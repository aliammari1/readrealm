package tn.esprit.libraryapp.models

import com.google.gson.annotations.SerializedName

data class Review(
    @SerializedName("_id")
    val id: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("bookId")
    val bookId: Int,

    @SerializedName("rating")
    val rating: Int,

    @SerializedName("comment")
    val comment: String,

    @SerializedName("createdAt")
    val dateAdded: String,
)
