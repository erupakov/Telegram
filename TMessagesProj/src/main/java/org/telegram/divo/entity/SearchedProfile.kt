package org.telegram.divo.entity

data class SearchedProfile(
    val id: Int,
    val name: String,
    val age: String,
    val country: String,
    val countryCode: String,
    val isMarked: Boolean,
    val likes: Int,
    val isLiked: Boolean,
    val photo: String,
    val roleLabel: String,
    val similarity: Int?
)
