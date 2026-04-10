package org.telegram.divo.components.items

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DivoBottomSheet(
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    title: String = "",
    containerColor: Color =  AppTheme.colors.backgroundLight,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    isSaveMode: Boolean = true,
    @DrawableRes iconClose: Int = R.drawable.ic_divo_close,
    onDismiss: () -> Unit,
    onSave: () -> Unit = {},
    onReset: () -> Unit = {},
    bodyContent: @Composable ColumnScope.() -> Unit
) {
    val systemBars = WindowInsets.systemBars.asPaddingValues()
    val topPadding = systemBars.calculateTopPadding()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = containerColor,
        dragHandle = null,
        modifier = Modifier.padding(top = topPadding + 16.dp),
        contentWindowInsets = { WindowInsets(0.dp) }
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(containerColor)
                .navigationBarsPadding()
                .padding(contentPadding),
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = verticalArrangement
        ) {
            HeaderSection(
                title = title,
                isSaveMode = isSaveMode,
                iconClose = iconClose,
                onDismiss = onDismiss,
                onSave = onSave,
                onReset = onReset
            )
            bodyContent()
        }
    }
}

@Composable
private fun HeaderSection(
    title: String,
    isSaveMode: Boolean,
    @DrawableRes iconClose: Int,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(36.dp)
            .padding(top = 8.dp)
            .height(5.dp)
            .clip(CircleShape)
            .background(AppTheme.colors.backgroundDark.copy(0.3f))
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
    ) {
        RoundedButton(
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterStart),
            resId = iconClose,
            paddingEnd = 0.dp,
            onClick = { onDismiss() }
        )

        Text(
            modifier = Modifier.align(Alignment.Center),
            text = title,
            style = AppTheme.typography.bodyLarge
        )

        if (isSaveMode) {
            RoundedButton(
                modifier = Modifier
                    .size(40.dp)
                    .align(Alignment.CenterEnd),
                resId = R.drawable.ic_divo_apply,
                iconSize = 28.dp,
                background = AppTheme.colors.accentOrange,
                iconTint = AppTheme.colors.onBackground,
                onClick = onSave
            )
        } else {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickableWithoutRipple { onReset() },
                text = stringResource(R.string.ButtonReset),
                fontSize = 15.sp,
                color = AppTheme.colors.accentOrange,
                style = AppTheme.typography.helveticaNeueRegular
            )
        }
    }
}