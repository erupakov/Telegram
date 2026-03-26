package org.telegram.divo.entity

import android.graphics.Bitmap

data class PublicationList(
    val items: List<Publication> = listOf(),
    val pagination: Pagination? = null
)

data class Publication(
    val id: Int,
    val title: String?,
    val description: String?,
    val type: String,
    val likesCount: Int,
    val isLikedByUser: Boolean,
    val files: List<PublicationFile>
)

data class PublicationFile(
    val order: Int,
    val fileName: String,
    val fullUrl: String,
    val fileUuid: String,
    val extension: String,
    val description: String?,
    val thumbnailBitmap: Bitmap? = null
) {
    val isVideo: Boolean get() = extension == "mp4"
    val isImage: Boolean get() = extension in listOf("jpg", "jpeg", "png", "webp")
}