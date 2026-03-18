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
                copy(
                    userFull = userFull.copy(gender = gender),
                    touchedFields = touchedFields + ParameterField.GENDER
                )
            }
            is YourParametersIntent.OnAgeChanged -> setState {
                copy(userFull = userFull.copy(birthday = intent.age.toDateString()))
            }
            is YourParametersIntent.OnHeightChanged -> updateAppearance { copy(height = intent.height) }
            is YourParametersIntent.OnWaistChanged -> updateAppearance { copy(waist = intent.waist) }
            is YourParametersIntent.OnHipsChanged -> updateAppearance { copy(hips = intent.hips) }
            is YourParametersIntent.OnShoeSizeChanged -> updateAppearance { copy(shoesSize = intent.shoeSize) }
            is YourParametersIntent.OnHairLengthChanged -> setState {
                val appearance = userFull.model?.appearance?.copy(
                    hairLength = userFull.model.appearance.hairLength.copy(id = intent.hairLength, title = intent.title)
                )
                copy(
                    userFull = userFull.copy(model = userFull.model?.copy(appearance = appearance)),
                    touchedFields = touchedFields + ParameterField.HAIR_LENGTH
                )
            }
            is YourParametersIntent.OnHairColorChanged -> setState {
                val appearance = userFull.model?.appearance?.copy(
                    hairColor = userFull.model.appearance.hairColor.copy(id = intent.color, title = intent.title)
                )
                copy(
                    userFull = userFull.copy(model = userFull.model?.copy(appearance = appearance)),
                    touchedFields = touchedFields + ParameterField.HAIR_COLOR
                )
            }
            is YourParametersIntent.OnEyeColorChanged -> setState {
                val appearance = userFull.model?.appearance?.copy(
                    eyeColor = userFull.model.appearance.eyeColor.copy(id = intent.color, title = intent.title)
                )
                copy(
                    userFull = userFull.copy(model = userFull.model?.copy(appearance = appearance)),
                    touchedFields = touchedFields + ParameterField.EYE_COLOR
                )
            }
            is YourParametersIntent.OnSkinColorChanged -> setState {
                val appearance = userFull.model?.appearance?.copy(
                    skinColor = userFull.model.appearance.skinColor.copy(id = intent.color, title = intent.title)
                )
                copy(
                    userFull = userFull.copy(model = userFull.model?.copy(appearance = appearance)),
                    touchedFields = touchedFields + ParameterField.SKIN_COLOR
                )
            }
            is YourParametersIntent.OnBreastSizeChanged -> setState {
                val appearance = userFull.model?.appearance?.copy(breastSize = intent.size)
                copy(
                    userFull = userFull.copy(model = userFull.model?.copy(appearance = appearance)),
                    touchedFields = touchedFields + ParameterField.BREAST_SIZE
                )
            }
            is YourParametersIntent.OnBackClicked -> sendEffect(YourParametersEffect.NavigateBack)
            is YourParametersIntent.OnSaveClicked -> updateProfile()
            is YourParametersIntent.OnLoad -> loadUserProfile()
        }
    }

    private fun updateAppearance(update: Appearance.() -> Appearance) {
        setState {
            val appearance = userFull.model?.appearance?.update()
            val model = userFull.model?.copy(appearance = appearance)
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
