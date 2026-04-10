package org.telegram.divo.screen.your_parameters

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.items.ProfileParameter
import org.telegram.divo.entity.AppearanceItem
import org.telegram.divo.entity.UserInfo

data class YourParametersViewState(
    val user: UserInfo = UserInfo(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isError: Boolean = false,
    val gender: ProfileParameter? = null,
    val blockParams: List<ProfileParameter> = listOf(),
    val hairLength: ProfileParameter? = null,
    val hairColor: ProfileParameter? = null,
    val eyeColor: ProfileParameter? = null,
    val skinColor: ProfileParameter? = null,
    val breastSize: ProfileParameter? = null,
    val hairLengthOptions: List<AppearanceItem> = emptyList(),
    val hairColorOptions: List<AppearanceItem> = emptyList(),
    val eyeColorOptions: List<AppearanceItem> = emptyList(),
    val skinColorOptions: List<AppearanceItem> = emptyList(),
) : ViewState

sealed class YourParametersIntent : ViewIntent {
    data object OnBackClicked : YourParametersIntent()
    data class OnSaveClicked(val isEditScreen: Boolean) : YourParametersIntent()
    data object OnLoad : YourParametersIntent()
    data class OnParamValueChanged(val type: ParametersType, val value: String) : YourParametersIntent()
    data class OnParamCleared(val type: ParametersType) : YourParametersIntent()
}

sealed class YourParametersEffect : ViewEffect {
    data object NavigateBack : YourParametersEffect()
    data object SaveSuccess : YourParametersEffect()
    data class Error(val message: String) : YourParametersEffect()
}