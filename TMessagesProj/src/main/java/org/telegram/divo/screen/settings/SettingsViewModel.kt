package org.telegram.divo.screen.settings

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.analytics.AnalyticsEvent
import org.telegram.divo.analytics.DivoAnalytics
import org.telegram.divo.common.arch.BaseViewModel
import org.telegram.divo.common.DivoSettings
import org.telegram.divo.common.arch.OffsetPaginator
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.messenger.UserConfig

class SettingsViewModel : BaseViewModel<SettingsViewState, SettingsViewIntent, SettingsViewEffect>() {

    val savedProfilesPaginator = OffsetPaginator { offset, limit ->
        val result = DivoApi.publicationRepository.getFollowing(limit = limit, offset = offset)
        if (result is DivoResult.Success) {
            result.value
        } else {
            throw Exception(result.getErrorMessage())
        }
    }

    init {
        DivoAnalytics.logEvent(AnalyticsEvent.SettingsOpened())
        viewModelScope.launch {
            DivoApi.userRepository.currentUserFlow.collect { user ->
                user?.let {
                    val tgUser = UserConfig.getInstance(UserConfig.selectedAccount).currentUser
                    val displayUserName = if (!tgUser?.username.isNullOrEmpty()) "@${tgUser.username}" else ""
                    setState {
                        copy(
                            userId = it.id,
                            role = it.roleLabel,
                            userName = displayUserName,
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
                sendEffect(SettingsViewEffect.NavigateToProfile())
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
            SettingsViewIntent.OnSavedProfilesClicked -> {
                logOptionTapped("saved_profiles")
                setState { copy(isSavedProfilesSheetVisible = true) }
                viewModelScope.launch {
                    savedProfilesPaginator.loadInitial()
                }
            }
            SettingsViewIntent.OnCloseSavedProfilesSheet -> {
                setState { copy(isSavedProfilesSheetVisible = false) }
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
            setState { copy(isLoading = userId == -1) }
            val result = DivoApi.userRepository.getCurrentUserInfo()

            if (result is DivoResult.Success) {
                val tgUser = UserConfig.getInstance(UserConfig.selectedAccount).currentUser
                val displayUserName = if (!tgUser?.username.isNullOrEmpty()) "@${tgUser.username}" else ""
                setState {
                    copy(
                        userId = result.value.id,
                        userName = displayUserName,
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