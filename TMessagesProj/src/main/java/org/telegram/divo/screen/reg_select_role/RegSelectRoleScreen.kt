package org.telegram.divo.screen.reg_select_role

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.screen.reg_select_role.components.RoleCard
import org.telegram.messenger.R


data class RoleOption(
    val role: UserIntent,
    val title: String,
    val description: String,
)

@Composable
fun RoleSelectionScreen(
    viewModel: RoleSelectionViewModel = viewModel(),
    onContinue: (UserIntent) -> Unit,
    onBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    val options = listOf(
        RoleOption(
            role = UserIntent.GET_HIRED,
            title = stringResource(R.string.OptionGetHiredTitle),
            description = stringResource(R.string.OptionGetHiredSubtitle),
        ),
        RoleOption(
            role = UserIntent.LOOKING_FOR_TALENT,
            title = stringResource(R.string.OptionLookingForTalentTitle),
            description = stringResource(R.string.OptionLookingForTalentSubtitle),
        ),
        RoleOption(
            role = UserIntent.FAN,
            title = stringResource(R.string.OptionHereToFollowTitle),
            description = stringResource(R.string.OptionHereToFollowSubtitle),
        ),
    )

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.divo_select_role_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Column {
            RoundedButton(
                modifier = Modifier.padding(top = 40.dp, start = 16.dp),
                onClick = onBack
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(options) { item ->
                            RoleCard(
                                title = item.title,
                                description = item.description,
                                selected = state.intent == item.role,
                                onClick = {
                                    viewModel.setIntent(RoleSelectionIntent.OnUserIntentSelected(item.role))
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            UIButtonNew(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp),
                enabled = state.intent != null,
                text = stringResource(R.string.ButtonContinue)
            ) {
                state.intent?.let {
                    onContinue(it)
                }
            }

        }
    }
}
