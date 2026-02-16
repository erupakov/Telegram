package org.telegram.divo.screen.profile_social_links

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.components.UIButton
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R


@Composable
fun ProfileSocialLinksScreen(
    viewModel: ProfileSocialLinksViewModel = viewModel(),
    onCloseScreen: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        viewModel.setIntent(ProfileSocialLinksViewModel.Intent.OnLoad)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { action ->
            when (action) {
                ProfileSocialLinksViewModel.Effect.NavigateBack -> onCloseScreen()
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
    uiState: ProfileSocialLinksViewModel.UiViewState = ProfileSocialLinksViewModel.UiViewState(),
    onIntent: (ProfileSocialLinksViewModel.Intent) -> Unit = {},
    onCloseScreen: () -> Unit = {}
) {
    // Store ONLY editable parts (username/path), not the prefix.
    // Use key to reset state when uiState values change (after loading)
    var instagramUser by remember(uiState.instagramUser) { mutableStateOf(uiState.instagramUser) }
    var tiktokUser by remember(uiState.tiktokUser) { mutableStateOf(uiState.tiktokUser) }
    var youtubePath by remember(uiState.youtubePath) { mutableStateOf(uiState.youtubePath) }
    var website by remember(uiState.website) { mutableStateOf(uiState.website) }

    Scaffold(
        modifier = Modifier.padding(top = 36.dp),
        containerColor = AppTheme.colors.backgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "EDIT SOCIAL LINKS",
                        color = AppTheme.colors.textColor,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCloseScreen) {
                        Icon(
                            painter = painterResource(R.drawable.ic_divo_back),
                            contentDescription = null,
                            tint = AppTheme.colors.textColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.backgroundDark
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                SocialLinkField(
                    prefix = "instagram.com/@",
                    value = instagramUser,
                    onValueChange = { instagramUser = it }
                )

                SocialLinkField(
                    prefix = "tiktok.com/@",
                    value = tiktokUser,
                    onValueChange = { tiktokUser = it }
                )

                SocialLinkField(
                    prefix = "youtube.com/",
                    value = youtubePath,
                    onValueChange = { youtubePath = it }
                )

                SocialLinkField(
                    prefix = null,
                    placeholder = "Enter your website",
                    value = website,
                    onValueChange = { website = it }
                )

                Spacer(Modifier.height(16.dp))

                UIButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (uiState.isLoading) "Saving..." else "Save",
                    enabled = !uiState.isLoading,
                    onClick = {
                        val instagramUrl = buildUrl("https://instagram.com/", instagramUser)
                        val tiktokUrl = buildUrl("https://tiktok.com/", tiktokUser)
                        val youtubeUrl = buildUrl("https://youtube.com/", youtubePath)
                        val websiteUrl = normalizeWebsite(website)

                        onIntent(
                            ProfileSocialLinksViewModel.Intent.OnSaveClicked(
                                instagramUrl = instagramUrl,
                                tiktokUrl = tiktokUrl,
                                youtubeUrl = youtubeUrl,
                                website = websiteUrl
                            )
                        )
                    }
                )
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp),
                    color = Color(0xFFBF7A54)
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
    onValueChange: (String) -> Unit,
    placeholder: String? = null
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        value = value,
        onValueChange = { raw ->
            // Small cleanup: remove spaces and also remove leading @ if user types/pastes it
            val cleaned = raw.trim().replace(" ", "").removePrefix("@")
            onValueChange(cleaned)
        },
        placeholder = if (placeholder != null) ({
            Text(
                text = placeholder,
                color = Color.White.copy(alpha = 0.5f)
            )
        }) else null,
        prefix = if (prefix != null) ({
            Text(
                text = prefix,
                color = Color.White
            )
        }) else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = false
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedIndicatorColor = Color.White.copy(alpha = 0.6f),
            unfocusedIndicatorColor = Color.White.copy(alpha = 0.4f),
            cursorColor = Color(0xFFBF7A54)
        )
    )
}

private fun buildUrl(prefix: String, editablePart: String): String {
    val v = editablePart.trim().removePrefix("@")
    return if (v.isBlank()) "" else prefix + v
}

private fun normalizeWebsite(input: String): String {
    val v = input.trim()
    if (v.isBlank()) return ""
    // If user typed domain without scheme, add https://
    return if (v.startsWith("http://") || v.startsWith("https://")) v else "https://$v"
}

