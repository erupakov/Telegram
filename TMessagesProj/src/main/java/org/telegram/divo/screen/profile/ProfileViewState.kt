package org.telegram.divo.screen.profile

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.dal.dto.user.GalleryItem
import org.telegram.divo.entity.UserGalleryItem
import org.telegram.divo.entity.UserInfo
import org.telegram.tgnet.TLRPC
import java.time.LocalDate
import java.time.Period
import java.util.Locale

data class ProfileViewState(
    val userId: Int = -1,
    val isOwnProfile: Boolean = false,
    val userInfo: UserInfo? = null,
    val userGalleryItems: List<UserGalleryItem> = listOf(),

    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val portfolioLoading: Boolean = false,
    val portfolioUploading: Boolean = false,

    val socialLinks: SocialLinks = SocialLinks(),
    val physicalParams: PhysicalParams = PhysicalParams(),
    val statistic: UserStatistic = UserStatistic()
) : ViewState {

    val countryFlagEmoji: String
        get() = userInfo?.let { user ->
            user.city.countryCode
                .uppercase()
                .map { char -> Character.toCodePoint('\uD83C', '\uDDE6' + (char - 'A')) }
                .joinToString("") { String(Character.toChars(it)) }
        }.orEmpty()

    fun formattedAge(age: String, locale: Locale = Locale.getDefault()): String {
        try {
            val birthDate = LocalDate.parse(age)
            val age = Period.between(birthDate, LocalDate.now()).years
            val format = mapOf(
                "ar" to "$age سنة",
                "de" to "$age J.",
                "es" to "$age años",
                "it" to "$age anni",
                "ko" to "${age}세",
            )
            return format[locale.language] ?: "$age y.o."
        } catch (_: Exception) {
            return ""
        }
    }
}

// --- Вспомогательные классы для UI ---

data class SocialLinks(
    val instagram: String = "",
    val tiktok: String = "",
    val youtube: String = "",
    val website: String = ""
)

data class PhysicalParams(
    val gender: String = "",
    val age: String = "",
    val height: Int = 0,
    val waist: Int = 0,
    val hips: Int = 0,
    val shoeSize: Int = 0,
    val hairLength: String = "",
    val hairColor: String = "",
    val eyeColor: String = "",
    val skinColor: String = "",
    val breastSize: String = ""
)

data class UserStatistic(
    val followers: Int = 0,
    val following: Int = 0,
    val views: Int = 0,
    val likes: Int = 0,
    val saves: Int = 0
)

// --- Intents (Действия UI) ---

sealed class ProfileIntent : ViewIntent {
    data class OnLoad(val userId: Int, val isOwnProfile: Boolean) : ProfileIntent()
    object OnClearPortfolioUpload : ProfileIntent()

    // Для загрузки фото теперь передаем путь к файлу или Uri
    class OnPortfolioPhotoSelected(
        val photo: TLRPC.InputFile,
        val localPath: String?
    ) : ProfileIntent()
    class OnBackgroundPhotoSelected(
        val photo: TLRPC.InputFile,
        val localPath: String?
    ) : ProfileIntent()
    class OpenSocialLink(val url: String) : ProfileIntent()
}

// --- Effects (Навигация и Toast) ---

sealed class ProfileEffect : ViewEffect {
    class OpenUrl(val url: String) : ProfileEffect()
    class ShowError(val message: String) : ProfileEffect()
    // Остальные эффекты навигации...
    object NavigateToSearch : ProfileEffect()
}