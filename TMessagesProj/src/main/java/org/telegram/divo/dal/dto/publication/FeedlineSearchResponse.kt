package org.telegram.divo.dal.dto.publication

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.CityDto
import org.telegram.divo.dal.dto.common.PaginationDto
import org.telegram.divo.dal.dto.common.toEntity
import org.telegram.divo.entity.EmojiCounts
import org.telegram.divo.entity.FeedlineItem
import org.telegram.divo.entity.FeedlineSearchResult
import org.telegram.divo.entity.FeedlineUser
import org.telegram.divo.entity.ModelParameters
import org.telegram.divo.entity.RangeParam

class FeedlineSearchResponse(
    @SerializedName("data") val data: FeedlineSearchDataDto?,
)

class FeedlineSearchDataDto(
    @SerializedName("items") val items: List<FeedlineItemDto>,
    @SerializedName("pagination") val pagination: PaginationDto?
)

class FeedlineItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("feedId") val feedId: Int?,
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("entity") val entity: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("likesCount") val likesCount: Int?,
    @SerializedName("isLikedByUser") val isLikedByUser: Boolean?,
    @SerializedName("isFavoriteByUser") val isFavoriteByUser: Boolean?,
    @SerializedName("user") val user: FeedlineUserDto?,
    @SerializedName("files") val files: List<FeedlineFileDto>?,
    @SerializedName("searchImage") val searchImage: FeedlineFileDto?,
)

class FeedlineUserDto(
    @SerializedName("id") val id: Int,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("roleLabel") val roleLabel: String?,
    @SerializedName("city") val city: CityDto?,
    @SerializedName("age") val age: Int?,
    @SerializedName("height") val height: Float?,
    @SerializedName("weight") val weight: Float?,
    @SerializedName("emojiCounts") val emojiCounts: EmojiCountsDto?,
    @SerializedName("totalEmojisCount") val totalEmojisCount: Int?,
)

class FeedlineFileDto(
    @SerializedName("order") val order: Int,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("fullUrl") val fullUrl: String?,
    @SerializedName("extension") val extension: String?,
    @SerializedName("videoThumbnail") val videoThumbnail: String?,
    @SerializedName("fileUuid") val fileUuid: String?
)

class EmojiCountsDto(
    @SerializedName("thumbsUp") val thumbsUp: Int?,
    @SerializedName("thumbsDown") val thumbsDown: Int?,
    @SerializedName("heart") val heart: Int?,
    @SerializedName("fire") val fire: Int?,
)

fun FeedlineSearchResponse.toEntities() = FeedlineSearchResult(
    items = data?.items?.map { it.toEntity() } ?: emptyList(),
    pagination = data?.pagination?.meta?.toEntity()
)

fun FeedlineItemDto.toEntity() = FeedlineItem(
    id = id,
    feedId = feedId,
    title = title.orEmpty(),
    description = description,
    type = type,
    likesCount = likesCount ?: 0,
    isLikedByUser = isLikedByUser ?: false,
    isFavoriteByUser = isFavoriteByUser ?: false,
    user = user?.toEntity(),
    files = files?.mapNotNull { it.fullUrl } ?: emptyList(),
    searchImageUrl = searchImage?.fullUrl
)

fun FeedlineUserDto.toEntity() = FeedlineUser(
    id = id,
    fullName = fullName.orEmpty(),
    role = role.orEmpty(),
    roleLabel = roleLabel.orEmpty(),
    city = city?.toEntity(),
    age = age,
    height = height,
    weight = weight,
    emojiCounts = emojiCounts?.toEntity(),
    totalEmojisCount = totalEmojisCount ?: 0,
)

fun ModelParametersDto.toEntity() = ModelParameters(
    gender = gender,
    geoCityId = geoCityId,
    age = age?.toEntity(),
    weight = weight?.toEntity(),
    height = height?.toEntity(),
    breastSize = breastSize?.toEntity(),
    waist = waist?.toEntity(),
    shoesSize = shoesSize?.toEntity(),
    hips = hips?.toEntity(),
    eyeColor = eyeColor,
    skinColor = skinColor,
    hairColor = hairColor,
    hairLength = hairLength
)

fun EmojiCountsDto.toEntity() = EmojiCounts(
    thumbsUp = thumbsUp ?: 0,
    thumbsDown = thumbsDown ?: 0,
    heart = heart ?: 0,
    fire = fire ?: 0,
)

fun RangeParamDto.toEntity() = RangeParam(from = from, to = to)


