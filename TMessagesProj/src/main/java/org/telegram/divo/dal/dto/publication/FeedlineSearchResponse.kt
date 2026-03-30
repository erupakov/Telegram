package org.telegram.divo.dal.dto.publication

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.PaginationDto
import org.telegram.divo.dal.dto.common.toEntity
import org.telegram.divo.entity.FeedlineItem
import org.telegram.divo.entity.FeedlineSearchResult
import org.telegram.divo.entity.FeedlineUser

class FeedlineSearchResponse(
    @SerializedName("data") val data: FeedlineSearchDataDto?,
)

class FeedlineSearchDataDto(
    @SerializedName("items") val items: List<FeedlineItemDto>,
    @SerializedName("pagination") val pagination: PaginationDto
)

class FeedlineItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String?,
    @SerializedName("user") val user: FeedlineUserDto?,
    @SerializedName("searchImage") val searchImage: FeedlineFileDto?,
)

class FeedlineUserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("roleLabel") val roleLabel: String?,
)

class FeedlineFileDto(
    @SerializedName("order") val order: Int,
    @SerializedName("fullUrl") val fullUrl: String?,
    @SerializedName("fileUuid") val fileUuid: String?
)

fun FeedlineSearchResponse.toEntities() = FeedlineSearchResult(
    items = data?.items?.map { it.toEntity() } ?: emptyList(),
    pagination = data?.pagination?.meta?.toEntity()
)

fun FeedlineItemDto.toEntity() = FeedlineItem(
    id = id,
    title = title.orEmpty(),
    user = user?.toEntity(),
    searchImageUrl = searchImage?.fullUrl
)

fun FeedlineUserDto.toEntity() = FeedlineUser(
    id = id,
    fullName = fullName.orEmpty(),
    role = role.orEmpty(),
    roleLabel = roleLabel.orEmpty(),
)


