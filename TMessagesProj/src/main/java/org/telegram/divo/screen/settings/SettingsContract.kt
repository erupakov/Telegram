package org.telegram.divo.screen.settings

import androidx.annotation.DrawableRes
import org.telegram.divo.base.ViewAction
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.messenger.R

data class SettingsItem(
    val title: String,
    @DrawableRes val iconResId: Int,
    val intent: SettingsViewIntent,
)

data class SettingsViewState(
    val userName: String = "Joe Doe",
    val phoneNumber: String = "+1 305 345 4564",
    val settingsItems: List<SettingsItem> = listOf(
        SettingsItem(
            title = "Saved Messages",
            iconResId = R.drawable.ic_divo_saved_message_icon,
            intent = SettingsViewIntent.OnSavedMessagesClicked
        ),
        SettingsItem(
            title = "Stickers",
            iconResId = R.drawable.ic_divo_stickers_icon,
            intent = SettingsViewIntent.OnStickersClicked
        ),
        SettingsItem(
            title = "Notifications and Sounds",
            iconResId = R.drawable.ic_divo_settings_notification,
            intent = SettingsViewIntent.OnNotificationsClicked
        ),
        SettingsItem(
            title = "Privacy and Security",
            iconResId = R.drawable.ic_divo_settings_privacy_and_security,
            intent = SettingsViewIntent.OnPrivacyClicked
        ),
        SettingsItem(
            title = "Data and Storage",
            iconResId = R.drawable.ic_divo_settings_data_and_storage,
            intent = SettingsViewIntent.OnDataStorageClicked
        ),
        SettingsItem(
            title = "Appearance",
            iconResId = R.drawable.ic_divo_apperance,
            intent = SettingsViewIntent.OnAppearanceClicked
        ),
    )
) : ViewState

sealed class SettingsViewIntent : ViewIntent {
    data object OnEditProfileClicked : SettingsViewIntent()
    data object OnOpenProfileClicked : SettingsViewIntent()
    data object OnSetUsernameClicked : SettingsViewIntent()
    data object OnFillParametersClicked : SettingsViewIntent()
    data object OnPromoClicked : SettingsViewIntent()
    data object OnSavedMessagesClicked : SettingsViewIntent()
    data object OnStickersClicked : SettingsViewIntent()
    data object OnNotificationsClicked : SettingsViewIntent()
    data object OnPrivacyClicked : SettingsViewIntent()
    data object OnDataStorageClicked : SettingsViewIntent()
    data object OnAppearanceClicked : SettingsViewIntent()
    data object OnQrCodeClicked : SettingsViewIntent()
}

sealed class SettingsViewAction : ViewAction {
    data object NavigateToEditProfile : SettingsViewAction()
    data object NavigateToProfile : SettingsViewAction()
    data object NavigateToSetUsername : SettingsViewAction()
    data object NavigateToFillParameters : SettingsViewAction()
    data object NavigateToPromo : SettingsViewAction()
    data object NavigateToSavedMessages : SettingsViewAction()
    data object NavigateToStickers : SettingsViewAction()
    data object NavigateToNotifications : SettingsViewAction()
    data object NavigateToPrivacy : SettingsViewAction()
    data object NavigateToDataStorage : SettingsViewAction()
    data object NavigateToAppearance : SettingsViewAction()
    data object ShowQrCode : SettingsViewAction()
}