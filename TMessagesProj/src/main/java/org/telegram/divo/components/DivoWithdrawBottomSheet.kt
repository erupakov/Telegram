package org.telegram.divo.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.style.AppTheme
import org.telegram.divo.style.DivoLocaleProvider
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DivoWithdrawBottomSheet(
    onKeepApplication: () -> Unit,
    onWithdraw: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        DivoLocaleProvider {
            Column(
                modifier = Modifier
                    .fillMaxWidth()

                    .padding(horizontal = 8.dp)
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
            ) {
                // First Block
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppTheme.colors.onBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.WithdrawApplicationTitle),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 13.sp,
                            color = AppTheme.colors.textPrimary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.WithdrawApplicationMessage),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 13.sp,
                            color = AppTheme.colors.textPrimary.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }

                    HorizontalDivider(
                        color = Color.LightGray.copy(alpha = 0.8f),
                        thickness = 0.5.dp
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onKeepApplication() }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.KeepApplicationButton),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 17.sp,
                            color = AppTheme.colors.accentOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Second Block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppTheme.colors.onBackground)
                        .clickable { onWithdraw() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.YesWithdrawButton),
                        style = AppTheme.typography.helveticaNeueRegular,
                        fontSize = 17.sp,
                        color = AppTheme.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
