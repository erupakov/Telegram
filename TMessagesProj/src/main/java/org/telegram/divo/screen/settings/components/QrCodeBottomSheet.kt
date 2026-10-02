package org.telegram.divo.screen.settings.components

import android.graphics.Bitmap
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.telegram.divo.common.utils.DivoShareType
import org.telegram.divo.common.utils.DivoSharingHelper
import org.telegram.divo.components.inputs.UIButton
import org.telegram.divo.style.AppTheme
import org.telegram.divo.common.arch.DivoLocaleProvider
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrCodeBottomSheet(
    userId: Int,
    message: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Состояние шаринга (дизейбл кнопки + лоадер)
    var isSharing by remember { mutableStateOf(false) }

    val profileLink = remember(userId) { DivoShareType.PROFILE.buildUrl(userId) }

    val qrBitmap by produceState<Bitmap?>(initialValue = null, profileLink) {
        value = withContext(Dispatchers.IO) {
            try {
                val logoDrawable = AppCompatResources.getDrawable(context, R.drawable.divo_logo)
                val logoBitmap = logoDrawable?.let {
                    val b = createBitmap(it.intrinsicWidth, it.intrinsicHeight)
                    val canvas = android.graphics.Canvas(b)
                    it.setBounds(0, 0, canvas.width, canvas.height)
                    it.draw(canvas)
                    b
                }

                val writer = QRCodeWriter()
                val hints = mapOf(
                    EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                    EncodeHintType.MARGIN to 0
                )

                writer.encode(profileLink, 768, 768, hints, null, 1.0f, 0xffffffff.toInt(), 0xff000000.toInt(), logoBitmap, 4)
            } catch (e: Exception) {
                null
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.backgroundLight,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
    ) {
        DivoLocaleProvider {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.YourQrCode),
                    style = AppTheme.typography.helveticaNeueLtCom.copy(fontWeight = FontWeight.Bold),
                    fontSize = 20.sp,
                    color = AppTheme.colors.textPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val bitmap = qrBitmap
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        CircularProgressIndicator(color = AppTheme.colors.accentOrange)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.ScanToProfile),
                    style = AppTheme.typography.helveticaNeueRegular,
                    fontSize = 15.sp,
                    color = AppTheme.colors.textPrimary.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Кнопка с лоадером и дизейблом
                UIButton(
                    text = stringResource(R.string.DivoShareBtn),
                    modifier = Modifier.fillMaxWidth(),
                    background = AppTheme.colors.accentOrange,
                    enabled = !isSharing,
                    isLoading = isSharing,
                    onClick = {
                        isSharing = true
                        DivoSharingHelper.share(
                            context = context,
                            scope = scope,
                            type = DivoShareType.PROFILE,
                            id = userId,
                            customMessage = message,
                        )
                        scope.launch {
                            delay(1000)
                            isSharing = false
                        }
                    }
                )
            }
        }
    }
}
