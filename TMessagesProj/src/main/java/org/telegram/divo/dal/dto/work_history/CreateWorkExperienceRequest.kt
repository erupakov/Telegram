package org.telegram.divo.dal.dto.work_history

import com.google.gson.annotations.SerializedName

class CreateWorkExperienceRequest(
    @SerializedName("agencyId") val agencyId: Int? = null,
    @SerializedName("agencyName") val agencyName: String? = null,
    @SerializedName("startDate") val startDate: String,
    @SerializedName("endDate") val endDate: String? = null,
    @SerializedName("isCurrent") val isCurrent: Boolean,
)