package org.telegram.divo.entity

data class UserInfo(
    val id: Int,
    val fullName: String,
    val gender: Gender,
    val birthday: String,
    val city: City,
    val email: String,
    val phone: String,
    val photoUrl: String,
    val avatarUrl: String,
    val role: String,
    val subrole: String?,
    val roleLabel: String,
    val measuringSystem: String,
    val pushNotifications: Boolean,
    val isRegistrationFinished: Boolean,
    val model: Model,
    val customer: Any?,
    val agency: Any?,
    val agencyEmployee: Any?,
    val statistic: Statistic,
    val isFavorite: Boolean,
    val isFollowed: Boolean,
    val userRatingStatus: String,
    val userSocialNetworks: List<String>
)
data class Gender(
    val id: String,
    val title: String
)
data class City(
    val id: Int,
    val countryCode: String,
    val countryName: String,
    val areaName: String,
    val name: String
)
data class Model(
    val agency: String?,
    val education: String,
    val workExperience: String,
    val languages: String,
    val profileUrl: String?,
    val additionalInformation: String,
    val hasInternationalPassport: Boolean,
    val hasTattoo: Boolean,
    val hasPiercing: Boolean,
    val hasActingEducation: Boolean,
    val appearance: Appearance
)
data class Appearance(
    val measuringSystem: String,
    val height: Int,
    val weight: Int,
    val breastSize: String,
    val waist: Int,
    val hips: Int,
    val shoesSize: Int,
    val hairColor: HairColor,
    val hairLength: HairLength,
    val eyeColor: EyeColor,
    val skinColor: SkinColor
)
data class HairColor(
    val id: Int,
    val title: String
)
data class HairLength(
    val id: Int,
    val title: String
)
data class EyeColor(
    val id: Int,
    val title: String
)
data class SkinColor(
    val id: Int,
    val title: String
)
data class Statistic(
    val followersCount: Int,
    val followingCount: Int,
    val viewsCount: Int,
    val sentToAgenciesCount: Int,
    val modelsCount: Int
)