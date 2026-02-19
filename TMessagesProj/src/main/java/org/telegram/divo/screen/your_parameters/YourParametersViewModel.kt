package org.telegram.divo.screen.your_parameters

import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLog
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC

class YourParametersViewModel :
    BaseViewModel<
            YourParametersViewModel.YourParametersViewState,
            YourParametersViewModel.YourParametersIntent,
            YourParametersViewModel.YourParametersEffect
            >() {

    data class YourParametersViewState(
        val isLoading: Boolean = false,
        val gender: String = "",
        val age: Float = 24f,
        val height: Float = 168f,
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
        data object OnLoad : YourParametersIntent()
    }

    sealed class YourParametersEffect : ViewEffect {
        data object NavigateBack : YourParametersEffect()
        data object SaveSuccess : YourParametersEffect()
        data class Error(val message: String) : YourParametersEffect()
    }

    private var currentAccount = UserConfig.selectedAccount



    override fun createInitialState(): YourParametersViewState = YourParametersViewState(isLoading = true)


    fun getUserParam() {
        setState { copy(isLoading = true) }

        val req = TLRPC.TL_profile_getUserProfile()
        val user = TLRPC.TL_inputUser()

        val userFull = MessagesController.getInstance(currentAccount)
            .getUserFull(UserConfig.getInstance(currentAccount).getClientUserId())

        if (userFull?.user == null) {
            AndroidUtilities.runOnUIThread {
                setState { copy(isLoading = false) }
            }
            return
        }

        user.user_id = userFull.user.id
        user.access_hash = userFull.user.access_hash
        req.user_id = user

        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TLRPC.TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (response is TLRPC.TL_userProfile) {
                        FileLog.d("TL_profile_getUserProfile response -> $response")
                        applyProfileToState(response)
                    } else {
                        FileLog.e("TL_profile_getUserProfile failed: ${error?.text}")
                        setState { copy(isLoading = false) }
                        if (error != null) {
                            sendEffect(YourParametersEffect.Error(error.text ?: "Failed to load profile"))
                        }
                    }
                }
            },
        )
    }

    fun loadUserProfile() {
        getUserParam()
    }

    private fun applyProfileToState(profile: TLRPC.TL_userProfile) {
        val params = profile.physical_params
        val gender = when (profile.gender) {
            is TLRPC.TL_genderMale -> "Male"
            is TLRPC.TL_genderFemale -> "Female"
            else -> ""
        }

        setState {
            copy(
                isLoading = false,
                gender = gender,
                age = params?.age?.takeIf { it > 0 }?.toFloat() ?: age,
                height = params?.height?.takeIf { it > 0 }?.toFloat() ?: height,
                waist = params?.waist?.takeIf { it > 0 }?.toFloat() ?: waist,
                hips = params?.hips?.takeIf { it > 0 }?.toFloat() ?: hips,
                shoeSize = params?.shoe_size?.takeIf { it > 0 }?.toFloat() ?: shoeSize,
                hairLength = params?.hair_length?.takeIf { it >= 0 }?.toFloat() ?: hairLength,
                hairColor = params?.hair_color?.capitalize() ?: hairColor,
                eyeColor = params?.eye_color?.capitalize() ?: eyeColor,
                skinColor = params?.skin_color?.capitalize() ?: skinColor,
                breastSize = params?.breast_size?.uppercase() ?: breastSize,
            )
        }
    }

    private fun String.capitalize(): String {
        return this.lowercase().replaceFirstChar { it.uppercase() }
    }

    override fun handleIntent(intent: YourParametersIntent) {
        when (intent) {
            is YourParametersIntent.OnGenderChanged -> setState {
                val newBreastSize = if (intent.gender == "Male") "" else breastSize
                copy(gender = intent.gender, breastSize = newBreastSize)
            }
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
            is YourParametersIntent.OnSaveClicked -> saveProfile()
            is YourParametersIntent.OnLoad -> loadUserProfile()
        }
    }

    private fun saveProfile() {
        setState { copy(isLoading = true) }
        updateProfile()
    }

    private fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        about: String? = null,
        country: String? = null,
        photoId: Long? = null,
    ) {
        val s = state.value

        val F_FIRST_NAME = 1
        val F_LAST_NAME = 2
        val F_COUNTRY = 4
        val F_ABOUT = 8
        val F_GENDER = 16
        val F_BIRTH_DATE = 32
        val F_HEIGHT = 64
        val F_WAIST = 128
        val F_HIPS = 256
        val F_SHOE_SIZE = 512
        val F_HAIR_LENGTH = 1024
        val F_HAIR_COLOR = 2048
        val F_EYE_COLOR = 4096
        val F_SKIN_COLOR = 8192
        val F_BREAST_SIZE = 16384
        val F_PHOTO_ID = 32768

        val req = TLRPC.TL_profile_updateProfile()
        req.flags = 0

        val fName = firstName?.trim().orEmpty()
        if (fName.isNotEmpty()) {
            req.first_name = fName
            req.flags = req.flags or F_FIRST_NAME
        }

        val lName = lastName?.trim().orEmpty()
        if (lName.isNotEmpty()) {
            req.last_name = lName
            req.flags = req.flags or F_LAST_NAME
        }

        val ctry = country?.trim().orEmpty()
        if (ctry.isNotEmpty()) {
            req.country = ctry
            req.flags = req.flags or F_COUNTRY
        }

        val abt = about?.trim().orEmpty()
        if (abt.isNotEmpty()) {
            req.about = abt
            req.flags = req.flags or F_ABOUT
        }

        if (photoId != null && photoId != 0L) {
            req.photo_id = photoId
            req.flags = req.flags or F_PHOTO_ID
        }

        val genderObj: TLRPC.Gender? = when (s.gender.trim().lowercase()) {
            "male", "m" -> TLRPC.TL_genderMale()
            "female", "f" -> TLRPC.TL_genderFemale()
            else -> null
        }
        if (genderObj != null) {
            req.gender = genderObj
            req.flags = req.flags or F_GENDER
        }

        val age = s.age.toInt()
        if (age > 0) {
            req.birth_date = age
            req.flags = req.flags or F_BIRTH_DATE
        }

        val heightCm = s.height.toInt()
        if (heightCm > 0) {
            req.height = heightCm
            req.flags = req.flags or F_HEIGHT
        }

        val waist = s.waist.toInt()
        if (waist > 0) {
            req.waist = waist
            req.flags = req.flags or F_WAIST
        }

        val hips = s.hips.toInt()
        if (hips > 0) {
            req.hips = hips
            req.flags = req.flags or F_HIPS
        }

        val shoe = s.shoeSize.toInt()
        if (shoe > 0) {
            req.shoe_size = shoe
            req.flags = req.flags or F_SHOE_SIZE
        }

        val hairLen = s.hairLength.toInt()
        if (hairLen >= 0) {
            req.hair_length = hairLen
            req.flags = req.flags or F_HAIR_LENGTH
        }

        val hairColor = s.hairColor.trim()
        if (hairColor.isNotEmpty()) {
            req.hair_color = hairColor
            req.flags = req.flags or F_HAIR_COLOR
        }

        val eyeColor = s.eyeColor.trim()
        if (eyeColor.isNotEmpty()) {
            req.eye_color = eyeColor
            req.flags = req.flags or F_EYE_COLOR
        }

        val skinColor = s.skinColor.trim()
        if (skinColor.isNotEmpty()) {
            req.skin_color = skinColor
            req.flags = req.flags or F_SKIN_COLOR
        }

        val breastSize = s.breastSize.trim()
        if (breastSize.isNotEmpty()) {
            req.breast_size = breastSize
            req.flags = req.flags or F_BREAST_SIZE
        }

        FileLog.d("updateProfile flags=${req.flags}")

        ConnectionsManager.getInstance(currentAccount).sendRequest(req) { response, error ->
            AndroidUtilities.runOnUIThread {
                setState { copy(isLoading = false) }

                if (error != null) {
                    val msg = "${error.text ?: "Unknown error"} (code=${error.code})"
                    FileLog.e("updateProfile error: $msg")
                    sendEffect(YourParametersEffect.Error(msg))
                } else {
                    FileLog.d("updateProfile success")
                    sendEffect(YourParametersEffect.SaveSuccess)
                }
            }
        }
    }
}
