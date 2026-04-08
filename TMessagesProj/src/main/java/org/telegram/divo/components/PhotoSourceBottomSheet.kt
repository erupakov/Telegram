package org.telegram.divo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.screen.search.utils.SearchPreferences
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

enum class SearchImageAction { CAMERA, GALLERY, DIVO_PHOTO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoSourceBottomSheet(
    sheetState: SheetState,
    onActionSelected: (SearchImageAction) -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val prefs = remember { SearchPreferences(context) }

    var showDisclaimer by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<SearchImageAction?>(null) }

    fun handleAction(action: SearchImageAction) {
        if (prefs.isFaceSearchFirstUse) {
            pendingAction = action
            showDisclaimer = true
        } else {
            onActionSelected(action)
        }
    }

    if (showDisclaimer) {
        FaceSearchDisclaimerDialog(
            onConfirm = {
                prefs.isFaceSearchFirstUse = false
                showDisclaimer = false
                pendingAction?.let { onActionSelected(it) }
                pendingAction = null
            },
            onDismiss = {
                showDisclaimer = false
                pendingAction = null
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        Column {
            ChoosePhotoSection(
                onTakePhoto = { handleAction(SearchImageAction.CAMERA) },
                onChooseFromLibrary = { handleAction(SearchImageAction.GALLERY) },
                onUseDivoPhoto = { handleAction(SearchImageAction.DIVO_PHOTO) }
            )
            Spacer(Modifier.height(10.dp))
            UIButtonNew(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                text = stringResource(R.string.ButtonCancel),
                background = AppTheme.colors.backgroundLight,
                shape = RoundedCornerShape(18.dp),
                textStyle = AppTheme.typography.manropeRegular.copy(
                    color = AppTheme.colors.accentOrange,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                paddingTop = 0.dp,
                onClick = onDismiss
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ChoosePhotoSection(
    onTakePhoto: () -> Unit,
    onChooseFromLibrary: () -> Unit,
    onUseDivoPhoto: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(AppTheme.colors.backgroundLight),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.FindSimilarProfiles),
            style = AppTheme.typography.manropeRegular,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(6.dp))
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(R.string.UploadPhoto),
            style = AppTheme.typography.manropeRegular,
            fontSize = 13.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colors.textPrimary.copy(0.6f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))

        Divider(color = Color.LightGray, thickness = 0.5.dp)
        Button(
            modifier = Modifier.clickableWithoutRipple { onTakePhoto() },
            text = stringResource(R.string.TakePhoto)
        )
        Divider(color = Color.LightGray, thickness = 0.5.dp)
        Button(
            modifier = Modifier.clickableWithoutRipple { onChooseFromLibrary() },
            text = stringResource(R.string.ChooseFromLibrary)
        )
        Divider(color = Color.LightGray, thickness = 0.5.dp)
        Button(
            modifier = Modifier.clickableWithoutRipple { onUseDivoPhoto() },
            text = stringResource(R.string.UseDivoPhoto)
        )
    }
}

@Composable
private fun FaceSearchDisclaimerDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(270.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(AppTheme.colors.backgroundLight)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                modifier = Modifier.size(32.dp),
                painter = painterResource(R.drawable.ic_divo_face_rec),
                contentDescription = null,
                tint = AppTheme.colors.accentOrange,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.FaceSearchTitle),
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colors.textPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.FaceSearchDescriptionFirst),
                fontSize = 13.sp,
                color = AppTheme.colors.textPrimary.copy(0.6f),
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.FaceSearchDescriptionSecond),
                fontSize = 13.sp,
                color = AppTheme.colors.textPrimary.copy(0.6f),
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(32.dp))
            UIButtonNew(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.FaceSearchConfirm),
                textStyle = AppTheme.typography.helveticaNeueLtCom.copy(
                    fontSize = 16.sp,
                    color = AppTheme.colors.textColor
                ),
                height = 40.dp,
                onClick = onConfirm
            )
        }
    }
}

@Composable
private fun Button(
    modifier: Modifier = Modifier,
    text: String,
) {
    Box(
        modifier = modifier.height(56.dp).fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = AppTheme.typography.manropeRegular,
            fontSize = 17.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

