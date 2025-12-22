package org.telegram.divo.screen.your_parameters

import org.telegram.divo.base.BaseViewModel

class YourParametersViewModel :
    BaseViewModel<YourParametersViewState, YourParametersIntent, YourParametersEffect>() {

    override fun createInitialState(): YourParametersViewState = YourParametersViewState()

    override fun handleIntent(intent: YourParametersIntent) {
        when (intent) {
            is YourParametersIntent.OnGenderChanged -> setState { copy(gender = intent.gender) }
            is YourParametersIntent.OnAgeChanged -> setState { copy(age = intent.age) }
            is YourParametersIntent.OnHeightChanged -> setState { copy(height = intent.height) }
            is YourParametersIntent.OnWaistChanged -> setState { copy(waist = intent.waist) }
            is YourParametersIntent.OnHipsChanged -> setState { copy(hips = intent.hips) }
            is YourParametersIntent.OnShoeSizeChanged -> setState { copy(shoeSize = intent.shoeSize) }
            is YourParametersIntent.OnHairLengthChanged -> setState { copy(hairLength = intent.hairLength) }
            is YourParametersIntent.OnHairColorChanged -> setState { copy(hairColor = intent.color) }
            is YourParametersIntent.OnEyeColorChanged -> setState { copy(eyeColor = intent.color) }
            is YourParametersIntent.OnSkinColorChanged -> setState { copy(skinColor = intent.color) }
            is YourParametersIntent.OnBreastSizeChanged -> setState { copy(breastSize = intent.size) }
            is YourParametersIntent.OnBackClicked -> sendEffect(YourParametersEffect.NavigateBack)
            is YourParametersIntent.OnSaveClicked -> {
                // TODO: Save parameters
            }
        }
    }
}