package org.telegram.divo.screen.your_parameters

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.common.toDateString
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.Appearance
import org.telegram.divo.entity.Gender

class YourParametersViewModel : BaseViewModel<YourParametersViewState, YourParametersIntent, YourParametersEffect>() {

    override fun createInitialState(): YourParametersViewState = YourParametersViewState(isLoading = true)

    override fun handleIntent(intent: YourParametersIntent) {
        when (intent) {
            is YourParametersIntent.OnGenderChanged -> setState {
                val gender = Gender(id = intent.gender.lowercase(), title = intent.gender)
                copy(userFull = userFull.copy(gender = gender))
            }
            is YourParametersIntent.OnAgeChanged -> setState {
                copy(userFull = userFull.copy(birthday = intent.age.toDateString()))
            }
            is YourParametersIntent.OnHeightChanged -> updateAppearance { copy(height = intent.height) }
            is YourParametersIntent.OnWaistChanged -> updateAppearance { copy(waist = intent.waist) }
            is YourParametersIntent.OnHipsChanged -> updateAppearance { copy(hips = intent.hips) }
            is YourParametersIntent.OnShoeSizeChanged -> updateAppearance { copy(shoesSize = intent.shoeSize) }
            is YourParametersIntent.OnHairLengthChanged -> updateAppearance { copy(hairLength = hairLength.copy(id = intent.hairLength, title = intent.title)) }
            is YourParametersIntent.OnHairColorChanged -> updateAppearance { copy(hairColor = hairColor.copy(id = intent.color, title = intent.title)) }
            is YourParametersIntent.OnEyeColorChanged -> updateAppearance { copy(eyeColor = eyeColor.copy(id = intent.color, title = intent.title)) }
            is YourParametersIntent.OnSkinColorChanged -> updateAppearance { copy(skinColor = skinColor.copy(id = intent.color, title = intent.title)) }
            is YourParametersIntent.OnBreastSizeChanged -> updateAppearance { copy(breastSize = intent.size) }
            is YourParametersIntent.OnBackClicked -> sendEffect(YourParametersEffect.NavigateBack)
            is YourParametersIntent.OnSaveClicked -> updateProfile()
            is YourParametersIntent.OnLoad -> loadUserProfile()
        }
    }

    private fun updateAppearance(update: Appearance.() -> Appearance) {
        setState {
            val appearance = userFull.model.appearance.update()
            val model = userFull.model.copy(appearance = appearance)
            copy(userFull = userFull.copy(model = model))
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }
            val result = DivoApi.userRepository.getCurrentUserInfo()

            if (result is DivoResult.Success) {
                setState {
                    copy(
                        userFull = result.value,
                        isLoading = false
                    )
                }
            } else {
                val errorMsg = result.getErrorMessage()
                setState { copy(isLoading = false, errorMessage = errorMsg) }
            }
        }
    }

    private fun updateProfile() {
        viewModelScope.launch {
            setState { copy(isLoading = true) }

            val result = DivoApi.userRepository.updateProfile(
                userInfo = state.value.userFull
            )

            when (result) {
                is DivoResult.Success -> {
                    setState { copy(isLoading = false) }
                    sendEffect(YourParametersEffect.SaveSuccess)
                }
                else -> {
                    setState { copy(isLoading = false, errorMessage = result.getErrorMessage()) }
                }
            }
        }
    }
}
