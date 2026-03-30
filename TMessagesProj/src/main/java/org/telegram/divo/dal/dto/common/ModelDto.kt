package org.telegram.divo.dal.dto.common

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.Appearance
import org.telegram.divo.entity.EyeColor
import org.telegram.divo.entity.HairColor
import org.telegram.divo.entity.HairLength
import org.telegram.divo.entity.Model
import org.telegram.divo.entity.SkinColor

class ModelDto(
    @SerializedName("agency") val agency: AgencyDto?,
    @SerializedName("education") val education: String?,
    @SerializedName("workExperience") val workExperience: String?,
    @SerializedName("languages") val languages: String,
    @SerializedName("description") val description: String?,
    @SerializedName("profileUrl") val profileUrl: String?,
    @SerializedName("additionalInformation") val additionalInformation: String,
    @SerializedName("hasInternationalPassport") val hasInternationalPassport: Boolean,
    @SerializedName("hasTattoo") val hasTattoo: Boolean,
    @SerializedName("hasPiercing") val hasPiercing: Boolean,
    @SerializedName("hasActingEducation") val hasActingEducation: Boolean,
    @SerializedName("appearance") val appearance: AppearanceDto?,
    @SerializedName("tiktokUrl") val tiktokUrl: String?,
    @SerializedName("youtubeUrl") val youtubeUrl: String?,
    @SerializedName("instagramUrl") val instagramUrl: String?,
    @SerializedName("websiteUrl") val websiteUrl: String?,
)

class AppearanceDto(
    @SerializedName("measuringSystem") val measuringSystem: String?,
    @SerializedName("height") val height: Float?,
    @SerializedName("weight") val weight: Float?,
    @SerializedName("breastSize") val breastSize: String?,
    @SerializedName("waist") val waist: Float?,
    @SerializedName("hips") val hips: Float?,
    @SerializedName("shoesSize") val shoesSize: Float?,
    @SerializedName("hairColor") val hairColor: HairColorDto?,
    @SerializedName("hairLength") val hairLength: HairLengthDto?,
    @SerializedName("eyeColor") val eyeColor: EyeColorDto?,
    @SerializedName("skinColor") val skinColor: SkinColorDto?
)

class HairColorDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("title") val title: String?
)

class HairLengthDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("title") val title: String?
)

class EyeColorDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("title") val title: String?
)

class SkinColorDto(
    @SerializedName("id") val id: Int?,
    @SerializedName("title") val title: String?
)

fun ModelDto.toEntity(): Model =
    Model(
        agency = agency?.toEntity(),
        education = education.orEmpty(),
        workExperience = workExperience.orEmpty(),
        languages = languages,
        description = description.orEmpty(),
        profileUrl = profileUrl.orEmpty(),
        additionalInformation = additionalInformation,
        hasInternationalPassport = hasInternationalPassport,
        hasTattoo = hasTattoo,
        hasPiercing = hasPiercing,
        hasActingEducation = hasActingEducation,
        appearance = appearance?.toEntity(),
        tiktokUrl = tiktokUrl,
        youtubeUrl = youtubeUrl,
        instagramUrl = instagramUrl,
        websiteUrl = websiteUrl
    )

private fun AppearanceDto.toEntity(): Appearance =
    Appearance(
        measuringSystem = measuringSystem,
        height = height,
        weight = weight,
        breastSize = breastSize,
        waist = waist,
        hips = hips,
        shoesSize = shoesSize,
        hairColor = hairColor?.toEntity(),
        hairLength = hairLength?.toEntity(),
        eyeColor = eyeColor?.toEntity(),
        skinColor = skinColor?.toEntity()
    )

private fun HairColorDto.toEntity(): HairColor =
    HairColor(
        id = id,
        title = title
    )

private fun HairLengthDto.toEntity(): HairLength =
    HairLength(
        id = id,
        title = title
    )

private fun EyeColorDto.toEntity(): EyeColor =
    EyeColor(
        id = id,
        title = title
    )

private fun SkinColorDto.toEntity(): SkinColor =
    SkinColor(
        id = id,
        title = title
    )

private fun HairColor.toDto(): HairColorDto =
    HairColorDto(
        id = id,
        title = title
    )

private fun HairLength.toDto(): HairLengthDto =
    HairLengthDto(
        id = id,
        title = title
    )

private fun EyeColor.toDto(): EyeColorDto =
    EyeColorDto(
        id = id,
        title = title
    )

private fun SkinColor.toDto(): SkinColorDto =
    SkinColorDto(
        id = id,
        title = title
    )