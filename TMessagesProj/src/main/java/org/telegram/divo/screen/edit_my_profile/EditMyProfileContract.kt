package org.telegram.divo.screen.edit_my_profile

import org.telegram.divo.common.ViewEffect
import org.telegram.divo.common.ViewIntent
import org.telegram.divo.common.ViewState
import org.telegram.divo.entity.UserInfo
import org.telegram.tgnet.TLRPC
import java.io.File

data class EventListViewState(
    val fName: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val userFull: UserInfo? = null,
    val isModel: Boolean = false,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val errorMessage: String? = null,
) : ViewState

sealed class EditMyProfileIntent : ViewIntent {
    data class OnSaveClicked(val fName: String, val bio: String, val file: Result<File>?) :
        EditMyProfileIntent()

    data object OnLoad : EditMyProfileIntent()
    data class OnAvatarUploaded(
        val photo: TLRPC.InputFile?,
        val video: TLRPC.InputFile?,
        val videoStartTimestamp: Double,
        val videoPath: String?,
        val bigSize: TLRPC.PhotoSize?,
        val smallSize: TLRPC.PhotoSize?,
        val isVideo: Boolean,
        val emojiMarkup: TLRPC.VideoSize?
    ) : EditMyProfileIntent()
}

sealed class Effect : ViewEffect {
    data object NavigateBack : Effect()
    data object SaveSuccess : Effect()
}