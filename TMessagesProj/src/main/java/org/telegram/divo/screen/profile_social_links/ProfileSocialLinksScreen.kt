package org.telegram.divo.screen.profile_social_links

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
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

    LaunchedEffect(viewModel.effect) {
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
    var instagramUser by remember { mutableStateOf(uiState.instagramUser) }
    var tiktokUser by remember { mutableStateOf(uiState.tiktokUser) }
    var youtubePath by remember { mutableStateOf(uiState.youtubePath) }
    var website by remember { mutableStateOf(uiState.website) }

    Scaffold(
        modifier = Modifier.padding(top = 36.dp),
        containerColor = AppTheme.colors.backgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Social links", color = AppTheme.colors.textColor) },
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
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            SocialLinkField(
                label = "Instagram",
                prefix = "instagram.com/@",
                value = instagramUser,
                onValueChange = { instagramUser = it }
            )

            SocialLinkField(
                label = "TikTok",
                prefix = "tiktok.com/@",
                value = tiktokUser,
                onValueChange = { tiktokUser = it }
            )

            SocialLinkField(
                label = "YouTube",
                prefix = "youtube.com/",
                value = youtubePath,
                onValueChange = { youtubePath = it }
            )

            SocialLinkField(
                label = "Website",
                prefix = null,
                placeholder = "Enter your website",
                value = website,
                onValueChange = { website = it }
            )

            Spacer(Modifier.height(16.dp))

            UIButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Save",
                onClick = {
                    // Build full URLs here (prefix is NOT editable).
                    val instagramUrl = buildUrl("https://instagram.com/@", instagramUser)
                    val tiktokUrl = buildUrl("https://tiktok.com/@", tiktokUser)
                    val youtubeUrl = buildUrl("https://youtube.com/", youtubePath)
                    val websiteUrl = normalizeWebsite(website)

//                    onIntent(
////                        ProfileSocialLinksViewModel.Intent.OnSaveClicked(
////                            instagramUrl = instagramUrl,
////                            tiktokUrl = tiktokUrl,
////                            youtubeUrl = youtubeUrl,
////                            website = websiteUrl
////                        )
//                    )
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SocialLinkField(
    label: String,
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
        label = { Text(label) },
        placeholder = if (placeholder != null) ({ Text(placeholder) }) else null,
        prefix = if (prefix != null) ({ Text(prefix) }) else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrect = false
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


//
//@OptIn(ExperimentalMaterial3Api::class)
//@Preview
//@Composable
//fun ProfileSocialLinksScreenView(
//    uiState: ProfileSocialLinksViewModel.UiViewState = ProfileSocialLinksViewModel.UiViewState(),
//    onIntent: (ProfileSocialLinksViewModel.Intent) -> Unit = {},
//    onCloseScreen: ()-> Unit = {}
//
//) {
//    var instagram by remember { mutableStateOf(uiState.fName) }
//    var lName by remember { mutableStateOf(uiState.lName) }
//    var bio by remember { mutableStateOf(uiState.bio) }
//
//    Scaffold(
//        modifier = Modifier.padding(top = 36.dp),
//        containerColor = AppTheme.colors.backgroundDark,
//        topBar = {
//            TopAppBar(
//                title = {
//                    Text("Profile", color = AppTheme.colors.textColor)
//                },
//                navigationIcon = {
//                    IconButton(onClick = {
//                        onCloseScreen()
//                    }) {
//                        Icon(
//                            painter = painterResource(R.drawable.ic_divo_back),
//                            contentDescription = null,
//                            modifier = Modifier.size(24.dp),
//                            tint = AppTheme.colors.textColor
//                        )
//                    }
//                },
//                actions = {
//                },
//                colors = TopAppBarDefaults.topAppBarColors(
//                    containerColor = AppTheme.colors.backgroundDark
//                )
//
//            )
//        }
//    ) { paddingValues ->
//
//        Column(
//            modifier = Modifier
//                .padding(paddingValues)
//                .padding(horizontal = 16.dp)
//        ) {
//
//            UiDarkTextField(
//                value = fName,
//                onValueChange = {
//                    fName = it
//                },
//                label = "First Name",
//                modifier = Modifier.padding(top = 8.dp)
//            )
//            UiDarkTextField(
//                label = "Last Name",
//                value = lName,
//                onValueChange = {
//                    lName = it
//                },
//                modifier = Modifier.padding(top = 8.dp)
//
//            )
//
//
//            UIButton(
//                modifier = Modifier.fillMaxWidth(),
//                text = "Save",
//                onClick = {
//                    onIntent(
//                        ProfileSocialLinksViewModel.Intent.OnSaveClicked(
//                            fName = fName,
//                            lName = lName,
//                            bio = bio
//                        )
//                    )
//                })
//        }
//    }
//}

