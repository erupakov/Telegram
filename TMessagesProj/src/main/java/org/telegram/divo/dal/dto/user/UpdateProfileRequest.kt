package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.AgencyDto
import org.telegram.divo.dal.dto.common.CustomerDto
import org.telegram.divo.dal.dto.common.UuidContainerDto
import org.telegram.divo.entity.Appearance
import org.telegram.divo.entity.Model

class UpdateProfileRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("gender") val gender: String?,
    @SerializedName("birthday") val birthday: String,
    @SerializedName("geoCityId") val geoCityId: Int?,
    @SerializedName("measuringSystem") val measuringSystem: String,
    @SerializedName("subrole") val subrole: String?,
    @SerializedName("pushNotifications") val pushNotifications: Boolean,
    @SerializedName("isRegistrationFinished") val isRegistrationFinished: Boolean,
    @SerializedName("photo") val photo: UuidContainerDto?,
    @SerializedName("avatar") val avatar: UuidContainerDto?,
    @SerializedName("model") val model: UpdateProfileModelDto?,
    @SerializedName("agency") val agency: UpdateProfileAgencyRequest?,
    @SerializedName("customer") val customer: CustomerDto?
)

class UpdateProfileModelDto(
    @SerializedName("agencyId") val agencyId: Int?,
    @SerializedName("profileUrl") val profileUrl: String?,
    @SerializedName("education") val education: String?,
    @SerializedName("workExperience") val workExperience: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("languages") val languages: String?,
    @SerializedName("hasInternationalPassport") val hasInternationalPassport: Boolean,
    @SerializedName("hasTattoo") val hasTattoo: Boolean,
    @SerializedName("hasPiercing") val hasPiercing: Boolean,
    @SerializedName("hasActingEducation") val hasActingEducation: Boolean,
    @SerializedName("appearance") val appearance: UpdateProfileAppearanceDto?,
    @SerializedName("tiktokUrl") val tiktokUrl: String?,
    @SerializedName("youtubeUrl") val youtubeUrl: String?,
    @SerializedName("instagramUrl") val instagramUrl: String?,
    @SerializedName("websiteUrl") val websiteUrl: String?,
)

class UpdateProfileAppearanceDto(
    @SerializedName("measuringSystem") val measuringSystem: String?,
    @SerializedName("height") val height: Float?,
    @SerializedName("weight") val weight: Float?,
    @SerializedName("breastSize") val breastSize: String?,
    @SerializedName("waist") val waist: Float?,
    @SerializedName("hips") val hips: Float?,
    @SerializedName("shoesSize") val shoesSize: Float?,
    @SerializedName("hairColor") val hairColor: Int?,
    @SerializedName("hairLength") val hairLength: Int?,
    @SerializedName("eyeColor") val eyeColor: Int?,
    @SerializedName("skinColor") val skinColor: Int?
)

fun Model.toDto(): UpdateProfileModelDto =
    UpdateProfileModelDto(
        agencyId = agency?.id,
        profileUrl = profileUrl,
        education = education,
        workExperience = workExperience,
        languages = languages,
        description = description,
        hasInternationalPassport = hasInternationalPassport,
        hasTattoo = hasTattoo,
        hasPiercing = hasPiercing,
        hasActingEducation = hasActingEducation,
        appearance = appearance?.toDto(),
        tiktokUrl = tiktokUrl,
        youtubeUrl = youtubeUrl,
        websiteUrl = websiteUrl,
        instagramUrl = instagramUrl
    )

fun Appearance.toDto(): UpdateProfileAppearanceDto =
    UpdateProfileAppearanceDto(
        measuringSystem = measuringSystem,
        height = height,
        weight = weight,
        breastSize = breastSize,
        waist = waist,
        hips = hips,
        shoesSize = shoesSize,
        hairColor = hairColor?.id,
        hairLength = hairLength?.id,
        eyeColor = eyeColor?.id,
        skinColor = skinColor?.id
    )