package org.telegram.divo.dal.dto.user

import com.google.gson.annotations.SerializedName
import org.telegram.divo.entity.Agency
import org.telegram.divo.entity.AgencyAddress
import org.telegram.divo.entity.AgencyPhoto
import org.telegram.divo.entity.Appearance
import org.telegram.divo.entity.City
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
    @SerializedName("avatar") val avatar: AvatarDto,
    @SerializedName("role") val role: String,
    @SerializedName("subrole") val subrole: String?,
    @SerializedName("roleLabel") val roleLabel: String,
    @SerializedName("measuringSystem") val measuringSystem: String,
    @SerializedName("pushNotifications") val pushNotifications: Boolean,
    @SerializedName("isRegistrationFinished") val isRegistrationFinished: Boolean,
    @SerializedName("model") val model: ModelDto,
//    @SerializedName("customer") val customer: Any?,
//    @SerializedName("agency") val agency: Any?,
//    @SerializedName("agencyEmployee") val agencyEmployee: Any?,
    @SerializedName("statistic") val statistic: StatisticDto,
    @SerializedName("isFavorite") val isFavorite: Boolean,
    @SerializedName("isFollowed") val isFollowed: Boolean,
    @SerializedName("userRatingStatus") val userRatingStatus: String,
    @SerializedName("userSocialNetworks") val userSocialNetworks: List<String>
)

class GenderDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String
)

class CityDto(
    @SerializedName("id") val id: Int,
    @SerializedName("countryCode") val countryCode: String,
    @SerializedName("countryName") val countryName: String,
    @SerializedName("areaName") val areaName: String,
    @SerializedName("name") val name: String
)

class PhotoDto(
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fullUrl") val fullUrl: String,
    @SerializedName("extension") val extension: String,
    @SerializedName("fileUuid") val fileUuid: String
)

class AvatarDto(
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fullUrl") val fullUrl: String,
    @SerializedName("extension") val extension: String,
    @SerializedName("fileUuid") val fileUuid: String
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
    @SerializedName("photo") val photo: AgencyPhotoDto?
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

class AgencyPhotoDto(
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fullUrl") val fullUrl: String,
    @SerializedName("extension") val extension: String,
    @SerializedName("fileUuid") val fileUuid: String
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
        avatarUrl = avatar.fullUrl,
        role = role,
        subrole = subrole,
        roleLabel = roleLabel,
        measuringSystem = measuringSystem,
        pushNotifications = pushNotifications,
        isRegistrationFinished = isRegistrationFinished,
        model = model.toEntity(),
//        customer = customer,
//        agency = agency,
//        agencyEmployee = agencyEmployee,
        statistic = statistic.toEntity(),
        isFavorite = isFavorite,
        isFollowed = isFollowed,
        userRatingStatus = userRatingStatus,
        userSocialNetworks = userSocialNetworks
    )

private fun GenderDto.toEntity(): Gender =
    Gender(
        id = id,
        title = title
    )

private fun CityDto.toEntity(): City =
    City(
        id = id,
        countryCode = countryCode,
        countryName = countryName,
        areaName = areaName,
        name = name
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

private fun AgencyPhotoDto.toEntity(): AgencyPhoto =
    AgencyPhoto(
        fileName = fileName,
        fullUrl = fullUrl,
        extension = extension,
        fileUuid = fileUuid
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


