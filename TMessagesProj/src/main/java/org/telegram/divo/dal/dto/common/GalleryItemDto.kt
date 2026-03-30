package org.telegram.divo.dal.dto.common

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.UserGalleryItem

class GalleryItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("photo") val photo: PhotoDto,
    @SerializedName("likesCount") val likesCount: Int,
    @SerializedName("isLikedByUser") val isLikedByUser: Boolean,
    @SerializedName("preview") val preview: PhotoDto
)

fun GalleryItemDto.toEntity(): UserGalleryItem =
    UserGalleryItem(
        id = id,
        photoUrl = photo.fullUrl,
        previewUrl = preview.fullUrl,
        likesCount = likesCount,
        isLikedByUser = isLikedByUser
    )