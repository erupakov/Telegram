package org.telegram.divo.screen.edit_my_profile

import org.telegram.divo.base.BaseViewModel
import org.telegram.divo.base.ViewEffect
import org.telegram.divo.base.ViewIntent
import org.telegram.divo.base.ViewState
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.FileLog
import org.telegram.messenger.MessagesController
import org.telegram.messenger.MessagesStorage
import org.telegram.messenger.NotificationCenter
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.RequestDelegate
import org.telegram.tgnet.TLObject
import org.telegram.tgnet.TLRPC

class EditMyProfileViewModel :
    BaseViewModel<
            EditMyProfileViewModel.EventListViewState,
            EditMyProfileViewModel.EditMyProfileIntent,
            EditMyProfileViewModel.Effect
            >() {

    data class EventListViewState(
        val fName: String = "",
        val lName: String = "",
        val bio: String = "",
        val userFull: TLRPC.UserFull,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
    ) : ViewState

    sealed class EditMyProfileIntent : ViewIntent {
        data class OnSaveClicked(val fName: String, val lName: String, val bio: String) :
            EditMyProfileIntent()

        data object OnLoad : EditMyProfileIntent()
    }

    sealed class Effect : ViewEffect {
        data object NavigateBack : Effect()
    }

    override fun createInitialState(): EventListViewState {
        val userFull = MessagesController.getInstance(currentAccount)
            .getUserFull(UserConfig.getInstance(currentAccount).getClientUserId())

        return EventListViewState(
            userFull = userFull
        )
    }

    private val currentAccount: Int = UserConfig.selectedAccount

    init {
        val uc = UserConfig.getInstance(currentAccount)
        val mc = MessagesController.getInstance(currentAccount)

        val me = uc.currentUser
        val userFull = mc.getUserFull(uc.clientUserId)

        setState {
            copy(
                fName = me?.first_name ?: userFull?.user?.first_name ?: "",
                lName = me?.last_name ?: userFull?.user?.last_name ?: "",
                bio = userFull?.about ?: "",
                errorMessage = null
            )
        }
    }

    override fun handleIntent(intent: EditMyProfileIntent) {
        when (intent) {
            EditMyProfileIntent.OnLoad -> Unit
            is EditMyProfileIntent.OnSaveClicked -> {
                updateProfile(intent.fName, intent.lName, intent.bio)
            }
        }
    }


    private var messageStorage: MessagesStorage? = null

    fun setMessageStorage(_messageStorage: MessagesStorage) {
        messageStorage = _messageStorage
    }


    private fun applyProfileLocally(fName: String, lName: String, about: String) {
        val uc = UserConfig.getInstance(currentAccount)
        val mc = MessagesController.getInstance(currentAccount)

        // 1) Update current user (имя/фамилия) + save config
        uc.currentUser?.let { me ->
            me.first_name = fName
            me.last_name = lName
            uc.setCurrentUser(me)
            uc.saveConfig(true)
        }

        // 2) Update UserFull.about + persist to storage (если UserFull уже загружен)
        val userFull = mc.getUserFull(uc.clientUserId)
        if (userFull != null) {
            userFull.about = about
            messageStorage?.updateUserInfo(userFull, false)
        }

        // 3) Notify UI (как делает UserInfoActivity)
        val nc = NotificationCenter.getInstance(currentAccount)
        nc.postNotificationName(NotificationCenter.mainUserInfoChanged)
        nc.postNotificationName(
            NotificationCenter.updateInterfaces,
            MessagesController.UPDATE_MASK_NAME
        )

        sendEffect(Effect.NavigateBack)

    }

    fun updateProfile(fNameRaw: String, lNameRaw: String, aboutRaw: String) {
        // first name в Telegram лучше не отправлять пустым
        val fName = fNameRaw.trim().ifEmpty { " " }
        val lName = lNameRaw.trim()          // можно пустую строку
        val about = aboutRaw.trim()          // можно пустую строку

        setState { copy(isLoading = true, errorMessage = null) }

        val req = TLRPC.TL_account_updateProfile().apply {
            var flags = 0

            // Telegram обычно шлёт first_name всегда (flag 1)
            flags = flags or 1
            first_name = fName

            // last_name (flag 2)
            flags = flags or 2
            last_name = lName

            // about (flag 4)
            flags = flags or 4
            this.about = about

            this.flags = flags
        }

        ConnectionsManager.getInstance(currentAccount).sendRequest(
            req,
            RequestDelegate { response: TLObject?, error: TLRPC.TL_error? ->
                AndroidUtilities.runOnUIThread {
                    if (error != null) {
                        // Покажи причину в UI
                        setState {
                            copy(
                                isLoading = false,
                                errorMessage = "${error.text ?: "Unknown error"} (code=${error.code})"
                            )
                        }
                        FileLog.e("updateProfile error: ${error.text} code=${error.code}")
                        return@runOnUIThread
                    }

                    // Сервер может вернуть User, но даже если нет — нам важнее локально синхронизировать модели
                    applyProfileLocally(fName = fName, lName = lName, about = about)

                    // Обновим экран сразу
                    setState {
                        copy(
                            fName = fNameRaw,
                            lName = lNameRaw,
                            bio = aboutRaw,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            },
        )
    }

}
