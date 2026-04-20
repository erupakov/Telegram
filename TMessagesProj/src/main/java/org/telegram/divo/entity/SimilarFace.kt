package org.telegram.divo.entity

data class SimilarFace(
    val city: String,
    val fullName: String,
    val gender: String,
    val image: String,
    val index: Int,
    val photoId: Int,
    val rank: Int,
    val rating: String,
    val score: Double,
    val userId: Int
)
