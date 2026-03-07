package org.telegram.divo.entity

data class UserInfo(
    val id: Int = 0,
    val fullName: String = "",
    val gender: Gender = Gender(),
    val birthday: String = "",
    val city: City = City(),
    val email: String = "",
    val phone: String = "",
    val photoUrl: String = "",
    val photoUuid: String = "",
    val avatarUrl: String = "",
    val avatarUuid: String = "",
    val role: String = "",
    val subrole: String = "",
    val roleLabel: String = "",
    val measuringSystem: String = "",
    val pushNotifications: Boolean = false,
    val isRegistrationFinished: Boolean = false,
    val model: Model = Model(),
    val customer: Customer? = Customer(),
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
    val agency: Agency? = Agency(),
    val education: String = "",
    val workExperience: String = "",
    val languages: String = "",
    val profileUrl: String = "",
    val additionalInformation: String = "",
    val hasInternationalPassport: Boolean = false,
    val hasTattoo: Boolean = false,
    val hasPiercing: Boolean = false,
    val hasActingEducation: Boolean = false,
    val appearance: Appearance = Appearance()
)
data class Agency(
    val id: Int = 0,
    val title: String  = "",
    val site: String = "",
    val email: String = "",
    val description: String = "",
    val employeeTitle: String = "",
    val address: AgencyAddress? = AgencyAddress(),
    val photo: Photo? = Photo()
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
    val measuringSystem: String = "",
    val height: Float = 0f,
    val weight: Float = 0f,
    val breastSize: String = "",
    val waist: Float = 0f,
    val hips: Float = 0f,
    val shoesSize: Float = 0f,
    val hairColor: HairColor = HairColor(),
    val hairLength: HairLength = HairLength(),
    val eyeColor: EyeColor = EyeColor(),
    val skinColor: SkinColor = SkinColor()
)
data class HairColor(
    val id: Int = 0,
    val title: String = ""
)
data class HairLength(
    val id: Int = 0,
    val title: String = ""
)
data class EyeColor(
    val id: Int = 0,
    val title: String = ""
)
data class SkinColor(
    val id: Int = 0,
    val title: String = ""
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