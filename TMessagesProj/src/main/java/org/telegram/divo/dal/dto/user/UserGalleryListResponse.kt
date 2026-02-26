package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.PaginationDto
import org.telegram.divo.entity.UserGalleryItem

class UserGalleryListResponse(
    @SerializedName("data") val data: UserGalleryListData? = null,
)

class UserGalleryListData(
    @SerializedName("items") val items: List<GalleryItem> = emptyList(),
    @SerializedName("pagination") val pagination: PaginationDto
)

class GalleryItem(
    @SerializedName("id") val id: Int,
    @SerializedName("photo") val photo: MediaFileDto,
    @SerializedName("likesCount") val likesCount: Int,
    @SerializedName("isLikedByUser") val isLikedByUser: Boolean,
    @SerializedName("preview") val preview: MediaFileDto
)

class MediaFileDto(
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fullUrl") val fullUrl: String,
    @SerializedName("extension") val extension: String,
    @SerializedName("fileUuid") val fileUuid: String
)

fun GalleryItem.toEntity(): UserGalleryItem =
    UserGalleryItem(
        id = id,
        photoUrl = photo.fullUrl,
        previewUrl = preview.fullUrl,
        likesCount = likesCount,
        isLikedByUser = isLikedByUser
    )

fun UserGalleryListResponse.toEntities(): List<UserGalleryItem> =
    data?.items?.map { it.toEntity() } ?: emptyList()