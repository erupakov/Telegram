package org.telegram.divo.screen.settings

import org.telegram.divo.base.BaseViewModel

class SettingsViewModel :
    BaseViewModel<SettingsViewState, SettingsViewIntent, SettingsViewAction>() {

    override fun createInitialState(): SettingsViewState = SettingsViewState()

    override fun handleIntent(intent: SettingsViewIntent) {
        when (intent) {
            SettingsViewIntent.OnEditProfileClicked -> sendAction(SettingsViewAction.NavigateToEditProfile)
            SettingsViewIntent.OnOpenProfileClicked -> sendAction(SettingsViewAction.NavigateToProfile)
            SettingsViewIntent.OnSetUsernameClicked -> sendAction(SettingsViewAction.NavigateToSetUsername)
            SettingsViewIntent.OnFillParametersClicked -> sendAction(SettingsViewAction.NavigateToFillParameters)
            SettingsViewIntent.OnPromoClicked -> sendAction(SettingsViewAction.NavigateToPromo)
            SettingsViewIntent.OnSavedMessagesClicked -> sendAction(SettingsViewAction.NavigateToSavedMessages)
            SettingsViewIntent.OnStickersClicked -> sendAction(SettingsViewAction.NavigateToStickers)
            SettingsViewIntent.OnNotificationsClicked -> sendAction(SettingsViewAction.NavigateToNotifications)
            SettingsViewIntent.OnPrivacyClicked -> sendAction(SettingsViewAction.NavigateToPrivacy)
            SettingsViewIntent.OnDataStorageClicked -> sendAction(SettingsViewAction.NavigateToDataStorage)
            SettingsViewIntent.OnAppearanceClicked -> sendAction(SettingsViewAction.NavigateToAppearance)
            SettingsViewIntent.OnQrCodeClicked -> sendAction(SettingsViewAction.ShowQrCode)
        }
    }
}