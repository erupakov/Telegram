package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.CityDto
import org.telegram.divo.dal.dto.common.PhotoDto
import org.telegram.divo.dal.dto.common.toEntity

class AgencySearchResponse(
    @SerializedName("data") val data: AgencySearchDataDto?
)

class AgencySearchDataDto(
    @SerializedName("items") val items: List<AgencySearchItemDto>?,
    @SerializedName("pagination") val pagination: org.telegram.divo.dal.dto.common.PaginationDto?
)

class AgencySearchItemDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("birthday") val birthday: String?,
    @SerializedName("photo") val photo: PhotoDto?,
    @SerializedName("city") val city: CityDto?,
    @SerializedName("userId") val userId: Int,
    @SerializedName("role") val role: String?,
    @SerializedName("isPremium") val isPremium: Boolean?,
    @SerializedName("currentAgency") val currentAgency: CurrentAgencyDto?
)

class CurrentAgencyDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val name: String?
)

fun AgencySearchItemDto.toEntity(currentAgencyId: Int? = null): org.telegram.divo.entity.AgencySearchModel {
    val status = when {
        // We might not get "Request Pending" or "Already Added" directly if the backend filters them,
        // but let's assume we can derive it. For now:
        currentAgency != null && currentAgency.id != currentAgencyId -> 
            org.telegram.divo.entity.AgencySearchModelStatus.RepresentedByOther(currentAgency.name)
        currentAgency != null && currentAgency.id == currentAgencyId -> 
            org.telegram.divo.entity.AgencySearchModelStatus.AlreadyAdded
        else -> org.telegram.divo.entity.AgencySearchModelStatus.Available
    }

    return org.telegram.divo.entity.AgencySearchModel(
        id = id,
        userId = userId,
        name = name,
        username = null, // Backend doesn't return this yet
        photoUrl = photo?.fullUrl.orEmpty(),
        city = city?.toEntity(),
        role = role ?: "Model",
        isPremium = isPremium ?: false,
        birthday = birthday,
        status = status
    )
}

fun AgencySearchResponse.toEntities(currentAgencyId: Int? = null): List<org.telegram.divo.entity.AgencySearchModel> {
    return data?.items?.map { it.toEntity(currentAgencyId) } ?: emptyList()
}
