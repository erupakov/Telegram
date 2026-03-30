package org.telegram.divo.dal.dto.work_history

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.WorkExperience

class WorkExperienceResponse(
    @SerializedName("data") val data: WorkExperienceDto? = null,
)

class WorkExperienceDto(
    @SerializedName("id") val id: Int,
    @SerializedName("agencyId") val agencyId: Int? = null,
    @SerializedName("agencyName") val agencyName: String? = null,
    @SerializedName("agencyDisplayName") val agencyDisplayName: String? = null,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String? = null,
    @SerializedName("isCurrent") val isCurrent: Boolean,
)

fun WorkExperienceDto.toEntity() = WorkExperience(
    id = id,
    agencyId = agencyId,
    agencyName = agencyName,
    agencyDisplayName = agencyDisplayName,
    startDate = startDate,
    endDate = endDate,
    isCurrent = isCurrent,
)
