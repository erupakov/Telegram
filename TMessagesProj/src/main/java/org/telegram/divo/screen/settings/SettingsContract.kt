package org.telegram.divo.screen.settings

import androidx.annotation.DrawableRes
import org.telegram.divo.common.arch.ViewEffect
import org.telegram.divo.common.arch.ViewIntent
import org.telegram.divo.common.arch.ViewState

data class SettingsItem(
    val title: String,
    @DrawableRes val iconResId: Int,
    val intent: SettingsViewIntent,
)

data class SettingsViewState(
    val userId: Int = -1,
    val role: String = "",
    val isModel: Boolean = false,
    val isLoading: Boolean = false,
    val avatarUrl: String = "",
    val userName: String = "",
    val phoneNumber: String = "",
    val measuringSystem: String = "",
    val isSavedProfilesSheetVisible: Boolean = false,
) : ViewState

sealed class SettingsViewIntent : ViewIntent {
    data object OnEditProfileClicked : SettingsViewIntent()
    data object OnOpenProfileClicked : SettingsViewIntent()
    data object OnSetUsernameClicked : SettingsViewIntent()
    data object OnFillParametersClicked : SettingsViewIntent()
    data object OnPromoClicked : SettingsViewIntent()
    data object OnSavedProfilesClicked : SettingsViewIntent()
    data object OnCloseSavedProfilesSheet : SettingsViewIntent()
    data object OnNotificationsClicked : SettingsViewIntent()
    data object OnPrivacyClicked : SettingsViewIntent()
    data object OnDataStorageClicked : SettingsViewIntent()
    data object OnAppearanceClicked : SettingsViewIntent()
    data object OnLanguageClicked : SettingsViewIntent()
    data object OnRefresh : SettingsViewIntent()
    data object OnLogoutClicked : SettingsViewIntent()
    data object OnMeasuringSystemClicked : SettingsViewIntent()
    data class OnChangeMeasuringSystem(val system: String) : SettingsViewIntent()
    data object OnQrCodeClicked : SettingsViewIntent()
}

sealed class SettingsViewEffect : ViewEffect {
    data object NavigateToEditProfile : SettingsViewEffect()
    data class NavigateToProfile(val userId: Int? = null) : SettingsViewEffect()
    data object NavigateToSetUsername : SettingsViewEffect()
    data object NavigateToFillParameters : SettingsViewEffect()
    data object NavigateToPromo : SettingsViewEffect()
    data object NavigateToNotifications : SettingsViewEffect()
    data object NavigateToPrivacy : SettingsViewEffect()
    data object NavigateToDataStorage : SettingsViewEffect()
    data object NavigateToAppearance : SettingsViewEffect()
    data object NavigateToLanguage : SettingsViewEffect()
    data object NavigateToLogout : SettingsViewEffect()
    data object ShowMeasuringSystemDialog : SettingsViewEffect()
    data class ShowError(val message: String) : SettingsViewEffect()
    data object ShowQrCode : SettingsViewEffect()
}