package org.telegram.divo.screen.your_parameters

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.entity.UserInfo

data class YourParametersViewState(
    val isLoading: Boolean = false,
    val userFull: UserInfo = UserInfo(),
    val touchedFields: Set<ParameterField> = emptySet(),
    val errorMessage: String = "",
) : ViewState

sealed class YourParametersIntent : ViewIntent {
    data class OnGenderChanged(val gender: String) : YourParametersIntent()
    data class OnAgeChanged(val age: Float) : YourParametersIntent()
    data class OnHeightChanged(val height: Float) : YourParametersIntent()
    data class OnWaistChanged(val waist: Float) : YourParametersIntent()
    data class OnHipsChanged(val hips: Float) : YourParametersIntent()
    data class OnShoeSizeChanged(val shoeSize: Float) : YourParametersIntent()
    data class OnHairLengthChanged(val hairLength: Int, val title: String) : YourParametersIntent()
    data class OnHairColorChanged(val color: Int, val title: String) : YourParametersIntent()
    data class OnEyeColorChanged(val color: Int, val title: String) : YourParametersIntent()
    data class OnSkinColorChanged(val color: Int, val title: String) : YourParametersIntent()
    data class OnBreastSizeChanged(val size: String) : YourParametersIntent()
    data object OnBackClicked : YourParametersIntent()
    data object OnSaveClicked : YourParametersIntent()
    data object OnLoad : YourParametersIntent()
}

sealed class YourParametersEffect : ViewEffect {
    data object NavigateBack : YourParametersEffect()
    data object SaveSuccess : YourParametersEffect()
    data class Error(val message: String) : YourParametersEffect()
}

enum class ParameterField {
    GENDER, HAIR_LENGTH, HAIR_COLOR, EYE_COLOR, SKIN_COLOR, BREAST_SIZE
}