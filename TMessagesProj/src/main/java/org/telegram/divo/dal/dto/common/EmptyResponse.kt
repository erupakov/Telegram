package org.telegram.divo.dal.dto.common

import com.google.gson.annotations.SerializedName

class EmptyResponse(
    @SerializedName("data") val data: Any? = null,
)
