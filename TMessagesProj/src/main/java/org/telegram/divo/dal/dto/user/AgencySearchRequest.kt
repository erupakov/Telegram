package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName

class AgencySearchRequest(
    @SerializedName("name") val name: String? = null,
    @SerializedName("offset") val offset: Int? = null,
    @SerializedName("limit") val limit: Int? = null
)
