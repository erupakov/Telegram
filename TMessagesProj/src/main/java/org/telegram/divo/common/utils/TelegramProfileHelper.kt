package org.telegram.divo.common.utils

import kotlinx.coroutines.suspendCancellableCoroutine
import org.telegram.messenger.FileLoader
import org.telegram.messenger.MessagesController
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLRPC
import java.io.File
import kotlin.coroutines.resume

object TelegramProfileHelper {

    suspend fun updateTelegramAvatar(currentAccount: Int, photoFile: File): Boolean {
        return try {
            val inputFile = kotlinx.coroutines.withTimeoutOrNull(15000) {
                suspendCancellableCoroutine<TLRPC.InputFile?> { continuation ->
                    val path = photoFile.absolutePath
                    FileLoader.getInstance(currentAccount).uploadFile(path) { result ->
                        if (continuation.isActive) continuation.resume(result)
                    }
                    continuation.invokeOnCancellation {
                        FileLoader.getInstance(currentAccount).cancelFileUpload(path, false)
                    }
                }
            }

            if (inputFile != null) {
                val photoReq = TLRPC.TL_photos_uploadProfilePhoto().apply {
                    file = inputFile
                    flags = flags or 1
                }
                val photoResult = kotlinx.coroutines.withTimeoutOrNull(10000) {
                    suspendCancellableCoroutine<TLRPC.TL_photos_photo?> { continuation ->
                        val reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(photoReq) { response, error ->
                            if (error == null && response is TLRPC.TL_photos_photo) {
                                continuation.resume(response)
                            } else {
                                continuation.resume(null)
                            }
                        }
                        continuation.invokeOnCancellation {
                            ConnectionsManager.getInstance(currentAccount).cancelRequest(reqId, true)
                        }
                    }
                }
                
                if (photoResult != null) {
                    val uc = UserConfig.getInstance(currentAccount)
                    val currentUser = uc.currentUser
                    if (currentUser != null && photoResult.photo != null) {
                        val bigSize = FileLoader.getClosestPhotoSizeWithSize(photoResult.photo.sizes, 800)
                        val smallSize = FileLoader.getClosestPhotoSizeWithSize(photoResult.photo.sizes, 150)
                        if (smallSize != null && bigSize != null) {
                            if (currentUser.photo == null) {
                                currentUser.photo = TLRPC.TL_userProfilePhoto()
                            }
                            currentUser.photo.photo_id = photoResult.photo.id
                            currentUser.photo.photo_small = smallSize.location
                            currentUser.photo.photo_big = bigSize.location
                            currentUser.photo.dc_id = photoResult.photo.dc_id
                            uc.setCurrentUser(currentUser)
                            uc.saveConfig(true)
                        }
                    }
                    MessagesController.getInstance(currentAccount).putUsers(photoResult.users, false)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
