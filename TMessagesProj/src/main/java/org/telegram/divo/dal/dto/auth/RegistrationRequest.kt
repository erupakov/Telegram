package org.telegram.divo.dal.dto.auth

import com.google.gson.annotations.SerializedName

data class RegistrationRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String? = null,
    @SerializedName("role") val role: String,
    @SerializedName("subrole") val subrole: String? = null,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("deviceType") val deviceType: String,
    @SerializedName("additionalInfo") val additionalInfo: Map<String, Any> = mapOf("additionalProp1" to emptyMap<String, Any>()),
)

