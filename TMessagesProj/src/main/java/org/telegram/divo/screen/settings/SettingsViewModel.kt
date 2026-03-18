package org.telegram.divo.screen.settings

import androidx.annotation.DrawableRes
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.TLRPC

class SettingsViewModel :
    BaseViewModel<SettingsViewModel.SettingsViewState, SettingsViewModel.SettingsViewIntent, SettingsViewModel.SettingsViewEffect>() {

    data class SettingsItem(
        val title: String,
        @DrawableRes val iconResId: Int,
        val intent: SettingsViewIntent,
    )

    data class SettingsViewState(
        val userId: Int = -1,
        val userName: String = "",
        val phoneNumber: String = "",
        val userFull: TLRPC.UserFull? = null,
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
        data object OnRefresh : SettingsViewIntent()
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

    init {
        //TODO временно
        viewModelScope.launch {
            val result = DivoApi.userRepository.getCurrentUserInfo()

            if (result is DivoResult.Success) {
                setState { copy(userId = result.value.id) }
            } else {
                val errorMsg = result.getErrorMessage()
            }
        }
    }

    private var currentAccount = UserConfig.selectedAccount

    override fun createInitialState(): SettingsViewState {

        val userFull = MessagesController.getInstance(currentAccount)
            .getUserFull(UserConfig.getInstance(currentAccount).getClientUserId())
        return SettingsViewState(
            userFull = userFull
        )
    }

    override fun handleIntent(intent: SettingsViewIntent) {
        when (intent) {
            SettingsViewIntent.OnEditProfileClicked -> sendEffect(SettingsViewEffect.NavigateToEditProfile)
            SettingsViewIntent.OnOpenProfileClicked -> sendEffect(SettingsViewEffect.NavigateToProfile)
            SettingsViewIntent.OnSetUsernameClicked -> sendEffect(SettingsViewEffect.NavigateToSetUsername)
            SettingsViewIntent.OnFillParametersClicked -> sendEffect(SettingsViewEffect.NavigateToFillParameters)
            SettingsViewIntent.OnPromoClicked -> sendEffect(SettingsViewEffect.NavigateToPromo)
            SettingsViewIntent.OnSavedMessagesClicked -> sendEffect(SettingsViewEffect.NavigateToSavedMessages)
            SettingsViewIntent.OnStickersClicked -> sendEffect(SettingsViewEffect.NavigateToStickers)
            SettingsViewIntent.OnNotificationsClicked -> sendEffect(SettingsViewEffect.NavigateToNotifications)
            SettingsViewIntent.OnPrivacyClicked -> sendEffect(SettingsViewEffect.NavigateToPrivacy)
            SettingsViewIntent.OnDataStorageClicked -> sendEffect(SettingsViewEffect.NavigateToDataStorage)
            SettingsViewIntent.OnAppearanceClicked -> sendEffect(SettingsViewEffect.NavigateToAppearance)
            SettingsViewIntent.OnQrCodeClicked -> sendEffect(SettingsViewEffect.ShowQrCode)
            SettingsViewIntent.OnRefresh -> refreshUserData()
        }
    }

    private fun refreshUserData() {
        val userFull = MessagesController.getInstance(currentAccount)
            .getUserFull(UserConfig.getInstance(currentAccount).getClientUserId())
        setState { copy(userFull = userFull) }
    }
}