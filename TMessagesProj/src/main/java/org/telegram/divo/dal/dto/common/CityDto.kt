package org.telegram.divo.dal.dto.common

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.City

class CityDto(
    @SerializedName("id") val id: Int,
    @SerializedName("countryCode") val countryCode: String,
    @SerializedName("countryName") val countryName: String,
    @SerializedName("areaName") val areaName: String,
    @SerializedName("name") val name: String
)

fun CityDto.toEntity(): City =
    City(
        id = id,
        countryCode = countryCode,
        countryName = countryName,
        areaName = areaName,
        name = name
    )

fun City.toDto(): CityDto =
    CityDto(
        id = id,
        countryCode = countryCode,
        countryName = countryName,
        areaName = areaName,
        name = name
    )