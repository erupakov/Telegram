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
    val isLikedByUser: Boolean = false,
    val isPremium: Boolean = false,
    val isVerified: Boolean = false,
    val userRatingStatus: String = "",
    val isOnline: Boolean? = false,
    val telegramId: Long? = null,
    val telegramAccessHash: Long? = null,
    val telegramUsername: String? = null,
    val userSocialNetworks: List<UserSocialNetwork> = emptyList(),
    val channels: List<UserChannel> = emptyList()
) {
    val displayName: String
        get() {
            val rawName = if (role == RoleType.AGENCY) agency?.title?.takeIf { it.isNotBlank() } ?: fullName else fullName
            return rawName.toTitleCase()
        }

    private fun String.toTitleCase(): String {
        val delimiters = charArrayOf(' ', '-')
        var capitalizeNext = true
        val result = StringBuilder(length)
        for (char in this) {
            if (char in delimiters) {
                capitalizeNext = true
                result.append(char)
            } else if (capitalizeNext) {
                result.append(char.uppercaseChar())
                capitalizeNext = false
            } else {
                result.append(char.lowercaseChar())
            }
        }
        return result.toString()
    }
}

data class UserChannel(
    val id: Int,
    val telegramChatId: Long,
    val username: String?,
    val inviteLink: String?
)

data class Gender(
    val id: String = "",
    val title: String = ""
)

fun mapGenderToEnglish(localizedGenders: String?): String? {
    if (localizedGenders.isNullOrBlank()) return null
    val context = org.telegram.messenger.ApplicationLoader.applicationContext
    val array = context.resources.getStringArray(org.telegram.messenger.R.array.GenderItems)
    
    val maleLocalized = array.getOrNull(1)
    val femaleLocalized = array.getOrNull(2)
    
    val items = localizedGenders.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    
    val mapped = items.map { item ->
        val lower = item.lowercase()
        when {
            item.equals(maleLocalized, ignoreCase = true) || lower in listOf("male", "мужской", "masculino", "masculin", "男性") -> "male"
            item.equals(femaleLocalized, ignoreCase = true) || lower in listOf("female", "женский", "femenino", "feminino", "féminin", "女性") -> "female"
            else -> lower
        }
    }
    
    return mapped.joinToString(",").takeIf { it.isNotEmpty() }
}

fun mapGenderToLocalized(genderId: String?): String? {
    if (genderId.isNullOrBlank()) return null
    val context = org.telegram.messenger.ApplicationLoader.applicationContext
    val array = context.resources.getStringArray(org.telegram.messenger.R.array.GenderItems)
    
    return when (genderId.lowercase()) {
        "male" -> array.getOrNull(1)
        "female" -> array.getOrNull(2)
        else -> null
    }
}

fun mapRoleToEnglish(localizedRoles: String?): String? {
    if (localizedRoles.isNullOrBlank()) return null
    val context = org.telegram.messenger.ApplicationLoader.applicationContext
    val array = context.resources.getStringArray(org.telegram.messenger.R.array.ModelNewTalentAgency)
    
    val allRolesLabel = array.getOrNull(0)
    val modelLocalized = array.getOrNull(1)
    val newTalentLocalized = array.getOrNull(2)
    val agencyLocalized = array.getOrNull(3)
    
    val items = localizedRoles.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    
    val mapped = items.map { item ->
        val lower = item.lowercase().replace(" ", "_")
        when {
            item.equals(allRolesLabel, ignoreCase = true) || lower in listOf("all", "all_roles", "все_роли", "todos_los_roles", "tous_les_rôles", "todos_os_papéis", "所有角色") -> "all"
            item.equals(modelLocalized, ignoreCase = true) || lower in listOf("model", "модель", "modelo", "modèle", "模特") -> "model"
            item.equals(newTalentLocalized, ignoreCase = true) || lower in listOf("new_talent", "new_face", "новое_лицо", "новое лицо", "nuevo_talento", "nouveau_talent", "novo_talento", "新面孔") -> "new_face"
            item.equals(agencyLocalized, ignoreCase = true) || lower in listOf("agency", "агентство", "agencia", "agence", "agência", "机构") -> "agency"
            item.equals("fan", ignoreCase = true) || lower in listOf("fan", "фан", "fã", "爱好者") -> "fan"
            else -> lower
        }
    }
    
    return mapped.joinToString(",").takeIf { it.isNotEmpty() }
}

fun mapRoleToLocalized(roleId: String?): String? {
    if (roleId.isNullOrBlank()) return null
    val context = org.telegram.messenger.ApplicationLoader.applicationContext
    val array = context.resources.getStringArray(org.telegram.messenger.R.array.ModelNewTalentAgency)
    
    return when (roleId.lowercase().trim().replace(" ", "_")) {
        "all", "all_roles" -> array.getOrNull(0)
        "model" -> array.getOrNull(1)
        "new_face", "new_talent" -> array.getOrNull(2)
        "agency", "agency_employee" -> array.getOrNull(3)
        else -> roleId
    }
}

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
    val tiktokUrl: String? = null,
    val youtubeUrl: String? = null,
    val telegramUrl: String? = null,
    val instagramUrl: String? = null,
    val websiteUrl: String? = null,
    val socialNetworks: List<UserSocialNetwork> = emptyList(),
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
    val likesCount: Int = 0,
    val followersCount: Int = 0,
    val viewsCount: Int = 0,
    val sentToAgenciesCount: Int = 0,
    val modelsCount: Int = 0
)

data class Customer(
    val site: String = "",
    val description: String = "",
    val backgroundUuid: String = "",
)