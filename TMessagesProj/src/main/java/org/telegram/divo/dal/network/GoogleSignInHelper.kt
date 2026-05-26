 package org.telegram.divo.dal.network

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.telegram.divo.dal.dto.auth.SocialLoginRequest
import kotlin.coroutines.resume

object GoogleSignInHelper {

    private const val TAG = "GoogleSignInHelper"

    /**
     * Web Client ID from Firebase Console (client_type: 3).
     * Project: divo-2-stage
     */
    private const val WEB_CLIENT_ID =
        "121062802243-9hajm212lfu3cpejssjgtkbpqb3j4jlp.apps.googleusercontent.com"

    private const val DEBUG_HARDCODED_PHONE = "79100083642"
    private const val DEBUG_HARDCODED_CODE = "12345"

    interface GoogleSignInCallback {
        /** User exists on backend — fully authenticated, accessToken saved. */
        fun onSuccess(authResponse: org.telegram.tgnet.TLRPC.TL_auth_authorization)
        /** User NOT found on backend — need to go through registration form.
         *  [firebaseUid], [email], [dummyPhone], and [authResponse] should be passed to the registration flow. */
        fun onUserNotFound(firebaseUid: String, email: String, dummyPhone: String, authResponse: org.telegram.tgnet.TLRPC.TL_auth_authorization)
        fun onError(error: String)
        fun onCancelled()
    }

    /**
     * Google Sign-In flow:
     * 1. Credential Manager → Google account picker → Google ID Token
     * 2. Firebase Auth → sign in with Google credential → Firebase UID
     * 3. POST /api/auth/login-social { uid, providerId } → Divo access token
     * 4. If user not found → callback.onUserNotFound() (caller navigates to RegForm)
     *
     * @param context Must be an Activity context.
     * @param callback Receives the result on the Main thread.
     * @return A [Runnable] that cancels the ongoing operation when invoked.
     */
    @JvmStatic
    fun signInWithGoogle(
        context: Context,
        callback: GoogleSignInCallback
    ): Runnable {
        val job = CoroutineScope(Dispatchers.Main).launch {
            try {
                // Step 1: Get Google ID Token via Credential Manager
                val googleIdToken = getGoogleIdToken(context)
                if (googleIdToken == null) {
                    callback.onError("Failed to get Google ID Token")
                    return@launch
                }

                // Step 2: Sign in to Firebase with Google credential
                val firebaseUser = signInToFirebase(googleIdToken)
                if (firebaseUser == null) {
                    callback.onError("Firebase authentication failed")
                    return@launch
                }

                val uid = firebaseUser.uid
                val email = firebaseUser.email ?: ""
                Log.d(TAG, "Firebase UID: $uid, email: $email")

                // Step 3: Try login-social on Divo backend
                val deviceId = getDeviceId()
                val loginResult = withContext(Dispatchers.IO) {
                    DivoApi.authRepository.loginSocial(
                        SocialLoginRequest(
                            uid = uid,
                            providerId = "google.com",
                            deviceId = deviceId,
                            deviceType = "android"
                        )
                    )
                }

                val currentAccount = org.telegram.messenger.UserConfig.selectedAccount

                if (loginResult is DivoResult.Success) {
                    val profileResult = withContext(Dispatchers.IO) {
                        DivoApi.userRepository.getCurrentUserInfo(forceRefresh = true)
                    }
                    var dummyPhone = if (profileResult is DivoResult.Success) {
                        profileResult.value.phone.takeIf { it.isNotBlank() }
                    } else null

                    if (DEBUG_HARDCODED_PHONE.isNotBlank()) {
                        dummyPhone = DEBUG_HARDCODED_PHONE
                    }

                    if (dummyPhone == null) {
                        withContext(Dispatchers.Main) {
                            callback.onError("Profile fetch failed or phone is missing")
                        }
                        return@launch
                    }
                    
                    Log.d(TAG, "Doing Telegram auth with existing dummy phone: $dummyPhone")
                    val authResponse = doTelegramAuth(dummyPhone, currentAccount)
                    
                    withContext(Dispatchers.Main) {
                        if (authResponse != null) {
                            callback.onSuccess(authResponse)
                        } else {
                            callback.onError("Telegram Auth Failed")
                        }
                    }
                } else {
                    Log.d(TAG, "User not found, fetching new dummy phone")
                    val dummyPhoneResult = withContext(Dispatchers.IO) {
                        DivoApi.authRepository.getDummyPhone()
                    }
                    var newDummyPhone = if (dummyPhoneResult is DivoResult.Success) {
                        dummyPhoneResult.value.data?.phone?.takeIf { it.isNotBlank() }
                    } else null

                    if (DEBUG_HARDCODED_PHONE.isNotBlank()) {
                        newDummyPhone = DEBUG_HARDCODED_PHONE
                    }

                    if (newDummyPhone == null) {
                        withContext(Dispatchers.Main) {
                            callback.onError("Failed to fetch new dummy phone for registration")
                        }
                        return@launch
                    }

                    val authResponse = doTelegramAuth(newDummyPhone, currentAccount)
                    
                    withContext(Dispatchers.Main) {
                        if (authResponse != null) {
                            callback.onUserNotFound(uid, email, newDummyPhone, authResponse)
                        } else {
                            callback.onError("Telegram Auth Failed")
                        }
                    }
                }

            } catch (e: GetCredentialCancellationException) {
                callback.onCancelled()
            } catch (e: GetCredentialException) {
                callback.onError(e.message ?: "Google Sign-In failed")
            } catch (e: Exception) {
                callback.onError(e.message ?: "Unexpected error")
            }
        }
        return Runnable { job.cancel() }
    }

