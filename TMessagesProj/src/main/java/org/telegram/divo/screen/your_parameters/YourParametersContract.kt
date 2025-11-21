package org.telegram.divo.screen.your_parameters

import org.telegram.divo.base.ViewAction
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState

data class YourParametersViewState(
    val gender: String = "",
    val age: Float = 24f,
    val height: Float = 1.68f,
    val waist: Float = 48f,
    val hips: Float = 80f,
    val shoeSize: Float = 36f,
    val hairLength: Float = 0f,
    val hairColor: String = "",
    val eyeColor: String = "",
    val skinColor: String = "",
    val breastSize: String = "",
) : ViewState

sealed class YourParametersIntent : ViewIntent {
    data class OnGenderChanged(val gender: String) : YourParametersIntent()
    data class OnAgeChanged(val age: Float) : YourParametersIntent()
    data class OnHeightChanged(val height: Float) : YourParametersIntent()
    data class OnWaistChanged(val waist: Float) : YourParametersIntent()
    data class OnHipsChanged(val hips: Float) : YourParametersIntent()
    data class OnShoeSizeChanged(val shoeSize: Float) : YourParametersIntent()
    data class OnHairLengthChanged(val hairLength: Float) : YourParametersIntent()
    data class OnHairColorChanged(val color: String) : YourParametersIntent()
    data class OnEyeColorChanged(val color: String) : YourParametersIntent()
    data class OnSkinColorChanged(val color: String) : YourParametersIntent()
    data class OnBreastSizeChanged(val size: String) : YourParametersIntent()
    data object OnBackClicked : YourParametersIntent()
    data object OnSaveClicked : YourParametersIntent()
}

sealed class YourParametersAction : ViewAction {
    data object NavigateBack : YourParametersAction()
}