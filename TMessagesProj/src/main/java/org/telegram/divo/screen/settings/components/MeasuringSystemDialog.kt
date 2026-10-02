package org.telegram.divo.screen.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.telegram.divo.common.DivoSettings
import org.telegram.divo.common.compose.clickableWithoutRipple
import org.telegram.divo.components.inputs.RadioCircle
import org.telegram.divo.components.inputs.UIButton
import org.telegram.divo.style.AppTheme
import org.telegram.divo.common.arch.DivoLocaleProvider
import org.telegram.messenger.R

@Composable
fun MeasuringSystemDialog(
    currentSystem: String,
    onApply: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSystem by remember { mutableStateOf(currentSystem) }

    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(28.dp),
            color = AppTheme.colors.backgroundLight,
            modifier = Modifier.fillMaxWidth()
        ) {
            DivoLocaleProvider {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp)
                ) {
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = stringResource(R.string.MeasuringSystemLabel),
                        style = AppTheme.typography.helveticaNeueLtCom.copy(fontWeight = FontWeight.Bold),
                        fontSize = 20.sp,
                        color = Color.Black,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableWithoutRipple { selectedSystem = DivoSettings.SYSTEM_METRIC }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioCircle(selected = selectedSystem == DivoSettings.SYSTEM_METRIC)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            modifier = Modifier.offset(y = 2.dp),
                            text = stringResource(R.string.MeasuringSystemMetric),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = AppTheme.colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickableWithoutRipple { selectedSystem = DivoSettings.SYSTEM_IMPERIAL }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioCircle(selected = selectedSystem == DivoSettings.SYSTEM_IMPERIAL)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            modifier = Modifier.offset(y = 2.dp),
                            text = stringResource(R.string.MeasuringSystemImperial),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 18.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = AppTheme.colors.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(28.dp))
                    UIButton(
                        text = stringResource(R.string.ButtonApply),
                        height = 46.dp,
                        modifier = Modifier.fillMaxWidth(),
                        background = AppTheme.colors.buttonSecondary,
                        onClick = { onApply(selectedSystem) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}