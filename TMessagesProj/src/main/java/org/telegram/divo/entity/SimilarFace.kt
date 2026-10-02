package org.telegram.divo.entity

data class SimilarFace(
    val birthday: String,
    val countryCode: String,
    val countryName: String,
    val fullName: String,
    val image: String,
    val index: Int,
    val rank: Int,
    val role: String,
    val score: Double,
    val userId: Int,
    val followersCount: Int = 0,
    val isFollowedByUser: Boolean = false,
    val likedCount: Int = 0,
    val isLikedByUser: Boolean = false
)
