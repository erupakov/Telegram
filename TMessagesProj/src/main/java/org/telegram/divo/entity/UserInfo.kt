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
//    val customer: Any?,
//    val agency: Any?,
//    val agencyEmployee: Any?,
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
data class Model(
    val agency: Agency?,
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
data class Agency(
    val id: Int,
    val title: String,
    val site: String?,
    val email: String?,
    val description: String?,
    val employeeTitle: String?,
    val address: AgencyAddress?,
    val photo: Photo?
)

data class AgencyAddress(
    val street: String?,
    val house: String?,
    val apartment: String?,
    val formatted: String?,
    val latitude: Double?,
    val longitude: Double?,
    val city: City?
)

data class Appearance(
    val measuringSystem: String,
    val height: Float,
    val weight: Float,
    val breastSize: String,
    val waist: Int,
    val hips: Int,
    val shoesSize: Float,
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