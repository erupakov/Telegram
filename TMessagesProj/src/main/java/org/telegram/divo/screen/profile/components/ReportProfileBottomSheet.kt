package org.telegram.divo.screen.profile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.style.AppTheme
import org.telegram.divo.common.arch.DivoLocaleProvider
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportProfileBottomSheet(
    reportTypes: Map<String, String>,
    onDismissRequest: () -> Unit,
    onReportOptionSelected: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        DivoLocaleProvider {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppTheme.colors.backgroundLight),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.ReportThisProfile),
                        style = AppTheme.typography.bodyMedium.copy(fontSize = 17.sp),
                        color = AppTheme.colors.textPrimary
                    )
                    Spacer(Modifier.height(16.dp))
                    Divider(color = Color.LightGray.copy(alpha = 0.5f))
                    
                    reportTypes.entries.forEachIndexed { index, entry ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickableWithoutRipple { onReportOptionSelected(entry.key) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                modifier = Modifier.padding(vertical = 16.dp, horizontal = 10.dp),
                                text = entry.value,
                                style = AppTheme.typography.helveticaNeueRegular,
                                fontSize = 16.sp,
                                lineHeight = 18.sp,
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                        }
                        if (index < reportTypes.size - 1) {
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AppTheme.colors.backgroundLight)
                        .clickableWithoutRipple { onDismissRequest() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 20.dp),
                        text = stringResource(R.string.ButtonCancel),
                        style = AppTheme.typography.helveticaNeueRegular,
                        fontSize = 17.sp,
                        color = AppTheme.colors.textPrimary
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
