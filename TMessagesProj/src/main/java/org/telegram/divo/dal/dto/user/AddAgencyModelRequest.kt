package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName

class AddAgencyModelRequest(
    @SerializedName("note") val note: String? = null
)
