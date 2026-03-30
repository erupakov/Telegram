package org.telegram.divo.dal.dto.work_history

import com.google.gson.annotations.SerializedName

class WorkHistoryResponse(
    @SerializedName("data") val data: WorkHistoryData? = null,
)

class WorkHistoryData(
    @SerializedName("items") val items: List<WorkExperienceDto> = emptyList(),
)