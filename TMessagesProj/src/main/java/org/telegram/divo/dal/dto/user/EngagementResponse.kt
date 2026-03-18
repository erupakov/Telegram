package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.PaginationDto
import org.telegram.divo.dal.dto.common.PaginationMetaDto
import org.telegram.divo.entity.Engagement
import org.telegram.divo.entity.EngagementList
import org.telegram.divo.entity.EngagementUser

class EngagementResponse(
    @SerializedName("data")
    val data: EngagementDataDto
)

class EngagementDataDto(
    @SerializedName("liked")
    val liked: EngagementListDto,
    @SerializedName("viewed")
    val viewed: EngagementListDto,
    @SerializedName("followed")
    val followed: EngagementListDto
)

class EngagementListDto(
    @SerializedName("items")
    val items: List<EngagementUserDto>,
    @SerializedName("pagination")
    val pagination: EngagementPaginationDto?
)

class EngagementPaginationDto(
    // для viewed/followed
    @SerializedName("meta")
    val meta: PaginationMetaDto?,
    // для liked
    @SerializedName("total")
    val total: Int?,
    @SerializedName("offset")
    val offset: Int?,
    @SerializedName("limit")
    val limit: Int?
)

class EngagementUserDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("roleLabel")
    val roleLabel: String,
    @SerializedName("photo")
    val photo: EngagementFileDto?,
    @SerializedName("avatar")
    val avatar: EngagementFileDto?
)

class EngagementFileDto(
    @SerializedName("fileName")
    val fileName: String,
    @SerializedName("fullUrl")
    val fullUrl: String,
    @SerializedName("extension")
    val extension: String,
    @SerializedName("fileUuid")
    val fileUuid: String
)

// Mapper
fun EngagementResponse.toEntity(): Engagement =
    Engagement(
        liked = data.liked.toEntity(),
        viewed = data.viewed.toEntity(),
        followed = data.followed.toEntity()
    )

private fun EngagementListDto.toEntity(): EngagementList =
    EngagementList(
        items = items.map { it.toEntity() },
        totalCount = pagination?.meta?.totalCount ?: pagination?.total ?: 0
    )

private fun EngagementUserDto.toEntity(): EngagementUser =
    EngagementUser(
        id = id,
        fullName = fullName,
        role = role,
        roleLabel = roleLabel,
        photoUrl = avatar?.fullUrl ?: photo?.fullUrl.orEmpty()
    )