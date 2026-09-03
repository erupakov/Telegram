package org.telegram.divo.dal.utils

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.telegram.divo.analytics.AnalyticsEvent
import org.telegram.divo.analytics.DivoAnalytics
import org.telegram.divo.dal.dto.auth.SocialLoginRequest
import org.telegram.divo.dal.network.DivoApi
import org.telegram.divo.dal.network.DivoResult
import org.telegram.divo.dal.network.getErrorMessage
import org.telegram.messenger.BuildVars
import org.telegram.messenger.MessagesController
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig
import org.telegram.tgnet.ConnectionsManager
import org.telegram.tgnet.TLRPC
import kotlin.coroutines.resume

object GoogleSignInHelper {

    private const val TAG = "GoogleSignInHelper"

    interface GoogleSignInCallback {
        /** User exists on backend — fully authenticated, accessToken saved. */
        fun onSuccess(authResponse: TLRPC.TL_auth_authorization)
        /** User NOT found on backend — need to go through registration form.
         *  [firebaseUid], [email], [dummyPhone], and [authResponse] should be passed to the registration flow. */
        fun onUserNotFound(firebaseUid: String, email: String, dummyPhone: String, authResponse: TLRPC.TL_auth_authorization, firstName: String?, lastName: String?, photoUrl: String?)
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
                val googleIdTokenCredential = getGoogleIdToken(context)
                if (googleIdTokenCredential == null) {
                    callback.onError(context.getString(R.string.ErrorGoogleSignInFailed))
                    return@launch
                }
                val googleIdToken = googleIdTokenCredential.idToken

                // Step 2: Sign in to Firebase with Google credential
                val firebaseUser = signInToFirebase(googleIdToken)
                if (firebaseUser == null) {
                    callback.onError(context.getString(R.string.ErrorFirebaseAuthFailed))
                    return@launch
                }

                val uid = firebaseUser.uid
                val email = firebaseUser.email ?: ""
                Log.d(TAG, "Firebase UID: $uid, email: $email")

                // Step 3: Try login-social on Divo backend
                DivoAnalytics.logEvent(AnalyticsEvent.SignInStart("google"))
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

                val currentAccount = UserConfig.selectedAccount

