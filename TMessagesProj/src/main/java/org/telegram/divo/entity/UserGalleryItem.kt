package org.telegram.divo.entity

data class UserGalleryList(
    val items: List<UserGalleryItem>,
    val pagination: Pagination? = null
)

data class UserGalleryItem(
    val id: Int,
    val photoUrl: String,
    val previewUrl: String,
    val likesCount: Int,
    val isLikedByUser: Boolean,
)
