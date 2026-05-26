package org.telegram.divo.dal.dto.auth

import com.google.gson.annotations.SerializedName

data class TelegramLinkRequest(
    @SerializedName("divoUserId") val divoUserId: Long? = null,
    @SerializedName("telegramUserId") val telegramUserId: Long,
    @SerializedName("phone") val phone: String,
    @SerializedName("deviceId") val deviceId: String? = null,
    @SerializedName("deviceType") val deviceType: String? = "android"
)
