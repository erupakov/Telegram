package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.dal.dto.common.CityDto
import org.telegram.divo.dal.dto.common.PhotoDto
import org.telegram.divo.dal.dto.common.UserSocialNetworkDto
import org.telegram.divo.dal.dto.common.UuidContainerDto
import org.telegram.divo.dal.dto.common.toEntities
import org.telegram.divo.dal.dto.common.toEntity
import org.telegram.divo.entity.Agency
import org.telegram.divo.entity.AgencyAddress
import org.telegram.divo.entity.Appearance
import org.telegram.divo.entity.Customer
import org.telegram.divo.entity.EyeColor
import org.telegram.divo.entity.Gender
import org.telegram.divo.entity.HairColor
import org.telegram.divo.entity.HairLength
import org.telegram.divo.entity.Model
import org.telegram.divo.entity.SkinColor
import org.telegram.divo.entity.Statistic
import org.telegram.divo.entity.UserInfo


class UserInfoResponse(
    @SerializedName("data") val data: UserDataDto,
)

class UserDataDto(
    @SerializedName("id") val id: Int,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("gender") val gender: GenderDto,
    @SerializedName("birthday") val birthday: String,
    @SerializedName("city") val city: CityDto,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("photo") val photo: PhotoDto,
    @SerializedName("avatar") val avatar: PhotoDto,
    @SerializedName("role") val role: String,
    @SerializedName("subrole") val subrole: String?,
    @SerializedName("roleLabel") val roleLabel: String,
    @SerializedName("measuringSystem") val measuringSystem: String,
    @SerializedName("pushNotifications") val pushNotifications: Boolean,
    @SerializedName("isRegistrationFinished") val isRegistrationFinished: Boolean,
    @SerializedName("model") val model: ModelDto,
    @SerializedName("statistic") val statistic: StatisticDto,
    @SerializedName("isFavorite") val isFavorite: Boolean,
    @SerializedName("isFollowed") val isFollowed: Boolean,
    @SerializedName("userRatingStatus") val userRatingStatus: String,
    @SerializedName("userSocialNetworks") val userSocialNetworks: List<UserSocialNetworkDto>,
    @SerializedName("customer") val customer: CustomerDto?,
)

class GenderDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String
)

class ModelDto(
    @SerializedName("agency") val agency: AgencyDto?,
    @SerializedName("education") val education: String?,
    @SerializedName("workExperience") val workExperience: String?,
    @SerializedName("languages") val languages: String,
    @SerializedName("profileUrl") val profileUrl: String?,
    @SerializedName("additionalInformation") val additionalInformation: String,
    @SerializedName("hasInternationalPassport") val hasInternationalPassport: Boolean,
    @SerializedName("hasTattoo") val hasTattoo: Boolean,
    @SerializedName("hasPiercing") val hasPiercing: Boolean,
    @SerializedName("hasActingEducation") val hasActingEducation: Boolean,
    @SerializedName("appearance") val appearance: AppearanceDto
)

class AgencyDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String,
    @SerializedName("site") val site: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("employeeTitle") val employeeTitle: String?,
    @SerializedName("address") val address: AgencyAddressDto?,
    @SerializedName("photo") val photo: PhotoDto?
)

class AgencyAddressDto(
    @SerializedName("street") val street: String?,
    @SerializedName("house") val house: String?,
    @SerializedName("apartment") val apartment: String?,
    @SerializedName("formatted") val formatted: String?,
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?,
    @SerializedName("city") val city: CityDto?
)

class AppearanceDto(
    @SerializedName("measuringSystem") val measuringSystem: String,
    @SerializedName("height") val height: Float,
    @SerializedName("weight") val weight: Float,
    @SerializedName("breastSize") val breastSize: String,
    @SerializedName("waist") val waist: Int,
    @SerializedName("hips") val hips: Int,
    @SerializedName("shoesSize") val shoesSize: Float,
    @SerializedName("hairColor") val hairColor: HairColorDto,
    @SerializedName("hairLength") val hairLength: HairLengthDto,
    @SerializedName("eyeColor") val eyeColor: EyeColorDto,
    @SerializedName("skinColor") val skinColor: SkinColorDto
)

class HairColorDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String
)

class HairLengthDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String
)

class EyeColorDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String
)

class SkinColorDto(
    @SerializedName("id") val id: Int,
    @SerializedName("title") val title: String
)

class StatisticDto(
    @SerializedName("followersCount") val followersCount: Int,
    @SerializedName("followingCount") val followingCount: Int,
    @SerializedName("viewsCount") val viewsCount: Int,
    @SerializedName("sentToAgenciesCount") val sentToAgenciesCount: Int,
    @SerializedName("modelsCount") val modelsCount: Int
)

data class CustomerDto(
    @SerializedName("site") val site: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("background") val backgroundUuid: UuidContainerDto? = null
)

fun UserInfoResponse.toEntity(): UserInfo = data.toEntity()

fun UserDataDto.toEntity(): UserInfo =
    UserInfo(
        id = id,
        fullName = fullName,
        gender = gender.toEntity(),
        birthday = birthday,
        city = city.toEntity(),
        email = email,
        phone = phone,
        photoUrl = photo.fullUrl,
        photoUuid = photo.fileUuid,
        avatarUrl = avatar.fullUrl,
        avatarUuid = avatar.fileUuid,
        role = role,
        subrole = subrole,
        roleLabel = roleLabel,
        measuringSystem = measuringSystem,
        pushNotifications = pushNotifications,
        isRegistrationFinished = isRegistrationFinished,
        model = model.toEntity(),
        customer = customer?.toEntity(),
//        agency = agency,
//        agencyEmployee = agencyEmployee,
        statistic = statistic.toEntity(),
        isFavorite = isFavorite,
        isFollowed = isFollowed,
        userRatingStatus = userRatingStatus,
        userSocialNetworks = userSocialNetworks.toEntities()
    )

private fun GenderDto.toEntity(): Gender =
    Gender(
        id = id,
        title = title
    )

private fun CustomerDto.toEntity(): Customer =
    Customer(
        site = site,
        description = description,
        backgroundUuid = backgroundUuid?.uuid
    )

private fun StatisticDto.toEntity(): Statistic =
    Statistic(
        followersCount = followersCount,
        followingCount = followingCount,
        viewsCount = viewsCount,
        sentToAgenciesCount = sentToAgenciesCount,
        modelsCount = modelsCount
    )

private fun ModelDto.toEntity(): Model =
    Model(
        agency = agency?.toEntity(),
        education = education.orEmpty(),
        workExperience = workExperience.orEmpty(),
        languages = languages,
        profileUrl = profileUrl,
        additionalInformation = additionalInformation,
        hasInternationalPassport = hasInternationalPassport,
        hasTattoo = hasTattoo,
        hasPiercing = hasPiercing,
        hasActingEducation = hasActingEducation,
        appearance = appearance.toEntity()
    )

private fun AgencyDto.toEntity(): Agency =
    Agency(
        id = id,
        title = title,
        site = site,
        email = email,
        description = description,
        employeeTitle = employeeTitle,
        address = address?.toEntity(),
        photo = photo?.toEntity()
    )

private fun AgencyAddressDto.toEntity(): AgencyAddress =
    AgencyAddress(
        street = street,
        house = house,
        apartment = apartment,
        formatted = formatted,
        latitude = latitude,
        longitude = longitude,
        city = city?.toEntity()
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
        hairColor = hairColor.toEntity(),
        hairLength = hairLength.toEntity(),
        eyeColor = eyeColor.toEntity(),
        skinColor = skinColor.toEntity()
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


