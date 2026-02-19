package org.telegram.divo.dal.dto.auth

import com.google.gson.annotations.SerializedName

data class RegistrationRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("role") val role: String
)

