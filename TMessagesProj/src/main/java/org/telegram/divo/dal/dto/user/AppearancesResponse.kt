package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.AppearanceItem
import org.telegram.divo.entity.Appearances

class AppearancesResponse(
    @SerializedName("data") val data: AppearancesDataDto
)

class AppearancesDataDto(
    @SerializedName("hairLength") val hairLength: List<AppearanceItemDto>?,
    @SerializedName("hairColor") val hairColor: List<AppearanceItemDto>?,
    @SerializedName("eyeColor") val eyeColor: List<AppearanceItemDto>?,
    @SerializedName("skinColor") val skinColor: List<AppearanceItemDto>?
)

data class AppearanceItemDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("title") val title: String?
)

fun AppearancesDataDto.toEntity(): Appearances =
    Appearances(
        hairLength = hairLength?.map { it.toEntity() },
        hairColor = hairColor?.map { it.toEntity() },
        eyeColor = eyeColor?.map { it.toEntity() },
        skinColor = skinColor?.map { it.toEntity() },
    )

private fun AppearanceItemDto.toEntity(): AppearanceItem =
    AppearanceItem(
        id = id,
        title = title
    )