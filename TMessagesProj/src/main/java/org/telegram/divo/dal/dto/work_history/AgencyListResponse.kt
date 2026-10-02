package org.telegram.divo.dal.dto.work_history

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.PaginationDto
import org.telegram.divo.entity.Agency

class AgencyListResponse(
    @SerializedName("data") val data: AgencyListDto
)

class AgencyListDto(
    @SerializedName("items") val items: List<AgencyItemDto>,
    @SerializedName("pagination") val pagination: PaginationDto?
)

class AgencyItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("agencyAvatarLink") val agencyAvatarLink: String? = null,
)

fun AgencyItemDto.toEntity() = Agency(
    id = id, 
    title = title,
    photo = agencyAvatarLink?.let { org.telegram.divo.entity.Photo(photoId = 0L, fullUrl = it) }
)

fun AgencyListResponse.toEntities() = data.items.map { it.toEntity() }