package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.CityDto
import org.telegram.divo.dal.dto.common.PhotoDto
import org.telegram.divo.dal.dto.common.toEntity
import org.telegram.divo.entity.AgencyModel
import org.telegram.divo.entity.AgencyModels

class AgencyModelsResponse(
    @SerializedName("data") val data: AgencyModelsDataDto,
)

class AgencyModelsDataDto(
    @SerializedName("items") val items: List<ModelItemDto>,
    // @SerializedName("pagination") val pagination: PaginationDto
)

class ModelItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("birthday") val birthday: String? = null,
    @SerializedName("photo") val photo: PhotoDto,
    @SerializedName("city") val city: CityDto? = null,
    @SerializedName("userId") val userId: Int
)

fun AgencyModelsResponse.toEntities(): AgencyModels =
    AgencyModels(
        items = data.items.map { it.toEntity() },
    )

fun ModelItemDto.toEntity(): AgencyModel =
    AgencyModel(
        id = id,
        name = name,
        birthday = birthday,
        photoUrl = photo.fullUrl,
        city = city?.toEntity(),
        userId = userId
    )