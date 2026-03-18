package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.UuidContainerDto
import org.telegram.divo.entity.Appearance
import org.telegram.divo.entity.Customer
import org.telegram.divo.entity.Model
import org.telegram.divo.entity.UserInfo

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
    @SerializedName("customer") val customer: UpdateProfileCustomerDto?
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
    @SerializedName("measuringSystem") val measuringSystem: String,
    @SerializedName("height") val height: Double?,
    @SerializedName("weight") val weight: Double?,
    @SerializedName("breastSize") val breastSize: String?,
    @SerializedName("waist") val waist: Double?,
    @SerializedName("hips") val hips: Double?,
    @SerializedName("shoesSize") val shoesSize: Double?,
    @SerializedName("hairColor") val hairColor: Int?,
    @SerializedName("hairLength") val hairLength: Int?,
    @SerializedName("eyeColor") val eyeColor: Int?,
    @SerializedName("skinColor") val skinColor: Int?
)

class UpdateProfileCustomerDto(
    @SerializedName("site") val site: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("background") val background: UuidContainerDto?
)

fun UserInfo.toDto(
    timezone: String,
    geoCityId: Int?,
    photoUuid: String?,
    avatarUuid: String?,
    customer: UpdateProfileCustomerDto? = null
): UpdateProfileRequest =
    UpdateProfileRequest(
        fullName = fullName,
        phone = phone,
        timezone = timezone,
        gender = gender?.id,
        birthday = birthday,
        geoCityId = geoCityId,
        measuringSystem = measuringSystem,
        subrole = subrole,
        pushNotifications = pushNotifications,
        isRegistrationFinished = isRegistrationFinished,
        photo = photoUuid?.let { UuidContainerDto(it) },
        avatar = avatarUuid?.let { UuidContainerDto(it) },
        model = model?.toDto(),
        customer = customer
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
        height = height.toDouble(),
        weight = weight.toDouble(),
        breastSize = breastSize,
        waist = waist.toDouble(),
        hips = hips.toDouble(),
        shoesSize = shoesSize.toDouble(),
        hairColor = hairColor.id,
        hairLength = hairLength.id,
        eyeColor = eyeColor.id,
        skinColor = skinColor.id
    )

fun Customer.toDto(): UpdateProfileCustomerDto =
    UpdateProfileCustomerDto(
        site = site,
        description = description,
        background = UuidContainerDto(backgroundUuid.orEmpty())
    )