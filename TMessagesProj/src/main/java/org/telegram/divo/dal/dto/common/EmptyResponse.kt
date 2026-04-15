package org.telegram.divo.dal.dto.common

import com.google.gson.annotations.SerializedName

data class EmptyResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: Any? = null,
    @SerializedName("errors") val errors: Any? = null,
)
