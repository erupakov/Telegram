package org.telegram.divo.dal.dto.publication

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.PaginationDto
import org.telegram.divo.dal.dto.common.toEntity
import org.telegram.divo.entity.Feed
import org.telegram.divo.entity.FeedItem
import org.telegram.divo.entity.FileModel
import org.telegram.divo.entity.User

class FeedResponse(
    @SerializedName("data")
    val data: FeedDataDto
)

class FeedDataDto(
    @SerializedName("items")
    val items: List<FeedItemDto>,
    @SerializedName("pagination")
    val pagination: PaginationDto
)

class FeedItemDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("feedId")
    val feedId: Int,
    @SerializedName("title")
    val title: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("entity")
    val entity: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("likesCount")
    val likesCount: Int,
    @SerializedName("isLikedByUser")
    val isLikedByUser: Boolean,
    @SerializedName("isFavoriteByUser")
    val isFavoriteByUser: Boolean,
    @SerializedName("user")
    val user: UserDto,
    @SerializedName("files")
    val files: List<FileDto>,
    @SerializedName("searchImage")
    val searchImage: FileDto,
    @SerializedName("additionalData")
    val additionalData: JsonElement?
)

class UserDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("subrole")
    val subrole: String?,
    @SerializedName("roleLabel")
    val roleLabel: String
)

class FileDto(
    @SerializedName("order")
    val order: Int,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("fullUrl")
    val fullUrl: String,
    @SerializedName("extension")
    val extension: String,
    @SerializedName("videoThumbnail")
    val videoThumbnail: String?,
    @SerializedName("fileUuid")
    val fileUuid: String
)

fun FeedResponse.toEntity(): Feed =
    Feed(
        items = data.items.map { it.toEntity() },
        pagination = data.pagination.meta.toEntity()
    )

private fun FeedItemDto.toEntity(): FeedItem =
    FeedItem(
        id = id,
        feedId = feedId,
        title = title.orEmpty(),
        description = description.orEmpty(),
        entity = entity,
        type = type,
        likesCount = likesCount,
        isLiked = isLikedByUser,
        isFavorite = isFavoriteByUser,
        user = user.toEntity(),
        files = files.map { it.toEntity() },
        previewImage = searchImage.toEntity()
    )

private fun UserDto.toEntity(): User =
    User(
        id = id,
        fullName = fullName,
        role = role,
        subrole = subrole,
        roleLabel = roleLabel
    )

private fun FileDto.toEntity(): FileModel =
    FileModel(
        order = order,
        fullName = fullName,
        url = fullUrl,
        extension = extension,
        videoThumbnail = videoThumbnail,
        uuid = fileUuid
    )