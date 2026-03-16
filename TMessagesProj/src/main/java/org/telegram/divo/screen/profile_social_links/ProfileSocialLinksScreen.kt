package org.telegram.divo.screen.profile_social_links

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.components.shimmer
import org.telegram.divo.entity.UserSocialNetwork
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
    var facebookUser by remember(uiState.tiktokUser) {
        mutableStateOf(
            uiState.socialLinks
            .firstOrNull { it.id == 1 }
            ?.name
            .orEmpty()
        )
    }
    var instagramUser by remember(uiState.instagramUser) {
        mutableStateOf(
            uiState.socialLinks
                .firstOrNull { it.id == 2 }
                ?.name
                .orEmpty()
        )
    }
    var youtubePath by remember(uiState.youtubePath) { mutableStateOf(uiState.youtubePath) }
    var website by remember(uiState.website) { mutableStateOf(uiState.website) }

    Scaffold(
        containerColor = AppTheme.colors.backgroundNew,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.padding(top = 36.dp),
                windowInsets = WindowInsets(0, 0, 0, 0),
                title = {
                    Text(
                        text = stringResource(R.string.EditSocialLinks).uppercase(),
                        style = AppTheme.typography.helveticaNeueLtCom,
                        fontSize = 20.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp)
                            .size(40.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                ambientColor = Color.Black.copy(alpha = 0.1f),
                                spotColor = Color.Black.copy(alpha = 0.2f)
                            )
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickableWithoutRipple(onCloseScreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = 1.dp).size(16.dp),
                            painter = painterResource(R.drawable.ic_divo_back),
                            contentDescription = null,
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.backgroundNew
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
                    prefix = "instagram.com/@",
                    value = facebookUser,
                    isLoading = uiState.isLoading,
                    onValueChange = {
                        facebookUser = it
                    }
                )

                SocialLinkField(
                    prefix = "tiktok.com/@", //"tiktok.com/@",
                    value = instagramUser,
                    isLoading = uiState.isLoading,
                    onValueChange = {
                        instagramUser = it
                    }
                )

                SocialLinkField(
                    prefix = "youtube.com/",
                    value = youtubePath,
                    isLoading = uiState.isLoading,
                    onValueChange = { youtubePath = it }
                )

                SocialLinkField(
                    prefix = null,
                    placeholder = "Enter your website",
                    value = website,
                    isLoading = uiState.isLoading,
                    onValueChange = { website = it }
                )

                Spacer(Modifier.height(24.dp))

                UIButtonNew(
                    modifier = Modifier.fillMaxWidth(),
                    text = if (uiState.isUploading) "Saving..." else "Save",
                    enabled = !uiState.isUploading,
                    onClick = {
//                        val instagramUrl = buildUrl("https://instagram.com/", instagramUser)
//                        val tiktokUrl = buildUrl("https://tiktok.com/", tiktokUser)
//                        val youtubeUrl = buildUrl("https://youtube.com/", youtubePath)
//                        val websiteUrl = normalizeWebsite(website)

                        val socialLinks = buildList {
                            if (facebookUser.isNotBlank()) {
                                add(
                                    UserSocialNetwork(
                                        id = 1,
                                        name = facebookUser,
                                        link = "",
                                        provider = ""
                                    )
                                )
                            }

                            if (instagramUser.isNotBlank()) {
                                add(
                                    UserSocialNetwork(
                                        id = 2,
                                        name = instagramUser,
                                        link = "",
                                        provider = ""
                                    )
                                )
                            }
                        }

                        onIntent(
                            ProfileSocialLinksViewModel.Intent.OnSaveClicked(socialLinks)
                        )
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
            cursorColor = AppTheme.colors.buttonColor
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

