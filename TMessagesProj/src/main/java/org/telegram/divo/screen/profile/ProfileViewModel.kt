package org.telegram.divo.screen.profile

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.telegram.divo.base.BaseViewModel
import org.telegram.divo.base.ViewEffect
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLRPC

class ProfileViewModel :
    BaseViewModel<ProfileViewModel.ProfileViewState, ProfileViewModel.ProfileIntent, ProfileViewModel.ProfileEffect>() {

    data class ProfileViewState(
        val userFull: TLRPC.UserFull,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val userPhotos: List<TLRPC.Photo> = emptyList(),
        val portfolioItems: List<TLRPC.TL_profile_portfolioItem> = emptyList(),
        val portfolioLoading: Boolean = false,
        val portfolioUploading: Boolean = false,
        val portfolioUploadLocalPath: String? = null
    ) : ViewState

    sealed class ProfileIntent : ViewIntent {
        data object OnLoad : ProfileIntent()
        data object LoadPortfolio : ProfileIntent()
        data class OnPortfolioPhotoSelected(
            val photo: TLRPC.InputFile,
            val localPath: String?
        ) : ProfileIntent()
        data object OnClearPortfolioUpload : ProfileIntent()
    }

    sealed class ProfileEffect : ViewEffect {
        data object NavigateToSearch : ProfileEffect()
        data object NavigateToCreateEvent : ProfileEffect()
        data class NavigateToEventDetails(val eventId: Long) : ProfileEffect()
    }

    override fun createInitialState(): ProfileViewState {
        val userFull = MessagesController.getInstance(currentAccount)
            .getUserFull(UserConfig.getInstance(currentAccount).getClientUserId())

        return ProfileViewState(
            userFull = userFull
        )
    }

    fun getData() {
        viewModelScope.launch {
            val userFull = MessagesController.getInstance(currentAccount)
                .getUserFull(UserConfig.getInstance(currentAccount).getClientUserId())

            val userPhotos = MessagesController
                .getInstance(currentAccount)
                .getDialogPhotos(userFull.id)

            userPhotos.loadCache()

            if (userPhotos.loaded) {
                setState {
                    copy(
                        userFull = userFull,
                        userPhotos = ArrayList(userPhotos.photos)
                    )
                }
            } else {
                delay(3000)
                setState {
                    copy(
                        userFull = userFull,
                        userPhotos = ArrayList(userPhotos.photos)
                    )
                }
            }

            loadPortfolio()
        }
    }

    private var currentAccount = UserConfig.selectedAccount
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun handleIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.OnLoad -> {
                getData()
            }
            ProfileIntent.LoadPortfolio -> {
                loadPortfolio()
            }
            is ProfileIntent.OnPortfolioPhotoSelected -> {
                uploadPortfolioItem(intent.photo, intent.localPath)
            }
            ProfileIntent.OnClearPortfolioUpload -> {
                setState { copy(portfolioUploadLocalPath = null, portfolioUploading = false) }
            }
        }
    }

    private fun loadPortfolio() {
        setState { copy(portfolioLoading = true) }

        val request = TLRPC.TL_profile_getPortfolio().apply {
            user_id = MessagesController.getInstance(currentAccount).getInputUser(
                UserConfig.getInstance(currentAccount).getClientUserId()
            )
            tab ="photo"
            offset = 0
            limit = 100
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(request) { response, error ->
            mainHandler.post {
                if (error == null && response is TLRPC.TL_profile_portfolio) {
                    setState {
                        copy(
                            portfolioItems = response.items,
                            portfolioLoading = false
                        )
                    }
                } else {
                    setState { copy(portfolioLoading = false) }
                }
            }
        }
    }

    private fun uploadPortfolioItem(inputFile: TLRPC.InputFile, localPath: String?) {
        setState {
            copy(
                portfolioUploading = true,
                portfolioUploadLocalPath = localPath
            )
        }

        val request = TLRPC.TL_profile_uploadPortfolioItem().apply {
            file = inputFile
            type = "photo"
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(request) { response, error ->
            mainHandler.post {
                if (error == null && response is TLRPC.TL_profile_portfolioItem) {
                    setState {
                        copy(
                            portfolioItems = listOf(response) + portfolioItems,
                            portfolioUploading = false,
                            portfolioUploadLocalPath = null
                        )
                    }
                } else {
                    setState {
                        copy(
                            portfolioUploading = false,
                            portfolioUploadLocalPath = null
                        )
                    }
                }
            }
        }
    }
}
