package org.telegram.divo.dal.dto.auth

import com.google.gson.annotations.SerializedName

/**
 * Request body for POST /api/auth/login-social
 * Authenticates an existing active user by Firebase UID.
 */
data class SocialLoginRequest(
    @SerializedName("uid") val uid: String,
    @SerializedName("providerId") val providerId: String = "google.com",
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("deviceType") val deviceType: String = "android",
)
