package org.telegram.divo.dal.dto.follower

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.SavedProfile

data class FollowerListResponse(
    @SerializedName("data") val data: FollowerDataDto
)

data class FollowerDataDto(
    @SerializedName("items") val items: List<FollowerItemDto>,
    @SerializedName("pagination") val pagination: FollowerPaginationDto?
)

data class FollowerPaginationDto(
    @SerializedName("meta") val meta: FollowerPaginationMetaDto?
)

data class FollowerPaginationMetaDto(
    @SerializedName("limit") val limit: Int,
    @SerializedName("currentOffset") val currentOffset: Int,
    @SerializedName("totalCount") val totalCount: Int
)

data class FollowerItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("roleLabel") val roleLabel: String?,
    @SerializedName("photo") val photo: FollowerPhotoDto?,
    @SerializedName("avatar") val avatar: FollowerPhotoDto?
)

data class FollowerPhotoDto(
    @SerializedName("photoId") val photoId: Int,
    @SerializedName("fileName") val fileName: String?,
    @SerializedName("fullUrl") val fullUrl: String?,
    @SerializedName("extension") val extension: String?,
    @SerializedName("fileUuid") val fileUuid: String?
)

fun FollowerItemDto.toEntity(): SavedProfile {
    return SavedProfile(
        id = id,
        fullName = fullName.orEmpty(),
        role = role.orEmpty(),
        roleLabel = roleLabel.orEmpty(),
        avatarUrl = avatar?.fullUrl ?: photo?.fullUrl.orEmpty()
    )
}
