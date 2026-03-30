package org.telegram.divo.screen.profile_social_links

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.components.shimmer
import org.telegram.divo.entity.SocialNetworkType
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R


@Composable
fun ProfileSocialLinksScreen(
    viewModel: ProfileSocialLinksViewModel = viewModel(),
    onCloseScreen: () -> Unit = {}
) {
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.setIntent(Intent.OnLoad)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { action ->
            when (action) {
                Effect.NavigateBack -> {
                    Toast.makeText(context, "Данные успешно обновлены", Toast.LENGTH_SHORT).show()
                    onCloseScreen()
                }
            }
        }
    }

    val uiState = viewModel.state.collectAsState().value

    ProfileSocialLinksScreenView(
        uiState = uiState,
        onIntent = { viewModel.setIntent(it) },
        onCloseScreen = onCloseScreen
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ProfileSocialLinksScreenView(
    uiState: UiViewState = UiViewState(),
    onIntent: (Intent) -> Unit = {},
    onCloseScreen: () -> Unit = {}
) {
    Scaffold(
        containerColor = AppTheme.colors.backgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.EditSocialLinks).uppercase(),
                        style = AppTheme.typography.appBar,
                    )
                },
                navigationIcon = {
                    RoundedButton(
                        modifier = Modifier.padding(start = 16.dp),
                        resId = R.drawable.ic_divo_back,
                        onClick = onCloseScreen
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.backgroundLight
                )
            )
        }
    ) { paddingValues ->
        val context = LocalContext.current

        // Show error toast
        LaunchedEffect(uiState.errorMessage) {
            uiState.errorMessage?.let { message ->
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(top = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                SocialLinkField(
                    prefix = SocialNetworkType.INSTAGRAM.value,
                    value = uiState.instagramUser.orEmpty(),
                    isLoading = uiState.isLoading,
                    onValueChange = {
                        onIntent(Intent.OnNicknameChanged(SocialNetworkType.INSTAGRAM, it))
                    }
                )

                SocialLinkField(
                    prefix = SocialNetworkType.TIKTOK.value,
                    value = uiState.tiktokUser.orEmpty(),
                    isLoading = uiState.isLoading,
                    onValueChange = {
                        onIntent(Intent.OnNicknameChanged(SocialNetworkType.TIKTOK, it))
                    }
                )

                SocialLinkField(
                    prefix = SocialNetworkType.YOUTUBE.value,
                    value = uiState.youtubeUser.orEmpty(),
                    isLoading = uiState.isLoading,
                    onValueChange = {
                        onIntent(Intent.OnNicknameChanged(SocialNetworkType.YOUTUBE, it))
                    }
                )

                SocialLinkField(
                    prefix = SocialNetworkType.WEBSITE.value,
                    placeholder = "Enter your website",
                    value = uiState.website,
                    isLoading = uiState.isLoading,
                    onValueChange = {
                        onIntent(Intent.OnNicknameChanged(SocialNetworkType.WEBSITE, it))
                    }
                )

                Spacer(Modifier.height(24.dp))

                UIButtonNew(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (uiState.isUploading) "Saving..." else "Save",
                    enabled = !uiState.isUploading,
                    onClick = {
                        onIntent(Intent.OnSaveClicked)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SocialLinkField(
    prefix: String?,
    value: String,
    isLoading: Boolean = false,
    onValueChange: (String) -> Unit,
    placeholder: String? = null
) {
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp)
            .shimmer(isLoading),
        value = value,
        onValueChange = { raw ->
            // Small cleanup: remove spaces and also remove leading @ if user types/pastes it
            val cleaned = raw.trim().replace(" ", "").removePrefix("@")
            onValueChange(cleaned)
        },
        shape = RoundedCornerShape(41.dp),
        placeholder = if (placeholder != null) ({
            Text(
                text = placeholder,
                style = AppTheme.typography.helveticaNeueRegular,
                fontSize = 16.sp,
                color = Color.Black.copy(alpha = 0.4f)
            )
        }) else null,
        prefix = if (prefix != null) ({
            Text(
                text = prefix,
                style = AppTheme.typography.helveticaNeueRegular,
                fontSize = 16.sp,
                color = Color.Black
            )
        }) else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = false
        ),
        textStyle = AppTheme.typography.helveticaNeueRegular.copy(
            fontSize = 16.sp
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = AppTheme.colors.accentOrange
        )
    )
}