                if (loginResult is DivoResult.Success) {
                    val profileResult = withContext(Dispatchers.IO) {
                        DivoApi.userRepository.getCurrentUserInfo(forceRefresh = true)
                    }
                    val dummyPhone = if (profileResult is DivoResult.Success) {
                        profileResult.value.phone.takeIf { it.isNotBlank() }
                    } else {
                        Log.d("RegForm", profileResult.getErrorMessage())
                        null
                    }

                    if (dummyPhone == null) {
                        withContext(Dispatchers.Main) {
                            callback.onError(context.getString(R.string.ErrorProfileFetchFailed))
                        }
                        return@launch
                    }

                    val authResponse = doTelegramAuth(dummyPhone, currentAccount)
                    
                    withContext(Dispatchers.Main) {
                        if (authResponse != null) {
                            DivoApi.accessTokenProvider.setGoogleLogin(true)
                            DivoAnalytics.logEvent(AnalyticsEvent.SignInComplete("google"))
                            callback.onSuccess(authResponse)
                        } else {
                            callback.onError(context.getString(R.string.ErrorTelegramAuthFailed))
                        }
                    }
                } else {
                    val dummyPhoneResult = withContext(Dispatchers.IO) {
                        DivoApi.authRepository.getDummyPhone()
                    }
                    val newDummyPhone = if (dummyPhoneResult is DivoResult.Success) {
                        dummyPhoneResult.value.data?.phone?.takeIf { it.isNotBlank() }
                    } else null

                    if (newDummyPhone == null) {
                        withContext(Dispatchers.Main) {
                            callback.onError(context.getString(R.string.ErrorDummyPhoneFailed))
                        }
                        return@launch
                    }

                    val authResponse = doTelegramAuth(newDummyPhone, currentAccount)
                    
                    withContext(Dispatchers.Main) {
                        if (authResponse != null) {
                            callback.onUserNotFound(uid, email, newDummyPhone, authResponse, googleIdTokenCredential.givenName, googleIdTokenCredential.familyName, googleIdTokenCredential.profilePictureUri?.toString())
                        } else {
                            callback.onError(context.getString(R.string.ErrorTelegramAuthFailed))
                        }
                    }
                }
            } catch (_: GetCredentialCancellationException) {
                callback.onCancelled()
            } catch (_: NoCredentialException) {
                callback.onError(context.getString(R.string.ErrorGoogleNoAccount))
            } catch (_: GetCredentialException) {
                callback.onError(context.getString(R.string.ErrorGoogleSignInFailed))
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onError(context.getString(R.string.ErrorUnexpected) + ": " + e.message)
                }
            }
        }
        return Runnable { job.cancel() }
    }

    private suspend fun doTelegramAuth(phone: String, currentAccount: Int): TLRPC.TL_auth_authorization? {
        return suspendCancellableCoroutine { continuation ->
            val sendCode = TLRPC.TL_auth_sendCode().apply {
                api_hash = BuildVars.APP_HASH
                api_id = BuildVars.APP_ID
                phone_number = phone
                settings = TLRPC.TL_codeSettings().apply {
                    allow_flashcall = false
                    current_number = false
                    allow_app_hash = false
                    token = "test"
                }
            }
            
            ConnectionsManager.getInstance(currentAccount).sendRequest(
                sendCode,
                { response, error ->
                    if (error != null) {
                        continuation.resume(null as TLRPC.TL_auth_authorization?)
                        return@sendRequest
                    }
                    if (response is TLRPC.TL_auth_sentCode) {
                        val signIn = TLRPC.TL_auth_signIn().apply {
                            flags = 1
                            phone_number = phone
                            phone_code_hash = response.phone_code_hash
                            phone_code = "12345"
                        }
                        ConnectionsManager.getInstance(currentAccount).sendRequest(
                            signIn,
                            { signInResponse, signInError ->
                                if (signInError != null) {
                                    continuation.resume(null as TLRPC.TL_auth_authorization?)
                                    return@sendRequest
                                }
                                when (signInResponse) {
                                    is TLRPC.TL_auth_authorization -> {
                                        UserConfig.getInstance(currentAccount).clientUserId = signInResponse.user.id
                                        UserConfig.getInstance(currentAccount).currentUser = signInResponse.user
                                        UserConfig.getInstance(currentAccount).saveConfig(true)
                                        MessagesController.getInstance(currentAccount).putUser(signInResponse.user, false)
                                        continuation.resume(signInResponse)
                                    }

                                    is TLRPC.TL_auth_authorizationSignUpRequired -> {
                                        // Номер не зарегистрирован — делаем signup с dummy-данными
                                        val signUp = TLRPC.TL_auth_signUp().apply {
                                            phone_number = phone
                                            phone_code_hash = response.phone_code_hash
                                            first_name = "User"
                                            last_name = ""
                                        }
                                        ConnectionsManager.getInstance(currentAccount).sendRequest(
                                            signUp,
                                            { signUpResponse, signUpError ->
                                                if (signUpError != null || signUpResponse !is TLRPC.TL_auth_authorization) {
                                                    continuation.resume(null)
                                                    return@sendRequest
                                                }
                                                UserConfig.getInstance(currentAccount).apply {
                                                    clientUserId = signUpResponse.user.id
                                                    currentUser = signUpResponse.user
                                                    saveConfig(true)
                                                }
                                                MessagesController.getInstance(currentAccount)
                                                    .putUser(signUpResponse.user, false)
                                                continuation.resume(signUpResponse)
                                            }, ConnectionsManager.RequestFlagWithoutLogin
                                        )
                                    }

                                    else -> {
                                        continuation.resume(null as TLRPC.TL_auth_authorization?)
                                    }
                                }
                            }, ConnectionsManager.RequestFlagWithoutLogin
                        )
                    } else {
                        continuation.resume(null as TLRPC.TL_auth_authorization?)
                    }
                }, ConnectionsManager.RequestFlagWithoutLogin
            )
        }
    }

    /**
     * Step 1: Present Google account picker and get the ID token.
     */
    private suspend fun getGoogleIdToken(context: Context): GoogleIdTokenCredential? {
        val webClientId = context.getString(R.string.default_web_client_id)
        if (webClientId.isBlank() || webClientId == "default_web_client_id") {
            Log.e(TAG, "Invalid default_web_client_id resource. Make sure google-services.json is configured properly.")
            return null
        }

        val credentialManager = CredentialManager.create(context)
        val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result: GetCredentialResponse = credentialManager.getCredential(
            context = context,
            request = request,
        )

        return GoogleIdTokenCredential.createFrom(result.credential.data)
    }

    /**
     * Step 2: Sign in to Firebase Auth with the Google ID token
     * to obtain the Firebase UID that the Divo backend expects.
     */
    private suspend fun signInToFirebase(idToken: String): FirebaseUser? {
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
