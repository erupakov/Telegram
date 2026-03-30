package org.telegram.divo.screen.your_parameters

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.EyeColor
import org.telegram.divo.entity.Gender
import org.telegram.divo.entity.HairColor
import org.telegram.divo.entity.HairLength
import org.telegram.divo.entity.SkinColor
import org.telegram.divo.entity.UserInfo
import org.telegram.divo.screen.your_parameters.components.ParametersType
import org.telegram.divo.screen.your_parameters.components.ProfileParameter

class YourParametersViewModel : BaseViewModel<YourParametersViewState, YourParametersIntent, YourParametersEffect>() {

    override fun createInitialState(): YourParametersViewState = YourParametersViewState(isLoading = true)

    override fun handleIntent(intent: YourParametersIntent) {
        when (intent) {
            is YourParametersIntent.OnLoad -> loadUserProfile()
            is YourParametersIntent.OnBackClicked -> sendEffect(YourParametersEffect.NavigateBack)
            is YourParametersIntent.OnSaveClicked -> if (intent.isEditScreen) sendEffect(YourParametersEffect.SaveSuccess) else updateProfile()
            is YourParametersIntent.OnParamValueChanged -> {
                val newId = findIdByTitle(intent.type, intent.value)
                updateParameterState(intent.type, intent.value, newId)
            }
            is YourParametersIntent.OnParamCleared -> {
                updateParameterState(intent.type, "", null)
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            setState { copy(isLoading = true, isError = false) }

            val userDeferred = async { DivoApi.userRepository.getCurrentUserInfo() }
            val dictDeferred = async { DivoApi.userRepository.getAppearances() }

            val userResult = userDeferred.await()
            val dictResult = dictDeferred.await()

            if (userResult is DivoResult.Success && dictResult is DivoResult.Success) {
                val user = userResult.value
                val dict = dictResult.value

                setState {
                    copy(
                        isLoading = false,
                        isError = false,
                        user = user,
                        gender = ProfileParameter(ParametersType.GENDER, user.gender?.title.orEmpty()),
                        blockParams = user.mapToBlockParams(),
                        hairLength = ProfileParameter(ParametersType.HAIR_LENGTH, user.model?.appearance?.hairLength?.title.orEmpty(), user.model?.appearance?.hairLength?.id),
                        hairColor = ProfileParameter(ParametersType.HAIR_COLOR, user.model?.appearance?.hairColor?.title.orEmpty(), user.model?.appearance?.hairColor?.id),
                        eyeColor = ProfileParameter(ParametersType.EYE_COLOR, user.model?.appearance?.eyeColor?.title.orEmpty(), user.model?.appearance?.eyeColor?.id),
                        skinColor = ProfileParameter(ParametersType.SKIN_COLOR, user.model?.appearance?.skinColor?.title.orEmpty(), user.model?.appearance?.skinColor?.id),
                        breastSize = ProfileParameter(ParametersType.BREAST_SIZE, user.model?.appearance?.breastSize.orEmpty()),

                        hairLengthOptions = dict.hairLength.orEmpty(),
                        hairColorOptions = dict.hairColor.orEmpty(),
                        eyeColorOptions = dict.eyeColor.orEmpty(),
                        skinColorOptions = dict.skinColor.orEmpty()
                    )
                }
            } else {
                val errorMessage = if (userResult !is DivoResult.Success) {
                    userResult.getErrorMessage()
                } else {
                    dictResult.getErrorMessage()
                }

                setState { copy(isLoading = false, isError = true) }
                sendEffect(YourParametersEffect.Error(errorMessage))
            }
        }
    }

    private fun updateProfile() {
        viewModelScope.launch {
            setState { copy(isSaving = true) }

            val result = DivoApi.userRepository.updateProfile(
                userInfo = state.value.toUserInfo()
            )

            when (result) {
                is DivoResult.Success -> {
                    setState { copy(isSaving = false) }
                    sendEffect(YourParametersEffect.SaveSuccess)
                }
                else -> {
                    setState { copy(isSaving = false) }
                    sendEffect(YourParametersEffect.Error(result.getErrorMessage()))
                }
            }
        }
    }

    private fun updateParameterState(type: ParametersType, newValue: String, newId: Int? = null) {
        setState {
            val isBlockParam = blockParams.any { it.type == type }

            if (isBlockParam) {
                val updatedBlockParams = blockParams.map { param ->
                    if (param.type == type) param.copy(value = newValue) else param
                }
                copy(blockParams = updatedBlockParams)
            } else {
                when (type) {
                    ParametersType.GENDER -> copy(gender = gender?.copy(value = newValue))
                    ParametersType.HAIR_LENGTH -> copy(hairLength = hairLength?.copy(value = newValue, id = newId))
                    ParametersType.HAIR_COLOR -> copy(hairColor = hairColor?.copy(value = newValue, id = newId))
                    ParametersType.EYE_COLOR -> copy(eyeColor = eyeColor?.copy(value = newValue, id = newId))
                    ParametersType.SKIN_COLOR -> copy(skinColor = skinColor?.copy(value = newValue, id = newId))
                    ParametersType.BREAST_SIZE -> copy(breastSize = breastSize?.copy(value = newValue))
                    else -> this
                }
            }
        }
    }

    private fun UserInfo.mapToBlockParams(): List<ProfileParameter> =
        listOf(
            ProfileParameter(ParametersType.AGE, birthday),
            ProfileParameter(ParametersType.HEIGHT, model?.appearance?.height?.let { "$it" }.orEmpty()),
            ProfileParameter(ParametersType.WAIST, model?.appearance?.waist?.let { "$it" }.orEmpty()),
            ProfileParameter(ParametersType.HIPS, model?.appearance?.hips?.let { "$it" }.orEmpty()),
            ProfileParameter(ParametersType.SHOE_SIZE, model?.appearance?.shoesSize?.let { "$it" }.orEmpty()),
        )

    private fun YourParametersViewState.toUserInfo(): UserInfo {
        val appearance = user.model?.appearance

        val ageStr = blockParams.getValue(ParametersType.AGE)
        val heightStr = blockParams.getValue(ParametersType.HEIGHT)
        val waistStr = blockParams.getValue(ParametersType.WAIST)
        val hipsStr = blockParams.getValue(ParametersType.HIPS)
        val shoesSizeStr = blockParams.getValue(ParametersType.SHOE_SIZE)

        val parsedHeight = heightStr?.toFloatOrNull()
        val parsedWaist = waistStr?.toFloatOrNull()
        val parsedHips = hipsStr?.toFloatOrNull()
        val parsedShoesSize = shoesSizeStr?.toFloatOrNull()

        val updatedUserInfo = user.copy(
            gender = gender?.let { Gender(id = it.value.lowercase(), title = it.value) },
            birthday = ageStr.orEmpty(),
            model = user.model?.copy(
                appearance = appearance?.copy(
                    height = parsedHeight,
                    waist = parsedWaist,
                    hips = parsedHips,
                    shoesSize = parsedShoesSize,

                    hairLength = hairLength?.let { HairLength(title = it.value, id = it.id ) },
                    hairColor = hairColor?.let { HairColor(title = it.value, id = it.id ) },
                    eyeColor = eyeColor?.let { EyeColor(title = it.value, id = it.id ) },
                    skinColor = skinColor?.let { SkinColor(title = it.value, id = it.id ) },
                    breastSize = breastSize?.value
                )
            )
        )

        return updatedUserInfo
    }


    private fun List<ProfileParameter>.getValue(type: ParametersType): String? {
        return firstOrNull { it.type == type }?.value
    }

    private fun findIdByTitle(type: ParametersType, title: String): Int? {
        val currentState = state.value
        return when (type) {
            ParametersType.HAIR_LENGTH -> currentState.hairLengthOptions.find { it.title == title }?.id
            ParametersType.HAIR_COLOR -> currentState.hairColorOptions.find { it.title == title }?.id
            ParametersType.EYE_COLOR -> currentState.eyeColorOptions.find { it.title == title }?.id
            ParametersType.SKIN_COLOR -> currentState.skinColorOptions.find { it.title == title }?.id
            else -> null
        }
    }
}
