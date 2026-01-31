package org.telegram.divo.screen.your_parameters

import org.telegram.divo.base.BaseViewModel
import org.telegram.divo.base.ViewEffect
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLog
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
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
        data object OnLoad : YourParametersIntent()
    }

    sealed class YourParametersEffect : ViewEffect {
        data object NavigateBack : YourParametersEffect()
        data object SaveSuccess : YourParametersEffect()
        data class Error(val message: String) : YourParametersEffect()
    }

    private var currentAccount = UserConfig.selectedAccount

    init {
        loadUserProfile()
    }

    override fun createInitialState(): YourParametersViewState = YourParametersViewState(isLoading = true)

    private fun loadUserProfile() {
        setState { copy(isLoading = true) }

//        val req = TLRPC.TL_profile_getUserProfile()
//        req.user_id = MessagesController.getInstance(currentAccount).getInputUser(
//            UserConfig.getInstance(currentAccount).getClientUserId()
//        )
//
//        ConnectionsManager.getInstance(currentAccount).sendRequest(req) { response, error ->
//            AndroidUtilities.runOnUIThread {
//                if (error != null) {
//                    FileLog.e("loadUserProfile error: ${error.text} (code=${error.code})")
//                    setState { copy(isLoading = false) }
//                    return@runOnUIThread
//                }
//
//                if (response is TLRPC.TL_userProfile) {
//                    applyProfileToState(response)
//                } else {
//                    FileLog.e("loadUserProfile: unexpected response type")
//                    setState { copy(isLoading = false) }
//                }
//            }
//        }
    }

    private fun applyProfileToState(profile: TLRPC.TL_userProfile) {
        val params = profile.physical_params
        val gender = when (profile.gender) {
            is TLRPC.TL_genderMale -> "Male"
            is TLRPC.TL_genderFemale -> "Female"
            else -> ""
        }

        // Convert birth_date (stored as age or timestamp) to age
        val age = if (profile.birth_date > 0) {
            // If birth_date is stored as age directly
            if (profile.birth_date < 150) {
                profile.birth_date.toFloat()
            } else {
                // If it's a timestamp, calculate age
                val currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
                val birthYear = java.util.Calendar.getInstance().apply {
                    timeInMillis = profile.birth_date * 1000
                }.get(java.util.Calendar.YEAR)
                (currentYear - birthYear).toFloat().coerceIn(14f, 45f)
            }
        } else {
            24f
        }

//        setState {
//            copy(
//                isLoading = false,
//                gender = gender,
//                age = age,
//                height = if (params != null && params.height > 0) {
//                    // Height stored in cm, convert to meters for UI if needed
//                    if (params.height > 100) params.height / 100f else params.height.toFloat()
//                } else height,
//                waist = if (params != null && params.waist > 0) params.waist.toFloat() else waist,
//                hips = if (params != null && params.hips > 0) params.hips.toFloat() else hips,
//                shoeSize = if (params != null && params.shoe_size > 0) params.shoe_size.toFloat() else shoeSize,
//                hairLength = if (params != null && params.hair_length >= 0) params.hair_length.toFloat() else hairLength,
//                hairColor = params?.hair_color?.capitalize() ?: hairColor,
//                eyeColor = params?.eye_color?.capitalize() ?: eyeColor,
//                skinColor = params?.skin_color?.capitalize() ?: skinColor,
//                breastSize = params?.breast_size?.uppercase() ?: breastSize,
//            )
//        }
    }

    private fun String.capitalize(): String {
        return this.lowercase().replaceFirstChar { it.uppercase() }
    }

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

        // Exact flags from your TL serializeToStream()
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

        // ----------- Optional text fields (if you pass them) -----------
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

        // ----------- Gender -----------
        val genderObj: TLRPC.Gender? = when (s.gender.trim().lowercase()) {
            "male", "m" -> TLRPC.TL_genderMale()
            "female", "f" -> TLRPC.TL_genderFemale()
            else -> null
        }
        if (genderObj != null) {
            req.gender = genderObj
            req.flags = req.flags or F_GENDER
        }

        // ----------- Birth date (you store AGE; TL field is birth_date:Int32) -----------
        // If backend expects a real date/timestamp, change conversion here.
        val age = s.age.toInt()
        if (age > 0) {
            req.birth_date = age
            req.flags = req.flags or F_BIRTH_DATE
        }

        // ----------- Height -----------
        // UI range is 1.68..2.50 but label says cm, so treat <10 as meters -> cm.
        val heightCm = if (s.height < 10f) (s.height * 100f).toInt() else s.height.toInt()
        if (heightCm > 0) {
            req.height = heightCm
            req.flags = req.flags or F_HEIGHT
        }

        // ----------- Waist / Hips / Shoe -----------
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

        // ----------- Hair length -----------
        val hairLen = s.hairLength.toInt()
        if (hairLen >= 0) {
            req.hair_length = hairLen
            req.flags = req.flags or F_HAIR_LENGTH
        }

        // ----------- Colors / Breast size -----------
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

        // If you send nothing, server may accept but do nothing. Log it to catch mistakes.
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
