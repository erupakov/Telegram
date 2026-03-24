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
import org.telegram.divo.entity.Gender
import org.telegram.divo.entity.RoleType
import org.telegram.divo.entity.Statistic
import org.telegram.divo.entity.UserInfo


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
    @SerializedName("userRatingStatus") val userRatingStatus: String?,
    @SerializedName("userSocialNetworks") val userSocialNetworks: List<UserSocialNetworkDto>,
    @SerializedName("customer") val customer: CustomerDto?,
    @SerializedName("agencyEmployee") val agencyEmployee: AgencyEmployeeDto?,
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
    @SerializedName("sentToAgenciesCount") val sentToAgenciesCount: Int,
    @SerializedName("modelsCount") val modelsCount: Int
)

fun UserInfoResponse.toEntity(): UserInfo = data.toEntity()

fun UserDataDto.toEntity(): UserInfo {
    val role = RoleType.from(role)
    val source = if (role.isModel()) photo else photo ?: agency?.photo

    return UserInfo(
        id = id,
        fullName = if (role.isModel()) fullName.orEmpty() else agency?.employeeTitle.orEmpty(),
        gender = gender?.toEntity(),
        birthday = birthday.orEmpty(),
        city = city?.toEntity(),
        email = email.orEmpty(),
        phone = phone.orEmpty(),
        photoUrl = source?.fullUrl.orEmpty(),
        photoUuid = source?.fileUuid.orEmpty(),
        avatarUrl = avatar?.fullUrl.orEmpty(),
        avatarUuid = avatar?.fileUuid.orEmpty(),
        role = role,
        subrole = subrole.orEmpty(),
        roleLabel = roleLabel.orEmpty(),
        measuringSystem = measuringSystem.orEmpty(),
        pushNotifications = pushNotifications,
        isRegistrationFinished = isRegistrationFinished,
        model = model?.toEntity(),
        customer = customer?.toEntity(),
        agency = agency?.toEntity(),
//        agencyEmployee = agencyEmployee,
        statistic = statistic.toEntity(),
        isFavorite = isFavorite,
        isFollowed = isFollowed,
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
        followersCount = followersCount,
        followingCount = followingCount,
        viewsCount = viewsCount,
        sentToAgenciesCount = sentToAgenciesCount,
        modelsCount = modelsCount
    )



