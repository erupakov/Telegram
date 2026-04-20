package org.telegram.divo.entity

data class UserInfo(
    val id: Int = 0,
    val fullName: String = "",
    val gender: Gender? = null,
    val birthday: String = "",
    val city: City? = null,
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val photoUuid: String = "",
    val avatarUrl: String = "",
    val avatarUuid: String = "",
    val avatarId: Long = 0,
    val role: RoleType = RoleType.UNKNOWN,
    val subrole: String = "",
    val roleLabel: String = "",
    val measuringSystem: String = "",
    val pushNotifications: Boolean = false,
    val isRegistrationFinished: Boolean = false,
    val model: Model? = null,
    val agency: Agency? = null,
    val customer: Customer? = null,
    val statistic: Statistic = Statistic(),
    val isFavorite: Boolean = false,
    val isFollowed: Boolean = false,
    val userRatingStatus: String = "",
    val userSocialNetworks: List<UserSocialNetwork> = emptyList()
)
data class Gender(
    val id: String = "",
    val title: String = ""
)
data class Model(
    val agency: Agency? = null,
    val education: String = "",
    val workExperience: String = "",
    val languages: String = "",
    val description: String = "",
    val profileUrl: String = "",
    val additionalInformation: String = "",
    val hasInternationalPassport: Boolean = false,
    val hasTattoo: Boolean = false,
    val hasPiercing: Boolean = false,
    val hasActingEducation: Boolean = false,
    val appearance: Appearance? = null,
    val tiktokUrl: String? = null,
    val youtubeUrl: String? = null,
    val instagramUrl: String? = null,
    val websiteUrl: String? = null,
)
data class Agency(
    val id: Int = 0,
    val title: String  = "",
    val site: String = "",
    val email: String = "",
    val description: String = "",
    val employeeTitle: String = "",
    val address: AgencyAddress? = null,
    val photo: Photo? = null,
    val background: Photo? = null,
)

data class AgencyAddress(
    val street: String = "",
    val house: String = "",
    val apartment: String = "",
    val formatted: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val city: City? = City()
)

data class Appearance(
    val measuringSystem: String? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val breastSize: String? = null,
    val waist: Float? = null,
    val hips: Float? = null,
    val shoesSize: Float? = null,
    val hairColor: HairColor? = null,
    val hairLength: HairLength? = null,
    val eyeColor: EyeColor? = null,
    val skinColor: SkinColor? = null
)
data class HairColor(
    val id: Int? = null,
    val title: String? = null
)
data class HairLength(
    val id: Int? = null,
    val title: String? = null
)
data class EyeColor(
    val id: Int? = null,
    val title: String? = null
)
data class SkinColor(
    val id: Int? = null,
    val title: String? = null
)
data class Statistic(
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val viewsCount: Int = 0,
    val sentToAgenciesCount: Int = 0,
    val modelsCount: Int = 0
)

data class Customer(
    val site: String = "",
    val description: String = "",
    val backgroundUuid: String = "",
)