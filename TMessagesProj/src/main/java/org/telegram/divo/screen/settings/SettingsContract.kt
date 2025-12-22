package org.telegram.divo.screen.settings

import androidx.annotation.DrawableRes
import org.telegram.divo.base.ViewEffect
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

sealed class SettingsViewEffect : ViewEffect {
    data object NavigateToEditProfile : SettingsViewEffect()
    data object NavigateToProfile : SettingsViewEffect()
    data object NavigateToSetUsername : SettingsViewEffect()
    data object NavigateToFillParameters : SettingsViewEffect()
    data object NavigateToPromo : SettingsViewEffect()
    data object NavigateToSavedMessages : SettingsViewEffect()
    data object NavigateToStickers : SettingsViewEffect()
    data object NavigateToNotifications : SettingsViewEffect()
    data object NavigateToPrivacy : SettingsViewEffect()
    data object NavigateToDataStorage : SettingsViewEffect()
    data object NavigateToAppearance : SettingsViewEffect()
    data object ShowQrCode : SettingsViewEffect()
}