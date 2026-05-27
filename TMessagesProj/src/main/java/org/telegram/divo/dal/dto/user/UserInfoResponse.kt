package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.AgencyDto
import org.telegram.divo.dal.dto.common.CityDto
import org.telegram.divo.dal.dto.common.CustomerDto
import org.telegram.divo.dal.dto.common.ModelDto
import org.telegram.divo.dal.dto.common.PhotoDto
import org.telegram.divo.dal.dto.common.UserSocialNetworkDto
import org.telegram.divo.dal.dto.common.toEntities
import org.telegram.divo.dal.dto.common.toEntity
import org.telegram.divo.entity.Agency
import org.telegram.divo.entity.City
import org.telegram.divo.entity.Customer
import org.telegram.divo.entity.Gender
import org.telegram.divo.entity.Model
import org.telegram.divo.entity.RoleType
import org.telegram.divo.entity.Statistic
import org.telegram.divo.entity.UserInfo
import org.telegram.divo.common.AdditionalInfoKeys


class UserInfoResponse(
    @SerializedName("data") val data: UserDataDto,
)

class UserDataDto(
    @SerializedName("id") val id: Int,
    @SerializedName("fullName") val fullName: String?,
    @SerializedName("gender") val gender: GenderDto?,
    @SerializedName("birthday") val birthday: String?,
    @SerializedName("city") val city: CityDto?,
    @SerializedName("email") val email: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("photo") val photo: PhotoDto?,
    @SerializedName("avatar") val avatar: PhotoDto?,
    @SerializedName("role") val role: String?,
    @SerializedName("subrole") val subrole: String?,
    @SerializedName("roleLabel") val roleLabel: String?,
    @SerializedName("measuringSystem") val measuringSystem: String?,
    @SerializedName("pushNotifications") val pushNotifications: Boolean,
    @SerializedName("isRegistrationFinished") val isRegistrationFinished: Boolean,
    @SerializedName("model") val model: ModelDto?,
    @SerializedName("agency") val agency: AgencyDto?,
    @SerializedName("statistic") val statistic: StatisticDto,
    @SerializedName("isFavorite") val isFavorite: Boolean,
    @SerializedName("isFollowed") val isFollowed: Boolean,
    @SerializedName("is_premium") val isPremium: Boolean,
    @SerializedName("userRatingStatus") val userRatingStatus: String?,
    @SerializedName("userSocialNetworks") val userSocialNetworks: List<UserSocialNetworkDto>,
    @SerializedName("customer") val customer: CustomerDto?,
    @SerializedName("agencyEmployee") val agencyEmployee: AgencyEmployeeDto?,
    @SerializedName("additionalInfo") val additionalInfo: Map<String, Any?>? = null,
)

class GenderDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String
)

class AgencyEmployeeDto(
    @SerializedName("role") val role: String
)

class StatisticDto(
    @SerializedName("followersCount") val followersCount: Int,
    @SerializedName("followingCount") val followingCount: Int,
    @SerializedName("viewsCount") val viewsCount: Int,
    @SerializedName("likesCount") val likesCount: Int,
    @SerializedName("sentToAgenciesCount") val sentToAgenciesCount: Int,
    @SerializedName("modelsCount") val modelsCount: Int
)

fun UserInfoResponse.toEntity(): UserInfo = data.toEntity()

