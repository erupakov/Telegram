package org.telegram.divo.entity

data class Feed(
    val items: List<FeedItem> = listOf(),
    val pagination: Pagination? = null
)

data class FeedItem(
    val id: Int,
    val feedId: Int,
    val title: String,
    val description: String,
    val entity: String,
    val type: String,
    val isLiked: Boolean,
    val isFavorite: Boolean,
    val user: User,
    val files: List<FileModel>,
    val previewImage: FileModel
)

data class User(
    val id: Int,
    val fullName: String,
    val role: RoleType,
    val subrole: String?,
    val roleLabel: String,
    val likesCount: Int,
    val viewsCount: Int,
    val followersCount: Int,
    val age: Int?,
    val countryCode: String?,
    val countryName: String?,
)

data class FileModel(
    val order: Int,
    val fullName: String,
    val url: String,
    val extension: String,
    val videoThumbnail: String?,
    val uuid: String
)