package tn.esprit.libraryapp.models

data class Book(
    val id: Int,
    val author: String,
    val title: String,
    val publicationDate: Int,
    val numOfPages: Int,
    val coverImage: String? = null,
    val genre: String    // Keep as String to match the JSON response
)
