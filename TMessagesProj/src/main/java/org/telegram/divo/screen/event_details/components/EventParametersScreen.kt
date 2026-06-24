package org.telegram.divo.screen.event_details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.MeasuringUnits
import org.telegram.divo.common.labelRes
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.entity.EventModelAttributes
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventParametersScreen(
    params: EventModelAttributes?,
    isNdaRequired: Boolean?,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AppTheme.colors.backgroundLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(top = 3.dp),
                        text = stringResource(R.string.ParametersForApplying).uppercase(),
                        style = AppTheme.typography.appBar
                    )
                },
                navigationIcon = {
                    RoundedButton(
                        modifier = Modifier.padding(start = 16.dp),
                        resId = R.drawable.ic_divo_back,
                        onClick = onBack
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppTheme.colors.backgroundLight
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(AppTheme.colors.onBackground)
                    .padding(horizontal = 16.dp)
            ) {
                ParameterItem(
                    label = stringResource(R.string.LabelGender),
                    value = params?.genders?.joinToString(", ").orEmpty()
                )
                if (params?.ageFrom != null && params.ageTo != null) {
                    ParameterItem(
                        label = stringResource(R.string.LabelAge),
                        value = stringResource(R.string.AgeRange, params.ageFrom, params.ageTo)
                    )
                }
                ParameterItem(
                    label = stringResource(ParametersType.WEIGHT.labelRes()),
                    value = MeasuringUnits.formatStoredRange(
                        ParametersType.WEIGHT,
                        params?.weightFrom,
                        params?.weightTo,
                        params?.measuringSystem,
                    )
                )
                ParameterItem(
                    label = stringResource(ParametersType.HEIGHT.labelRes()),
                    value = MeasuringUnits.formatStoredRange(
                        ParametersType.HEIGHT,
                        params?.heightFrom,
                        params?.heightTo,
                        params?.measuringSystem,
                    )
                )
                ParameterItem(
                    label = stringResource(R.string.LabelHairLength),
                    value = params?.hairLengths?.joinToString(", ").orEmpty()
                )
                ParameterItem(
                    label = stringResource(R.string.LabelEyeColor),
                    value = params?.eyeColors?.joinToString(", ").orEmpty()
                )
                ParameterItem(
                    label = stringResource(ParametersType.BREAST_SIZE.labelRes()),
                    value = MeasuringUnits.formatStoredRange(
                        ParametersType.BREAST_SIZE,
                        params?.breastSizeFrom,
                        params?.breastSizeTo,
                        params?.measuringSystem,
                    )
                )
                ParameterItem(
                    label = stringResource(ParametersType.WAIST.labelRes()),
                    value = MeasuringUnits.formatStoredRange(
                        ParametersType.WAIST,
                        params?.waistFrom,
                        params?.waistTo,
                        params?.measuringSystem,
                    )
                )
                ParameterItem(
                    label = stringResource(ParametersType.SHOE_SIZE.labelRes()),
                    value = MeasuringUnits.formatStoredRange(
                        ParametersType.SHOE_SIZE,
                        params?.shoesSizeFrom,
                        params?.shoesSizeTo,
                        params?.measuringSystem,
                    )
                )
                ParameterItem(
                    label = stringResource(R.string.LabelHairColor),
                    value = params?.hairColors?.joinToString(", ").orEmpty()
                )
                ParameterItem(
                    label = stringResource(ParametersType.HIPS.labelRes()),
                    value = MeasuringUnits.formatStoredRange(
                        ParametersType.HIPS,
                        params?.hipsFrom,
                        params?.hipsTo,
                        params?.measuringSystem,
                    )
                )
    
                ParameterItem(
                    label = stringResource(R.string.LabelSkinColor),
                    value = params?.skinColors?.joinToString(", ").orEmpty(),
                    showDivider = false
                )
                Spacer(Modifier.height(16.dp))
            }

            if (isNdaRequired != null) {
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(AppTheme.colors.onBackground)
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.weight(0.35f),
                        text = stringResource(R.string.EventNdaRequired),
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        modifier = Modifier.weight(0.65f),
                        text = if (isNdaRequired) stringResource(R.string.EventRequired) else stringResource(R.string.EventNotRequired),
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.textPrimary.copy(0.6f),
                        lineHeight = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

@Composable
private fun ParameterItem(
    label: String,
    value: String,
    showDivider: Boolean = true
) {
    Column() {
        Row(
            modifier = Modifier.padding(top = 28.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.weight(0.35f),
                text = label,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                modifier = Modifier.weight(0.65f),
                text = value,
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary.copy(0.6f),
                lineHeight = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.End,
            )
        }

        if (showDivider) Divider()
    }
}