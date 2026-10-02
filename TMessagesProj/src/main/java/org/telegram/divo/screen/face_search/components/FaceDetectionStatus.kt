package org.telegram.divo.screen.face_search.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.screen.face_search.FaceDetectionResult
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun FaceDetectionStatus(result: FaceDetectionResult) {
    when (result) {
        is FaceDetectionResult.NoFace -> {
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.NoFaceDetected),
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 20.sp,
                color = AppTheme.colors.textPrimary
            )
            Spacer(Modifier.height(10.dp))
            Text(
                modifier = Modifier.fillMaxWidth(0.9f),
                text = stringResource(R.string.PleaseUsePhotoClearly),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textPrimary,
            )
            Spacer(Modifier.height(8.dp))
            Column {
                Text(
                    text = "· " + stringResource(R.string.AvoidSunglasses),
                    style = AppTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = AppTheme.colors.textPrimary.copy(0.6f),
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "· " + stringResource(R.string.FullFacePhotos),
                    style = AppTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = AppTheme.colors.textPrimary.copy(0.6f),
                )
            }
        }
        else -> {}
    }
}