package org.telegram.divo.dal.dto.auth

import com.google.gson.annotations.SerializedName

data class RegistrationResponse(
    @SerializedName("status") val status: String,
    @SerializedName("data") val data: RegistrationData?,
    @SerializedName("message") val message: String?
) {
    data class RegistrationData(
        @SerializedName("token") val token: String?,
        @SerializedName("accessToken") val accessToken: String?,
        @SerializedName("status") val status: String?,
        @SerializedName("expiredAt") val expiredAt: String?,
        @SerializedName("user") val user: DivoUser?
    )
    
    data class DivoUser(
        @SerializedName("id") val id: Long?
    )
}
