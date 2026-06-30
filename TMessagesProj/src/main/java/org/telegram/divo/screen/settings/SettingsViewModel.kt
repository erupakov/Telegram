package org.telegram.divo.screen.settings

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.analytics.AnalyticsEvent
import org.telegram.divo.analytics.DivoAnalytics
import org.telegram.divo.common.arch.BaseViewModel
import org.telegram.divo.common.DivoSettings
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage

class SettingsViewModel : BaseViewModel<SettingsViewState, SettingsViewIntent, SettingsViewEffect>() {

    init {
        DivoAnalytics.logEvent(AnalyticsEvent.SettingsOpened())
        viewModelScope.launch {
            DivoApi.userRepository.currentUserFlow.collect { user ->
                user?.let {
                    setState {
                        copy(
                            userId = it.id,
                            role = it.roleLabel,
                            userName = it.fullName,
                            avatarUrl = it.avatarUrl,
                            phoneNumber = it.phone,
                            isModel = it.role.isModel(),
                            measuringSystem = DivoSettings.measuringSystem
                        )
                    }
                }
            }
        }
        loadUserData()
    }

    override fun createInitialState(): SettingsViewState = SettingsViewState()

    private fun logOptionTapped(option: String) {
        DivoAnalytics.logEvent(AnalyticsEvent.SettingsOptionTapped(option))
    }

    override fun handleIntent(intent: SettingsViewIntent) {
        when (intent) {
            SettingsViewIntent.OnEditProfileClicked -> {
                logOptionTapped("edit_profile")
                sendEffect(SettingsViewEffect.NavigateToEditProfile)
            }
            SettingsViewIntent.OnOpenProfileClicked -> {
                logOptionTapped("open_profile")
                sendEffect(SettingsViewEffect.NavigateToProfile)
            }
            SettingsViewIntent.OnSetUsernameClicked -> {
                logOptionTapped("set_username")
                sendEffect(SettingsViewEffect.NavigateToSetUsername)
            }
            SettingsViewIntent.OnFillParametersClicked -> {
                logOptionTapped("fill_parameters")
                sendEffect(SettingsViewEffect.NavigateToFillParameters)
            }
            SettingsViewIntent.OnPromoClicked -> {
                logOptionTapped("promo")
                sendEffect(SettingsViewEffect.NavigateToPromo)
            }
            SettingsViewIntent.OnSavedMessagesClicked -> {
                logOptionTapped("saved_messages")
                sendEffect(SettingsViewEffect.NavigateToSavedMessages)
            }
            SettingsViewIntent.OnNotificationsClicked -> {
                logOptionTapped("notifications")
                sendEffect(SettingsViewEffect.NavigateToNotifications)
            }
            SettingsViewIntent.OnPrivacyClicked -> {
                logOptionTapped("privacy")
                sendEffect(SettingsViewEffect.NavigateToPrivacy)
            }
            SettingsViewIntent.OnDataStorageClicked -> {
                logOptionTapped("data_storage")
                sendEffect(SettingsViewEffect.NavigateToDataStorage)
            }
            SettingsViewIntent.OnAppearanceClicked -> {
                logOptionTapped("appearance")
                sendEffect(SettingsViewEffect.NavigateToAppearance)
            }
            SettingsViewIntent.OnLanguageClicked -> {
                logOptionTapped("language")
                sendEffect(SettingsViewEffect.NavigateToLanguage)
            }
            SettingsViewIntent.OnRefresh -> loadUserData()
            SettingsViewIntent.OnLogoutClicked -> {
                logOptionTapped("logout")
                sendEffect(SettingsViewEffect.NavigateToLogout)
            }
            SettingsViewIntent.OnMeasuringSystemClicked -> {
                logOptionTapped("measuring_system")
                sendEffect(SettingsViewEffect.ShowMeasuringSystemDialog)
            }
            SettingsViewIntent.OnQrCodeClicked -> {
                logOptionTapped("qr_code")
                sendEffect(SettingsViewEffect.ShowQrCode)
            }
            is SettingsViewIntent.OnChangeMeasuringSystem -> {
                val newSystem = intent.system
                DivoAnalytics.logEvent(AnalyticsEvent.MeasurementSystemChanged(newSystem))
                DivoSettings.measuringSystem = newSystem
                setState { copy(measuringSystem = newSystem) }
                // Also update profile silently if needed
                viewModelScope.launch {
                    val userResult = DivoApi.userRepository.getCurrentUserInfo()
                    if (userResult is DivoResult.Success) {
                        val updatedUser = userResult.value.copy(measuringSystem = newSystem)
                        DivoApi.userRepository.updateProfile(updatedUser)
                    }
                }
            }
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
                        phoneNumber = result.value.phone,
                        isModel = result.value.role.isModel(),
                        measuringSystem = DivoSettings.measuringSystem,
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