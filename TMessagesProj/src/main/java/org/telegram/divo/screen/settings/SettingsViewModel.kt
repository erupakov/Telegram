package org.telegram.divo.screen.settings

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage

class SettingsViewModel : BaseViewModel<SettingsViewState, SettingsViewIntent, SettingsViewEffect>() {

    init {
        viewModelScope.launch {
            DivoApi.userRepository.currentUserFlow.collect { user ->
                user?.let {
                    setState {
                        copy(
                            userId = it.id,
                            userName = it.fullName,
                            avatarUrl = it.avatarUrl,
                            isModel = it.role.isModel()
                        )
                    }
                }
            }
        }
        loadUserData()
    }

    override fun createInitialState(): SettingsViewState = SettingsViewState()

    override fun handleIntent(intent: SettingsViewIntent) {
        when (intent) {
            SettingsViewIntent.OnEditProfileClicked -> sendEffect(SettingsViewEffect.NavigateToEditProfile)
            SettingsViewIntent.OnOpenProfileClicked -> sendEffect(SettingsViewEffect.NavigateToProfile)
            SettingsViewIntent.OnSetUsernameClicked -> sendEffect(SettingsViewEffect.NavigateToSetUsername)
            SettingsViewIntent.OnFillParametersClicked -> sendEffect(SettingsViewEffect.NavigateToFillParameters)
            SettingsViewIntent.OnPromoClicked -> sendEffect(SettingsViewEffect.NavigateToPromo)
            SettingsViewIntent.OnSavedMessagesClicked -> sendEffect(SettingsViewEffect.NavigateToSavedMessages)
            SettingsViewIntent.OnNotificationsClicked -> sendEffect(SettingsViewEffect.NavigateToNotifications)
            SettingsViewIntent.OnPrivacyClicked -> sendEffect(SettingsViewEffect.NavigateToPrivacy)
            SettingsViewIntent.OnDataStorageClicked -> sendEffect(SettingsViewEffect.NavigateToDataStorage)
            SettingsViewIntent.OnAppearanceClicked -> sendEffect(SettingsViewEffect.NavigateToAppearance)
            SettingsViewIntent.OnQrCodeClicked -> sendEffect(SettingsViewEffect.ShowQrCode)
            SettingsViewIntent.OnRefresh -> loadUserData()
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = DivoApi.userRepository.getCurrentUserInfo()

            if (result is DivoResult.Success) {
                setState {
                    copy(
                        userId = result.value.id,
                        userName = result.value.fullName,
                        avatarUrl = result.value.avatarUrl,
                        isModel = result.value.role.isModel(),
                        isLoading = false
                    )
                }
            } else {
                setState { copy(isLoading = false) }
                sendEffect(SettingsViewEffect.ShowError(result.getErrorMessage()))
            }
        }
    }
}