package org.telegram.divo.dal.dto.auth

import com.google.gson.annotations.SerializedName

data class TelegramLinkRequest(
    @SerializedName("divoUserId") val divoUserId: Long? = null,
    @SerializedName("telegramUserId") val telegramUserId: Long,
    @SerializedName("phone") val phone: String,
    // Signature from teamgram (help.getAppConfig "divo_link_proof"), required by the backend
    @SerializedName("proof") val proof: String? = null,
    @SerializedName("deviceId") val deviceId: String? = null,
    @SerializedName("deviceType") val deviceType: String? = "android"
)
