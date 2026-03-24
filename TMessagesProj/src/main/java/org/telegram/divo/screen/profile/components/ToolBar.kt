package org.telegram.divo.screen.profile.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.flow.distinctUntilChanged
import org.telegram.divo.common.formattedAge
import org.telegram.divo.components.BackButton
import org.telegram.divo.components.DivoPopupMenu
import org.telegram.divo.components.PopupMenuItem
import org.telegram.divo.screen.profile.ProfileViewState
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun ToolBar(
    modifier: Modifier = Modifier,
    uiState: ProfileViewState,
    isOwnProfile: Boolean,
    lazyListState: LazyListState,
    onNavigateBack: () -> Unit,
    onEditProfileClicked: () -> Unit,
    onEditBackgroundClicked: () -> Unit,
    onEditSocialLinksClicked: () -> Unit,
    onManageWorkExperienceClicked: () -> Unit,
) {
    var showDropdownMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        BackButton(
            modifier = Modifier
                .padding(start = 16.dp)
                .align(Alignment.CenterStart),
            onBackClicked = onNavigateBack
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 56.dp),
            contentAlignment = Alignment.Center
        ) {
            TitleContent(
                lazyListState = lazyListState,
                uiState = uiState
            )
        }

        if (isOwnProfile) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 50.dp),
                onClick = { showDropdownMenu = true }
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White)
            }
        }

        DivoPopupMenu(
            visible = showDropdownMenu,
            onDismiss = { showDropdownMenu = false },
            items = listOf(
                PopupMenuItem(R.string.EditProfile, onEditProfileClicked),
                PopupMenuItem(R.string.ChangeProfileBackground, onEditBackgroundClicked),
                PopupMenuItem(R.string.EditSocialLinks, onEditSocialLinksClicked),
                PopupMenuItem(R.string.ManageWorkExperience, onManageWorkExperienceClicked),
            )
        )

        Box(
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            //TODO пока не понятно назначение кнопки
            IconButton(onClick = {  }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
            }
//            DropdownMenu(
//                expanded = showDropdownMenu,
//                onDismissRequest = { showDropdownMenu = false }
//            ) {
//                DropdownMenuItem(
//                    text = { Text("Edit Background Image") },
//                    onClick = {
//                        showDropdownMenu = false
//                        onEditBackgroundClicked()
//                    }
//                )
//            }
        }
    }
}

@Composable
private fun TitleContent(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    uiState: ProfileViewState
) {
    val animatable = remember { Animatable(0f) }
    val previousVisible = remember { mutableStateOf(false) }

    LaunchedEffect(lazyListState) {
        snapshotFlow {
            lazyListState.firstVisibleItemIndex > 0 ||
                    (lazyListState.firstVisibleItemIndex == 0 &&
                            lazyListState.firstVisibleItemScrollOffset > 400)
        }.distinctUntilChanged()
            .collect { visible ->
                if (visible != previousVisible.value) {
                    previousVisible.value = visible
                    animatable.animateTo(
                        targetValue = if (visible) 1f else 0f,
                        animationSpec = tween(300)
                    )
                }
            }
    }

    Column(
        modifier = modifier
            .graphicsLayer {
                val progress = animatable.value
                alpha = progress
                translationY = (1f - progress) * 30f
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = uiState.userInfo.fullName,
            color = Color.White,
            style = AppTheme.typography.helveticaNeueLtCom,
            fontSize = 14.sp,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row {
            uiState.userInfo.let { userInfo ->
                Text(
                    text = userInfo.roleLabel.lowercase(),
                    color = Color.White,
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 10.sp,
                )
                if (userInfo.birthday.isNotEmpty()) {
                    Text(
                        text = " · ${userInfo.birthday.formattedAge()} · ",
                        style = AppTheme.typography.helveticaNeueRegular,
                        color = AppTheme.colors.textColor,
                        fontSize = 10.sp,
                    )
                } else {
                    Spacer(modifier = Modifier.width(2.dp))
                }

                if (userInfo.city != null) {
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