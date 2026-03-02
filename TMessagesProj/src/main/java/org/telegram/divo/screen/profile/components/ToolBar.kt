package org.telegram.divo.screen.profile.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import org.telegram.messenger.R
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.common.formattedAge
import org.telegram.divo.screen.profile.ProfileViewState
import org.telegram.divo.style.AppTheme

@Composable
fun ToolBar(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState,
    isOwnProfile: Boolean,
    titleVisible: Boolean = false,
    onEditClicked: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onEditBackgroundClicked: () -> Unit = {}
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterStart)
                .clickableWithoutRipple { onNavigateBack() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(painter = painterResource(R.drawable.ic_arrow_back_21), contentDescription = "Back", tint = Color.White)
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                modifier = Modifier.padding(top = 2.dp),
                text = "Back",
                style = AppTheme.typography.helveticaNeueRegular,
                fontSize = 17.sp,
                color = Color.White,
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 56.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = titleVisible,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = uiState.userInfo?.fullName.orEmpty(),
                        color = Color.White,
                        style = AppTheme.typography.helveticaNeueLtCom,
                        fontSize = 14.sp,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        uiState.userInfo?.let { userInfo ->
                            Text(
                                text = userInfo.roleLabel.lowercase(),
                                color = Color.White,
                                style = AppTheme.typography.helveticaNeueRegular,
                                fontSize = 10.sp,
                            )
                            Text(
                                text = " · ${userInfo.birthday.formattedAge()} · ",
                                style = AppTheme.typography.helveticaNeueRegular,
                                color = AppTheme.colors.textColor,
                                fontSize = 10.sp,
                            )
                            Text(
                                text = userInfo.city.name,
                                style = AppTheme.typography.helveticaNeueRegular,
                                color = AppTheme.colors.textColor,
                                fontSize = 10.sp,
                            )
                        }
                    }
                }
            }
        }
        if (isOwnProfile) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 54.dp),
                onClick = { onEditClicked() }
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
            }
        }
        Box(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            IconButton(onClick = { showDropdownMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
            }
            DropdownMenu(
                expanded = showDropdownMenu,
                onDismissRequest = { showDropdownMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Edit Background Image") },
                    onClick = {
                        showDropdownMenu = false
                        onEditBackgroundClicked()
                    }
                )
            }
        }
    }
}