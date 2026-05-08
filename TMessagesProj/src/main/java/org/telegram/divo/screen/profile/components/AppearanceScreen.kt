package org.telegram.divo.screen.profile.components

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
import org.telegram.divo.common.utils.formattedAge
import org.telegram.divo.common.utils.toDateFloat
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.entity.EventModelAttributes
import org.telegram.divo.screen.profile.PhysicalParams
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    params: PhysicalParams?,
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
                        text = stringResource(R.string.AppearanceLabel).uppercase(),
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
            params?.let {
                ParameterItem(
                    label = stringResource(R.string.LabelGender),
                    value = params.gender.ifBlank { "-" }
                )
                val age = params.age.toDateFloat()
                ParameterItem(
                    label = stringResource(R.string.LabelAge),
                    value = age?.toString() ?: "-"
                )
                ParameterItem(
                    label = stringResource(R.string.LabelHeight),
                    value = if (params.height != 0f) params.height.toString() else "-"
                )
                ParameterItem(
                    label = stringResource(R.string.LabelHips),
                    value = if (params.hips != 0) params.hips.toString() else "-"
                )
                ParameterItem(
                    label = stringResource(R.string.LabelHairLength),
                    value = params.hairLength.ifBlank { "-" }
                )
                ParameterItem(
                    label = stringResource(R.string.LabelEyeColor),
                    value = params.eyeColor.ifBlank { "-" }
                )
                ParameterItem(
                    label = stringResource(R.string.LabelBreastSize),
                    value = params.breastSize.ifBlank { "-" }
                )
                ParameterItem(
                    label = stringResource(R.string.LabelWaist),
                    value = if (params.waist != 0) params.waist.toString() else "-"
                )
                ParameterItem(
                    label = stringResource(R.string.LabelShoeSize),
                    value = if (params.shoeSize != 0f) params.shoeSize.toString() else "-"
                )
                ParameterItem(
                    label = stringResource(R.string.LabelHairColor),
                    value = params.hairColor.ifBlank { "-" }
                )

                ParameterItem(
                    label = stringResource(R.string.LabelSkinColor),
                    value = params.skinColor.ifBlank { "-" },
                    showDivider = false
                )
                Spacer(Modifier.height(16.dp))
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