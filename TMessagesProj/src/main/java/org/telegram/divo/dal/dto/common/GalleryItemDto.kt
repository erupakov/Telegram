package org.telegram.divo.dal.dto.common

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.UserGalleryItem

class GalleryItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("photo") val photo: PhotoDto,
    @SerializedName("likesCount") val likesCount: Int,
    @SerializedName("isLikedByUser") val isLikedByUser: Boolean,
    @SerializedName("preview") val preview: PhotoDto,
    // Per-photo views: not sent by the backend yet; null hides the counter
    @SerializedName("viewsCount") val viewsCount: Int? = null,
)

fun GalleryItemDto.toEntity(): UserGalleryItem =
    UserGalleryItem(
        id = id,
        photoUrl = photo.fullUrl.orEmpty(),
        previewUrl = preview.fullUrl.orEmpty(),
        likesCount = likesCount,
        isLikedByUser = isLikedByUser,
        viewsCount = viewsCount,
    )