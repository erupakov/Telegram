package org.telegram.divo.screen.profile

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.telegram.divo.common.BaseViewModel
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.divo.entity.UserInfo
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class ProfileViewModel : BaseViewModel<ProfileViewState, ProfileIntent, ProfileEffect>() {

    override fun createInitialState(): ProfileViewState {
        return ProfileViewState()
    }

    override fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            is ProfileIntent.OnLoad -> loadData(userId = intent.userId, isOwnProfile = intent.isOwnProfile)
            is ProfileIntent.OpenSocialLink -> { } // openLink(intent.url)
            is ProfileIntent.OnBackgroundPhotoSelected -> {} // uploadPhoto(intent.filePath, isBackground = true)
            is ProfileIntent.OnPortfolioPhotoSelected -> {} // uploadPhoto(intent.filePath, isBackground = false)
            is ProfileIntent.OnClearPortfolioUpload -> {}
        }
    }

    fun loadData(userId: Int, isOwnProfile: Boolean) {
        viewModelScope.launch {
            setState { copy(userId = userId, isOwnProfile = isOwnProfile) }

            launch { loadUserProfile() }
            launch { loadPortfolio() }
        }
    }

    private suspend fun loadUserProfile() {
        setState { copy(isLoading = true, errorMessage = null) }

        val result: DivoResult<UserInfo> = DivoApi.userRepository.getUserById(state.value.userId)

        if (result is DivoResult.Success) {
            val userData = result.value
            val params = mapPhysicalParams(userData)
            val social = mapSocialLinks(userData.userSocialNetworks)
            val stats = UserStatistic(
                followers = userData.statistic.followersCount,
                following = userData.statistic.followingCount,
                views = userData.statistic.viewsCount,
                likes = 0, //TODO понять откуда подтягивать данные
                saves = 0, //TODO понять откуда подтягивать данные
            )
            setState {
                copy(
                    isLoading = false,
                    userInfo = userData,
                    physicalParams = params,
                    socialLinks = social,
                    statistic = stats,
                )
            }
        } else {
            val errorMsg = result.getErrorMessage()
            setState { copy(isLoading = false, errorMessage = errorMsg) }
            sendEffect(ProfileEffect.ShowError(errorMsg))
        }
    }

    private suspend fun loadPortfolio() {
        val result = DivoApi.userRepository.getUserGalleryList(state.value.userId)

        if (result is DivoResult.Success) {
            val userData = result.value
            setState {
                copy(userGalleryItems = userData)
            }
        } else {
            val errorMsg = result.getErrorMessage()
            setState { copy(isLoading = false, errorMessage = errorMsg) }
            sendEffect(ProfileEffect.ShowError(errorMsg))
        }
    }

    private fun mapPhysicalParams(user: UserInfo): PhysicalParams {
        val appearance = user.model.appearance

        return PhysicalParams(
            gender = user.gender.title,
            age = state.value.formattedAge(user.birthday),
            height = appearance.height,
            waist = appearance.waist,
            hips = appearance.hips,
            shoeSize = appearance.shoesSize,
            hairLength = appearance.hairLength.title,
            hairColor = appearance.hairColor.title,
            eyeColor = appearance.eyeColor.title,
            skinColor = appearance.skinColor.title,
            breastSize = appearance.breastSize
        )
    }

    private fun mapSocialLinks(socials: List<Any>): SocialLinks {
        return SocialLinks()
    }

//    private fun openLink(url: String) {
//        if (url.isNotEmpty()) {
//            sendEffect(ProfileEffect.OpenUrl(url))
//        }
//    }
}
