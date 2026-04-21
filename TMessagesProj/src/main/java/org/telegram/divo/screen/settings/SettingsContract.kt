package org.telegram.divo.screen.settings

import androidx.annotation.DrawableRes
import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState

data class SettingsItem(
    val title: String,
    @DrawableRes val iconResId: Int,
    val intent: SettingsViewIntent,
)

data class SettingsViewState(
    val userId: Int = -1,
    val isModel: Boolean = false,
    val isLoading: Boolean = false,
    val avatarUrl: String = "",
    val userName: String = "",
    val phoneNumber: String = "",
) : ViewState

sealed class SettingsViewIntent : ViewIntent {
    data object OnEditProfileClicked : SettingsViewIntent()
    data object OnOpenProfileClicked : SettingsViewIntent()
    data object OnSetUsernameClicked : SettingsViewIntent()
    data object OnFillParametersClicked : SettingsViewIntent()
    data object OnPromoClicked : SettingsViewIntent()
    data object OnSavedMessagesClicked : SettingsViewIntent()
    data object OnNotificationsClicked : SettingsViewIntent()
    data object OnPrivacyClicked : SettingsViewIntent()
    data object OnDataStorageClicked : SettingsViewIntent()
    data object OnAppearanceClicked : SettingsViewIntent()
    data object OnQrCodeClicked : SettingsViewIntent()
    data object OnRefresh : SettingsViewIntent()
}

sealed class SettingsViewEffect : ViewEffect {
    data object NavigateToEditProfile : SettingsViewEffect()
    data object NavigateToProfile : SettingsViewEffect()
    data object NavigateToSetUsername : SettingsViewEffect()
    data object NavigateToFillParameters : SettingsViewEffect()
    data object NavigateToPromo : SettingsViewEffect()
    data object NavigateToSavedMessages : SettingsViewEffect()
    data object NavigateToNotifications : SettingsViewEffect()
    data object NavigateToPrivacy : SettingsViewEffect()
    data object NavigateToDataStorage : SettingsViewEffect()
    data object NavigateToAppearance : SettingsViewEffect()
    data object ShowQrCode : SettingsViewEffect()
    data class ShowError(val message: String) : SettingsViewEffect()
}