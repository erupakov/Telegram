package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName

data class AddChannelRequest(
    @SerializedName("telegramChatId") val telegramChatId: Long,
    @SerializedName("username") val username: String?,
    @SerializedName("inviteLink") val inviteLink: String?
)

data class ChannelListResponse(
    @SerializedName("data") val data: ChannelListData?
)

data class ChannelListData(
    @SerializedName("items") val items: List<ChannelDto>?
)

data class ChannelDto(
    @SerializedName("id") val id: Int,
    @SerializedName("telegramChatId") val telegramChatId: Long,
    @SerializedName("username") val username: String?,
    @SerializedName("inviteLink") val inviteLink: String?
)

fun ChannelDto.toEntity() = org.telegram.divo.entity.UserChannel(
    id = id,
    telegramChatId = telegramChatId,
    username = username,
    inviteLink = inviteLink
)

fun List<ChannelDto>.toEntities() = map { it.toEntity() }