fun UserDataDto.toEntity(): UserInfo {
    val roleEnum = RoleType.from(role)
    val source = if (roleEnum.isModel()) photo else photo ?: agency?.photo

    val info = additionalInfo

    val resolvedCity = city?.toEntity() ?: run {
        if (info != null) {
            val country = info[AdditionalInfoKeys.COUNTRY] as? String
            val cityName = info[AdditionalInfoKeys.CITY] as? String
            val countryCode = info[AdditionalInfoKeys.COUNTRY_CODE] as? String
            if (!country.isNullOrBlank() || !cityName.isNullOrBlank()) {
                City(
                    id = -1,
                    countryCode = countryCode.orEmpty(),
                    countryName = country.orEmpty(),
                    areaName = "",
                    name = cityName.orEmpty()
                )
            } else null
        } else null
    }

    val resolvedFullName = if (!fullName.isNullOrBlank()) fullName else {
        if (roleEnum.isModel()) {
            val firstName = info?.get(AdditionalInfoKeys.FIRST_NAME) as? String ?: ""
            val lastName = info?.get(AdditionalInfoKeys.LAST_NAME) as? String ?: ""
            listOf(firstName, lastName).filter { it.isNotBlank() }.joinToString(" ")
        } else {
            agency?.title?.takeIf { it.isNotBlank() } ?: info?.get(AdditionalInfoKeys.COMPANY_NAME) as? String ?: ""
        }
    }

    val resolvedGender = gender?.toEntity() ?: run {
        val genderStr = info?.get(AdditionalInfoKeys.GENDER) as? String
        if (!genderStr.isNullOrBlank()) {
            Gender(id = genderStr, title = genderStr) // Or whatever fallback makes sense
        } else null
    }

    val resolvedBirthday = if (!birthday.isNullOrBlank()) birthday else info?.get(AdditionalInfoKeys.DATE_OF_BIRTH) as? String ?: ""

    val resolvedPhone = phone.orEmpty().ifBlank {
        info?.get(AdditionalInfoKeys.PHONE) as? String ?: info?.get(AdditionalInfoKeys.CONTACT_PHONE) as? String ?: ""
    }

    val resolvedPhotoUuid = source?.fileUuid.orEmpty().ifBlank { info?.get(AdditionalInfoKeys.PHOTO_UUID) as? String ?: "" }
    val resolvedAvatarUuid = avatar?.fileUuid.orEmpty().ifBlank { info?.get(AdditionalInfoKeys.PHOTO_UUID) as? String ?: "" }

    val resolvedPhotoUrl = source?.fullUrl.orEmpty().ifBlank { info?.get(AdditionalInfoKeys.PHOTO_URL) as? String ?: "" }
    val resolvedAvatarUrl = avatar?.fullUrl.orEmpty().ifBlank { info?.get(AdditionalInfoKeys.PHOTO_URL) as? String ?: "" }

    val resolvedAgency = agency?.toEntity() ?: run {
        val title = info?.get(AdditionalInfoKeys.COMPANY_NAME) as? String ?: info?.get(AdditionalInfoKeys.AGENCY_NAME) as? String
        val website = info?.get(AdditionalInfoKeys.WEBSITE_URL) as? String
        if (!title.isNullOrBlank() || !website.isNullOrBlank()) {
            Agency(title = title.orEmpty(), site = website.orEmpty())
        } else null
    }

    val resolvedModel = model?.toEntity() ?: run {
        if (roleEnum.isModel()) {
            val instagram = info?.get(AdditionalInfoKeys.INSTAGRAM_URL) as? String
            val portfolio = info?.get(AdditionalInfoKeys.PORTFOLIO_URL) as? String
            val casting = info?.get(AdditionalInfoKeys.CASTING_PROFILE_URL) as? String
            val showreel = info?.get(AdditionalInfoKeys.SHOWREEL_URL) as? String
            if (!instagram.isNullOrBlank() || !portfolio.isNullOrBlank() || !casting.isNullOrBlank() || !showreel.isNullOrBlank()) {
                Model(
                    instagramUrl = instagram,
                    websiteUrl = portfolio,
                    profileUrl = casting.orEmpty(),
                    youtubeUrl = showreel
                )
            } else null
        } else null
    }

    val resolvedCustomer = customer?.toEntity() ?: run {
        if (roleEnum == RoleType.CUSTOMER) {
            val site = info?.get(AdditionalInfoKeys.WEBSITE_URL) as? String ?: info?.get(AdditionalInfoKeys.PORTFOLIO_URL) as? String
            val desc = info?.get(AdditionalInfoKeys.SPECIALISATION) as? String
            if (!site.isNullOrBlank() || !desc.isNullOrBlank()) {
                Customer(site = site.orEmpty(), description = desc.orEmpty())
            } else null
        } else null
    }

    return UserInfo(
        id = id,
        fullName = resolvedFullName,
        gender = resolvedGender,
        birthday = resolvedBirthday,
        city = resolvedCity,
        email = email.orEmpty(),
        phone = resolvedPhone,
        photoUrl = resolvedPhotoUrl,
        photoUuid = resolvedPhotoUuid,
        avatarUrl = resolvedAvatarUrl,
        avatarUuid = resolvedAvatarUuid,
        avatarId = avatar?.photoId ?: 0,
        role = roleEnum,
        subrole = subrole.orEmpty(),
        roleLabel = roleLabel.orEmpty(),
        measuringSystem = measuringSystem.orEmpty(),
        pushNotifications = pushNotifications,
        isRegistrationFinished = isRegistrationFinished,
        model = resolvedModel,
        customer = resolvedCustomer,
        agency = resolvedAgency,
//        agencyEmployee = agencyEmployee,
        statistic = statistic.toEntity(),
        isFavorite = isFavorite,
        isFollowed = isFollowed,
        isPremium = isPremium,
        userRatingStatus = userRatingStatus.orEmpty(),
        userSocialNetworks = userSocialNetworks.toEntities()
    )
}

private fun GenderDto.toEntity(): Gender =
    Gender(
        id = id,
        title = title
    )

private fun StatisticDto.toEntity(): Statistic =
    Statistic(
        likesCount = likesCount,
        followersCount = followersCount,
        viewsCount = viewsCount,
        sentToAgenciesCount = sentToAgenciesCount,
        modelsCount = modelsCount
    )



