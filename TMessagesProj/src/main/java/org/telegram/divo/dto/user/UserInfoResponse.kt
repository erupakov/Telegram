package org.telegram.divo.dto.user

import com.google.gson.annotations.SerializedName

/**
 * Minimal representation of the current user as returned by /user/info.
 * Additional fields from the backend will be ignored by Gson.
 */
data class UserInfoResponse(
    @SerializedName("id") val id: Long?,
    @SerializedName("email") val email: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("rating") val rating: Double?
)

