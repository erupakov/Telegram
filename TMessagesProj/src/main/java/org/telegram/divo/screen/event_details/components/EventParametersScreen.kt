package org.telegram.divo.screen.event_details.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.entity.EventModelAttributes
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventParametersScreen(
    params: EventModelAttributes?,
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
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(AppTheme.colors.onBackground)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
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
            val sep = if (params?.weightFrom != null && params.weightTo != null) "-" else ""
            ParameterItem(
                label = stringResource(R.string.LabelWeight),
                value = "${params?.weightFrom}$sep${params?.weightTo}"
            )
            val sepHeight = if (params?.heightFrom != null && params.heightTo != null) "-" else ""
            ParameterItem(
                label = stringResource(R.string.LabelHeight),
                value = "${params?.heightFrom}$sepHeight${params?.heightTo}"
            )
            ParameterItem(
                label = stringResource(R.string.LabelHairLength),
                value = params?.hairLengths?.joinToString(", ").orEmpty()
            )
            ParameterItem(
                label = stringResource(R.string.LabelEyeColor),
                value = params?.eyeColors?.joinToString(", ").orEmpty()
            )
            val sepBreast = if (params?.breastSizeFrom != null && params.breastSizeTo != null) "-" else ""
            ParameterItem(
                label = stringResource(R.string.LabelBreastSize),
                value = "${params?.breastSizeFrom}$sepBreast${params?.breastSizeTo}"
            )
            val waistSep = if (params?.waistFrom != null && params.waistTo != null) "-" else ""
            ParameterItem(
                label = stringResource(R.string.LabelWaist),
                value = "${params?.waistFrom}$waistSep${params?.waistTo}"
            )
            val sepShoe = if (params?.shoesSizeFrom != null && params.shoesSizeTo != null) "-" else ""
            ParameterItem(
                label = stringResource(R.string.LabelShoeSizeUS),
                value = "${params?.shoesSizeFrom}$sepShoe${params?.shoesSizeTo}"
            )
            ParameterItem(
                label = stringResource(R.string.LabelHairColor),
                value = params?.hairColors?.joinToString(", ").orEmpty()
            )
            val sepHips = if (params?.hipsFrom != null && params?.hipsTo != null) "-" else ""
            ParameterItem(
                label = stringResource(R.string.LabelHips),
                value = "${params?.hipsFrom}$sepHips${params?.hipsTo}"
            )

            ParameterItem(
                label = stringResource(R.string.LabelSkinColor),
                value = params?.skinColors?.joinToString(", ").orEmpty(),
                showDivider = false
            )
            Spacer(Modifier.height(16.dp))
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