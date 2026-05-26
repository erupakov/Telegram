package org.telegram.divo.screen.auth

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.dal.network.GoogleSignInHelper
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R
import org.telegram.messenger.UserConfig

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(),
    onAuthClicked: () -> Unit = {},
    onGoogleUserNotFound: (firebaseUid: String, email: String, dummyPhone: String, authResponse: org.telegram.tgnet.TLRPC.TL_auth_authorization) -> Unit = { _, _, _, _ -> },
    onGoogleSuccess: (authResponse: org.telegram.tgnet.TLRPC.TL_auth_authorization) -> Unit = { _ -> }
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val isGoogleLoading = remember { mutableStateOf(false) }

    val currentAccount = UserConfig.selectedAccount

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            launch {
                when (effect) {
                    is AuthViewEffect.ShowError -> {
                        Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                    }
                    is AuthViewEffect.LoginSuccess -> {
                        onAuthClicked()
                    }
                    is AuthViewEffect.GoogleSignInRequested -> {
                        var currentContext = context
                        var activity: Activity? = null
                        while (currentContext is android.content.ContextWrapper) {
                            if (currentContext is Activity) {
                                activity = currentContext
                                break
                            }
                            currentContext = currentContext.baseContext
                        }
                        
                        if (activity == null) {
                            Toast.makeText(context, "Context is not an Activity", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        isGoogleLoading.value = true
                        GoogleSignInHelper.signInWithGoogle(
                            context = activity,
                            callback = object : GoogleSignInHelper.GoogleSignInCallback {
                                override fun onSuccess(authResponse: org.telegram.tgnet.TLRPC.TL_auth_authorization) {
                                    isGoogleLoading.value = false
                                    onGoogleSuccess(authResponse)
                                }
                                override fun onUserNotFound(firebaseUid: String, email: String, dummyPhone: String, authResponse: org.telegram.tgnet.TLRPC.TL_auth_authorization) {
                                    isGoogleLoading.value = false
                                    onGoogleUserNotFound(firebaseUid, email, dummyPhone, authResponse)
                                }
                                override fun onError(error: String) {
                                    isGoogleLoading.value = false
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                                override fun onCancelled() {
                                    isGoogleLoading.value = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            LottieProgressIndicator(modifier = Modifier.size(32.dp))
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize().background(AppTheme.colors.backgroundLight).padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(top = 120.dp),
                text = stringResource(R.string.WelcomeToDivo).uppercase(),
                fontSize = 32.sp,
                lineHeight = 36.sp,
                style = AppTheme.typography.helveticaNeueLtCom,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                modifier = Modifier,
                text = stringResource(R.string.SignOrCreateAccount),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary.copy(0.8f)
            )
            Spacer(Modifier.height(32.dp))
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                UIButtonNew(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.ContinueWithPhoneNumber),
                    enabled = !isGoogleLoading.value,
                    onClick = {
                        onAuthClicked()
                    }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f)
                )
                Text(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    text = stringResource(R.string.OnboardingOrLabel),
                    style = AppTheme.typography.bodyLarge,
                    fontSize = 12.sp,
                    color = AppTheme.colors.textPrimary.copy(0.8f)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (isGoogleLoading.value) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp).clip(RoundedCornerShape(99.dp)).background(AppTheme.colors.onBackground),
                    contentAlignment = Alignment.Center
                ) {
                    LottieProgressIndicator(color = AppTheme.colors.textPrimary)
                }
            } else {
                UIButtonNew(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.OnboardingSignInWithGoogle),
                    background = AppTheme.colors.onBackground,
                    leadingIcon = R.drawable.google,
                    leadingIconSize = 20,
                    isLoading = isGoogleLoading.value,
                    textStyle = AppTheme.typography.textButton.copy(
                        color = AppTheme.colors.textPrimary,
                        fontSize = 16.sp
                    ),
                    onClick = { viewModel.setIntent(AuthViewIntent.GoogleSignIn) }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            TermsText()
        }
    }
}

@Composable
private fun TermsText() {
    val prefix = stringResource(R.string.TermsPrefix)
    val terms = stringResource(R.string.OnboardingTermsOfService)
    val privacy = stringResource(R.string.OnboardingPrivacyPolicy)
    val andText = stringResource(R.string.OnboardingAnd)

    val termsUrl = "https://www.divo.global/legal-documents/mobile-app-eula"
    val privacyUrl = "https://www.divo.global/legal-documents/privacy-policy"

    val annotatedString = buildAnnotatedString {

        append(prefix)
        append(" ")

        withLink(
            LinkAnnotation.Url(
                url = termsUrl
            )
        ) {
            withStyle(
                SpanStyle(
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(terms)
            }
        }

        append(" ")
        append(andText)
        append(" ")

        withLink(
            LinkAnnotation.Url(
                url = privacyUrl
            )
        ) {
            withStyle(
                SpanStyle(
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(privacy)
            }
        }

        append(".")
    }

    Text(
        text = annotatedString,
        style = TextStyle(
            color = AppTheme.colors.textPrimary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier.fillMaxWidth()
    )
}