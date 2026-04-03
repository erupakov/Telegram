package org.telegram.divo.screen.event_details.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.entity.EventModelAttributes
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@Composable
fun ParametersCard(
    param: EventModelAttributes,
    onMoreClicked: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = AppTheme.colors.onBackground,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 10.dp
                )
        ) {
            Text(
                text = stringResource(R.string.ParametersForApplying),
                style = AppTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                color = AppTheme.colors.textPrimary,
            )
            Spacer(Modifier.height(10.dp))
            Row {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    ParameterItem(
                        label = stringResource(R.string.LabelGender),
                        value = param.genders.joinToString(", ")
                    )
                    val sep = if (param.weightFrom != null && param.weightTo != null) "-" else ""
                    ParameterItem(
                        label = stringResource(R.string.LabelWeight),
                        value = "${param.weightFrom}$sep${param.weightTo}"
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (param.ageFrom != null && param.ageTo != null) {
                        ParameterItem(
                            label = stringResource(R.string.LabelAge),
                            value = stringResource(R.string.AgeRange, param.ageFrom, param.ageTo)
                        )
                    }
                    val sep = if (param.waistFrom != null && param.waistTo != null) "-" else ""
                    ParameterItem(
                        label = stringResource(R.string.LabelWaist),
                        value = "${param.waistFrom}$sep${param.waistTo}"
                    )
                }
            }

            Spacer(Modifier.height(3.dp))
            Text(
                text = stringResource(R.string.SeeMore),
                color = AppTheme.colors.textPrimary,
                style = AppTheme.typography.helveticaNeueLtCom,
                fontSize = 10.sp,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickableWithoutRipple { onMoreClicked() }
                    .padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ParameterItem(
    label: String,
    value: String
) {
    Column() {
        Row(
            modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = label,
                style = AppTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                color = AppTheme.colors.textPrimary.copy(0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier.weight(1f),
                text = value,
                style = AppTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                color = AppTheme.colors.textPrimary,
                lineHeight = 16.sp,
                textAlign = TextAlign.End,
            )
        }
        Divider()
    }
}