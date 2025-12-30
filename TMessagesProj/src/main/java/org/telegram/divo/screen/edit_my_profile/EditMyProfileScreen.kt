package org.telegram.divo.screen.edit_my_profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.components.TelegramUserAvatar
import org.telegram.divo.components.TelegramUserAvatarEditable
import org.telegram.divo.components.UIButton
import org.telegram.divo.components.UiDarkTextField
import org.telegram.divo.screen.event_list.EventListViewModel
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.MessagesStorage
import org.telegram.messenger.R


@Composable
fun EditMyProfileScreen(
    viewModel: EditMyProfileViewModel = viewModel(),
    messageStorage: MessagesStorage,
    onCloseScreen: ()-> Unit = {},
    onEditImageClicked:() -> Unit = {}
) {
    LaunchedEffect(true) {
        viewModel.setMessageStorage(messageStorage)
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { action ->
            when (action) {
                EditMyProfileViewModel.Effect.NavigateBack ->{
                    onCloseScreen()
                }
            }
        }
    }

    val uiState = viewModel.state.collectAsState().value

    EditMyProfileScreenView(
        uiState,
        onIntent = {
            viewModel.setIntent(it)
        },
        onCloseScreen = onCloseScreen,
        onEditImageClicked = onEditImageClicked
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMyProfileScreenView(
    uiState: EditMyProfileViewModel.EventListViewState,
    onIntent: (EditMyProfileViewModel.EditMyProfileIntent) -> Unit = {},
    onCloseScreen: ()-> Unit = {},
    onEditImageClicked:() -> Unit = {}
) {
    var fName by remember { mutableStateOf(uiState.fName) }
    var lName by remember { mutableStateOf(uiState.lName) }
    var bio by remember { mutableStateOf(uiState.bio) }

    Scaffold(
        modifier = Modifier.padding(top = 36.dp),
        containerColor = AppTheme.colors.backgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Text("Profile", color = AppTheme.colors.textColor)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onCloseScreen()
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.ic_divo_back),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = AppTheme.colors.textColor
                        )
                    }
                },
                actions = {
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            TelegramUserAvatarEditable(
                user = uiState.userFull.user,
                modifier = Modifier,
                onEditClick = {onEditImageClicked()}
            )

            UiDarkTextField(
                value = fName,
                onValueChange = {
                    fName = it
                },
                label = "First Name",
                modifier = Modifier.padding(top = 8.dp)
            )
            UiDarkTextField(
                label = "Last Name",
                value = lName,
                onValueChange = {
                    lName = it
                },
                modifier = Modifier.padding(top = 8.dp)

            )
            UiDarkTextField(
                label = "Biography",
                value = bio,
                onValueChange = {
                    bio = it
                },
                minLines = 5,
                singleLine = false,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
            )
            Spacer(modifier = Modifier.size(16.dp))

            UIButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onIntent(
                        EditMyProfileViewModel.EditMyProfileIntent.OnSaveClicked(
                            fName = fName,
                            lName = lName,
                            bio = bio
                        )
                    )
                })
        }
    }
}




