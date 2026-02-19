package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.City
import org.telegram.divo.entity.Gender
import org.telegram.divo.entity.Statistic
import org.telegram.divo.entity.UserInfo


class UserInfoResponse(
    @SerializedName("data") val data: UserData,
)

class UserData(
    @SerializedName("id") val id: Long,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("gender") val gender: Gender,
    @SerializedName("birthday") val birthday: String,
    @SerializedName("city") val city: City,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("photo") val photo: Photo,
    @SerializedName("avatar") val avatar: Avatar,
    @SerializedName("role") val role: String,
    @SerializedName("subrole") val subrole: String?,
    @SerializedName("roleLabel") val roleLabel: String,
    @SerializedName("measuringSystem") val measuringSystem: String,
    @SerializedName("pushNotifications") val pushNotifications: Boolean,
    @SerializedName("isRegistrationFinished") val isRegistrationFinished: Boolean,
    @SerializedName("model") val model: Model,
    @SerializedName("customer") val customer: Any?,
    @SerializedName("agency") val agency: Any?,
    @SerializedName("agencyEmployee") val agencyEmployee: Any?,
    @SerializedName("statistic") val statistic: Statistic,
    @SerializedName("isFavorite") val isFavorite: Boolean,
    @SerializedName("isFollowed") val isFollowed: Boolean,
    @SerializedName("userRatingStatus") val userRatingStatus: String,
    @SerializedName("userSocialNetworks") val userSocialNetworks: List<Any>
)

class Gender(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String
)

class City(
    @SerializedName("id") val id: Int,
    @SerializedName("countryCode") val countryCode: String,
    @SerializedName("countryName") val countryName: String,
    @SerializedName("areaName") val areaName: String,
    @SerializedName("name") val name: String
)

class Photo(
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fullUrl") val fullUrl: String,
    @SerializedName("extension") val extension: String,
    @SerializedName("fileUuid") val fileUuid: String
)

class Avatar(
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fullUrl") val fullUrl: String,
    @SerializedName("extension") val extension: String,
    @SerializedName("fileUuid") val fileUuid: String
)

class Model(
    @SerializedName("agency") val agency: String?,
    @SerializedName("education") val education: String,
    @SerializedName("workExperience") val workExperience: String,
    @SerializedName("languages") val languages: String,
    @SerializedName("profileUrl") val profileUrl: String?,
    @SerializedName("additionalInformation") val additionalInformation: String,
    @SerializedName("hasInternationalPassport") val hasInternationalPassport: Boolean,
    @SerializedName("hasTattoo") val hasTattoo: Boolean,
    @SerializedName("hasPiercing") val hasPiercing: Boolean,
    @SerializedName("hasActingEducation") val hasActingEducation: Boolean,
    @SerializedName("appearance") val appearance: Appearance
)

class Appearance(
    @SerializedName("measuringSystem") val measuringSystem: String,
    @SerializedName("height") val height: Int,
    @SerializedName("weight") val weight: Int,
    @SerializedName("breastSize") val breastSize: String,
    @SerializedName("waist") val waist: Int,
    @SerializedName("hips") val hips: Int,
    @SerializedName("shoesSize") val shoesSize: Int,
    @SerializedName("hairColor") val hairColor: HairColor,
    @SerializedName("hairLength") val hairLength: HairLength,
    @SerializedName("eyeColor") val eyeColor: EyeColor,
    @SerializedName("skinColor") val skinColor: SkinColor
)

class HairColor(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String
)

class HairLength(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String
)

class EyeColor(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String
)

class SkinColor(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String
)

class Statistic(
    @SerializedName("followersCount") val followersCount: Int,
    @SerializedName("followingCount") val followingCount: Int,
    @SerializedName("viewsCount") val viewsCount: Int,
    @SerializedName("sentToAgenciesCount") val sentToAgenciesCount: Int,
    @SerializedName("modelsCount") val modelsCount: Int
)

fun UserInfoResponse.toEntity(): UserInfo = data.toEntity()

fun UserData.toEntity(): UserInfo =
    UserInfo(
        id = id,
        fullName = fullName,
        gender = gender.toEntity(),
        birthday = birthday,
        city = city.toEntity(),
        email = email,
        phone = phone,
        avatarUrl = avatar.fullUrl,
        role = role,
        statistic = statistic.toEntity(),
        isFavorite = isFavorite,
        isFollowed = isFollowed
    )

private fun Gender.toEntity(): Gender =
    Gender(
        id = id,
        title = title
    )

private fun City.toEntity(): City =
    City(
        id = id,
        name = name,
        countryName = countryName
    )

private fun Statistic.toEntity(): Statistic =
    Statistic(
        followersCount = followersCount,
        followingCount = followingCount,
        viewsCount = viewsCount
    )


