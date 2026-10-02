package org.telegram.divo.dal.dto.auth

import com.google.gson.annotations.SerializedName

data class TelegramLinkResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: TelegramLinkData?,
    @SerializedName("message") val message: String?
) {
    data class TelegramLinkData(
        @SerializedName("accessToken") val accessToken: String?,
        @SerializedName("type") val type: String?,
        @SerializedName("user") val user: DivoUser?
    )
    
    data class DivoUser(
        @SerializedName("id") val id: Long?
    )
}