    private suspend fun doTelegramAuth(phone: String, currentAccount: Int): org.telegram.tgnet.TLRPC.TL_auth_authorization? {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            val sendCode = org.telegram.tgnet.TLRPC.TL_auth_sendCode().apply {
                api_hash = org.telegram.messenger.BuildVars.APP_HASH
                api_id = org.telegram.messenger.BuildVars.APP_ID
                phone_number = phone
                settings = org.telegram.tgnet.TLRPC.TL_codeSettings().apply {
                    allow_flashcall = false
                    current_number = false
                    allow_app_hash = false
                    token = "test"
                }
            }
            
            org.telegram.tgnet.ConnectionsManager.getInstance(currentAccount).sendRequest(
                sendCode,
                { response, error ->
                    if (error != null) {
                        continuation.resume(null as org.telegram.tgnet.TLRPC.TL_auth_authorization?)
                        return@sendRequest
                    }
                    if (response is org.telegram.tgnet.TLRPC.TL_auth_sentCode) {
                        val signIn = org.telegram.tgnet.TLRPC.TL_auth_signIn().apply {
                            flags = 1 // Required to serialize phone_code
                            phone_number = phone
                            phone_code_hash = response.phone_code_hash
                            phone_code = DEBUG_HARDCODED_CODE
                        }
                        org.telegram.tgnet.ConnectionsManager.getInstance(currentAccount).sendRequest(
                            signIn,
                            { signInResponse, signInError ->
                                if (signInError != null) {
                                    continuation.resume(null as org.telegram.tgnet.TLRPC.TL_auth_authorization?)
                                    return@sendRequest
                                }
                                if (signInResponse is org.telegram.tgnet.TLRPC.TL_auth_authorization) {
                                    org.telegram.messenger.UserConfig.getInstance(currentAccount).clientUserId = signInResponse.user.id
                                    org.telegram.messenger.UserConfig.getInstance(currentAccount).currentUser = signInResponse.user
                                    org.telegram.messenger.UserConfig.getInstance(currentAccount).saveConfig(true)
                                    org.telegram.messenger.MessagesController.getInstance(currentAccount).putUser(signInResponse.user, false)
                                    continuation.resume(signInResponse)
                                } else {
                                    continuation.resume(null as org.telegram.tgnet.TLRPC.TL_auth_authorization?)
                                }
                            }, org.telegram.tgnet.ConnectionsManager.RequestFlagWithoutLogin
                        )
                    } else {
                        continuation.resume(null as org.telegram.tgnet.TLRPC.TL_auth_authorization?)
                    }
                }, org.telegram.tgnet.ConnectionsManager.RequestFlagWithoutLogin
            )
        }
    }

    /**
     * Step 1: Present Google account picker and get the ID token.
     */
    private suspend fun getGoogleIdToken(context: Context): String? {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(WEB_CLIENT_ID)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result: GetCredentialResponse = credentialManager.getCredential(
            context = context,
            request = request,
        )

        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
        return googleIdTokenCredential.idToken
    }

    /**
     * Step 2: Sign in to Firebase Auth with the Google ID token
     * to obtain the Firebase UID that the Divo backend expects.
     */
    private suspend fun signInToFirebase(idToken: String): com.google.firebase.auth.FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
        return authResult.user
    }

    fun getDeviceId(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL ?: "Android Device"
        return "$manufacturer $model"
    }
}
