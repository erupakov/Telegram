package org.telegram.divo.dal.dto.auth

import com.google.gson.annotations.SerializedName

/**
 * Request body for POST /api/auth/registration-social
 * Creates a user linked to Firebase UID and returns an access token.
 */
data class SocialRegistrationRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String? = null,
    @SerializedName("role") val role: String,
    @SerializedName("subrole") val subrole: String? = null,
    @SerializedName("providerId") val providerId: String = "google.com",
    @SerializedName("uid") val uid: String,
    @SerializedName("timezone") val timezone: String? = null,
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("deviceType") val deviceType: String = "android",
    @SerializedName("additionalInfo") val additionalInfo: Map<String, Any> = mapOf("additionalProp1" to emptyMap<String, Any>()),
)
