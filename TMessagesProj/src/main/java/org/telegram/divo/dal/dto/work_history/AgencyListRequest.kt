package org.telegram.divo.dal.dto.work_history

import com.google.gson.annotations.SerializedName

class AgencyListRequest(
    @SerializedName("offset") val offset: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("title") val title: String = "",
)