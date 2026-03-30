package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName

class AgencyModelsRequest(
    @SerializedName("offset") val offset: Int,
    @SerializedName("limit") val limit: Int
)