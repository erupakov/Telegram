package org.telegram.divo.screen.settings

import org.telegram.divo.base.BaseViewModel

class SettingsViewModel :
    BaseViewModel<SettingsViewState, SettingsViewIntent, SettingsViewEffect>() {

    override fun createInitialState(): SettingsViewState = SettingsViewState()

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
        }
    }
}