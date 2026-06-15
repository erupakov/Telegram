package org.telegram.divo.entity

data class SearchedProfile(
    val id: Int,
    val feedId: Int? = null,
    val role: String = "",
    val name: String,
    val age: Int?,
    val country: String?,
    val countryCode: String?,
    val isMarked: Boolean,
    val likes: Int,
    val isLiked: Boolean,
    val followersCount: Int = 0,
    val photo: String,
    val index: Int?,
    val isModel: Boolean,
    val roleLabel: String,
    val similarity: Int?,
)
