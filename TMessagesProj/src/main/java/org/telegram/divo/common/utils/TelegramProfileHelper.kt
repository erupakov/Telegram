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

    suspend fun updateTelegramName(currentAccount: Int, firstName: String, lastName: String): TLRPC.User? {
        return try {
            val req = org.telegram.tgnet.tl.TL_account.updateProfile().apply {
                flags = 1 or 2 // 1 = first_name, 2 = last_name
                first_name = firstName
                last_name = lastName
            }

            val profileUpdateResult = kotlinx.coroutines.withTimeoutOrNull(5000) {
                suspendCancellableCoroutine<TLRPC.User?> { continuation ->
                    val reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req) { response, error ->
                        if (error == null && response is TLRPC.User) {
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

            if (profileUpdateResult != null) {
                val uc = UserConfig.getInstance(currentAccount)
                uc.currentUser = profileUpdateResult
                uc.saveConfig(true)
                MessagesController.getInstance(currentAccount).putUser(profileUpdateResult, false)
            }
            profileUpdateResult
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateTelegramBirthday(currentAccount: Int, year: Int?, month: Int?, day: Int?): Boolean {
        return try {
            val req = org.telegram.tgnet.tl.TL_account.updateBirthday().apply {
                if (year != null && month != null && day != null) {
                    flags = 1
                    birthday = org.telegram.tgnet.tl.TL_account.TL_birthday().apply {
                        this.year = year
                        this.month = month
                        this.day = day
                        this.flags = 1
                    }
                } else {
                    flags = 0
                }
            }

            val result = kotlinx.coroutines.withTimeoutOrNull(5000) {
                suspendCancellableCoroutine<Boolean> { continuation ->
                    val reqId = ConnectionsManager.getInstance(currentAccount).sendRequest(req) { response, error ->
                        if (error == null && response is TLRPC.TL_boolTrue) {
                            continuation.resume(true)
                        } else {
                            continuation.resume(false)
                        }
                    }
                    continuation.invokeOnCancellation {
                        ConnectionsManager.getInstance(currentAccount).cancelRequest(reqId, true)
                    }
                }
            }

            if (result == true) {
                val uc = UserConfig.getInstance(currentAccount)
                val currentUserFull = MessagesController.getInstance(currentAccount).getUserFull(uc.clientUserId)
                if (currentUserFull != null) {
                    currentUserFull.flags2 = currentUserFull.flags2 or 32
                    currentUserFull.birthday = req.birthday
                    org.telegram.messenger.MessagesStorage.getInstance(currentAccount).updateUserInfo(currentUserFull, false)
                }
            }

            result ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
