package org.telegram.divo.dal.dto.auth

import com.google.gson.annotations.SerializedName

/**
 * Response from POST /api/auth/login-social and /api/auth/registration-social.
 */
data class SocialAuthResponse(
    @SerializedName("data") val data: SocialAuthData?,
) {
    data class SocialAuthData(
        @SerializedName("type") val type: String?,
        @SerializedName("accessToken") val accessToken: String?,
        @SerializedName("user") val user: SocialUser?,
        @SerializedName("telegramLinked") val telegramLinked: Boolean?,
    )

    data class SocialUser(
        @SerializedName("id") val id: Long?,
        @SerializedName("telegramLinked") val telegramLinked: Boolean?,
    )
}
