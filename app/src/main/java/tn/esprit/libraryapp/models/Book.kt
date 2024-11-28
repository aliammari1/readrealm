package tn.esprit.libraryapp.models

import tn.esprit.libraryapp.enums.Genre

data class Book(
    val author: String,
    val title: String,
    val publicationDate: String,
    val numOfPages: Int,
    val coverImage: String,
    val genre: String
)
