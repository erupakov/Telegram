package org.telegram.divo.entity

data class UserGalleryItem(
    val id: Int,
    val photoUrl: String,
    val previewUrl: String,
    val likesCount: Int,
    val isLikedByUser: Boolean
)
