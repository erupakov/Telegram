package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName

class ReportTypesResponse(
    @SerializedName("data") val data: List<ReportTypeDto>
)

class ReportTypeDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String
)
