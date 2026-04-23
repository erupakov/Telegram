package org.telegram.divo.screen.similar_profiles.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.telegram.divo.common.clickableWithoutRipple
import org.telegram.divo.components.SteppedDivoSlider
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.components.items.DivoBottomSheet
import org.telegram.divo.components.items.ParameterBottomSheet
import org.telegram.divo.components.items.ParameterItem
import org.telegram.divo.components.items.ParametersBlock
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.components.items.ProfileParameter
import org.telegram.divo.screen.add_model.CountryPickerSheet
import org.telegram.divo.screen.add_model.LocalCountry
import org.telegram.divo.screen.similar_profiles.MIN_SIMILARITY
import org.telegram.divo.screen.similar_profiles.State
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimilarFilterBottomSheet(
    uiState: State,
    onApply: (
        countries: List<LocalCountry>,
        percent: Int,
        role: ProfileParameter,
        blockParams: List<ProfileParameter>
    ) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current

    var draftCountries by remember { mutableStateOf(uiState.selectedCountries) }
    var draftPercent by remember { mutableIntStateOf(uiState.similarityPercent) }
    var draftRole by remember { mutableStateOf(uiState.role) }
    var draftBlockParams by remember {
        mutableStateOf(uiState.blockParams.ifEmpty { uiState.getDefaultBlockParams() })
    }

    var showParametersSheet by remember { mutableStateOf(false) }
    var showCountrySheet by rememberSaveable { mutableStateOf(false) }

    var showMoreParameters by remember { mutableStateOf(false) }

    var currentParam by remember { mutableStateOf<ParametersType?>(null) }
    var currentParamOptions by remember { mutableStateOf<List<String>?>(null) }
    var currentValue by remember { mutableStateOf("") }

    val openBottomSheet = { param: ParametersType, options: List<String>?, value: String ->
        currentParam = param
        currentParamOptions = options
        currentValue = value
        showParametersSheet = true
    }

    if (showCountrySheet) {
        CountryPickerSheet(
            list = uiState.allCountries,
            selectedCountries = draftCountries,
            isMultiSelection = true,
            onDismiss = { showCountrySheet = false },
            onPick = { selectedList ->
                draftCountries = selectedList
                showCountrySheet = false
            }
        )
    }

    if (showParametersSheet) {
        ParameterBottomSheet(
            paramType = currentParam,
            options = currentParamOptions,
            isMultiSelect = true,
            initialValue = currentValue,
            iconClose = R.drawable.ic_divo_back,
            onDismiss = { showParametersSheet = false },
            onSave = { selectedValue ->
                currentParam?.let { paramType ->
                    when (paramType) {
                        ParametersType.ROLE -> draftRole = uiState.role.copy(value = selectedValue)
                        else -> {
                            draftBlockParams = draftBlockParams.map { item ->
                                if (item.type == paramType) item.copy(value = selectedValue) else item
                            }
                        }
                    }
                }
                showParametersSheet = false
            },
            onDelete = {
                currentParam?.let { paramType ->
                    when (paramType) {
                        ParametersType.ROLE -> draftRole = draftRole.copy(value = "")

                        else -> {
                            draftBlockParams = draftBlockParams.map { item ->
                                if (item.type == paramType) item.copy(value = "") else item
                            }
                        }
                    }
                }
                showParametersSheet = false
            }
        )
    }

    DivoBottomSheet(
        title = stringResource(R.string.FilterResults),
        onDismiss = onDismiss,
        isSaveMode = false,
        contentPadding = PaddingValues(horizontal = 16.dp),
        onReset = {
            draftCountries = emptyList()
            draftPercent = MIN_SIMILARITY
            draftRole = ProfileParameter(ParametersType.ROLE, "")
            draftBlockParams = draftBlockParams.map { it.copy(value = "") }

            onReset()
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 70.dp)
            ) {
                FilterSliderSection(
                    selectedPercent = draftPercent,
                    onPercentChange = { newValue -> draftPercent = newValue }
                )
                Spacer(Modifier.height(16.dp))
                ParameterItem(
                    param = ProfileParameter(
                        type = ParametersType.COUNTRY,
                        value = if (draftCountries.isEmpty()) stringResource(R.string.CountriesValue) else uiState.getFormatedCountries(draftCountries)
                    ),
                    weightLabel = 0.4f,
                    weightValue = 0.6f,
                    onClick = { showCountrySheet = true }
                )
                Spacer(Modifier.height(16.dp))
                ParameterItem(
                    param = draftRole.copy(value = if (draftRole.value.isEmpty()) stringResource(R.string.AllLabel) else uiState.getFormatedRoles(draftRole)),
                    onClick = { openBottomSheet(draftRole.type, context.resources.getStringArray(R.array.ModelFunNewTalent).toList(), draftRole.value) }
                )
                Spacer(Modifier.height(16.dp))

                if (uiState.isModel && !showMoreParameters) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier.clickableWithoutRipple { showMoreParameters = true},
                            text = stringResource(R.string.MoreParameters),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 15.sp,
                            color = AppTheme.colors.accentOrange
                        )
                    }
                }

                if (showMoreParameters) {
                    ParametersBlock(
                        items = draftBlockParams,
                        onClick = { param -> openBottomSheet(param.type, null, param.value) }
                    )
                }

                if (showMoreParameters) {
                    Spacer(Modifier.height(20.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier.clickableWithoutRipple { showMoreParameters = false },
                            text = stringResource(R.string.LessParameters),
                            style = AppTheme.typography.helveticaNeueRegular,
                            fontSize = 15.sp,
                            color = AppTheme.colors.accentOrange
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }

            UIButtonNew(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                text = stringResource(R.string.ApplyFilter),
                onClick = {
                    onApply(draftCountries, draftPercent, draftRole, draftBlockParams)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun FilterSliderSection(
    selectedPercent: Int,
    onPercentChange: (Int) -> Unit
) {
    val steps = listOf(30, 45, 60, 75, 85, 100)
    val tailLength = 40.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(AppTheme.colors.onBackground)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.MinimumSimilarity),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.colors.textPrimary
            )
            Text(
                text = "$selectedPercent%",
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.textPrimary.copy(0.6f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        SteppedDivoSlider(
            steps = steps,
            currentStep = selectedPercent,
            tailLength = tailLength,
            onStepChange = { newValue -> onPercentChange(newValue) }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Layout(
            content = {
                steps.forEach { step ->
                    Text(
                        text = "$step%",
                        color = AppTheme.colors.textPrimary,
                        fontSize = 12.sp,
                        style = AppTheme.typography.helveticaNeueRegular,
                        textAlign = TextAlign.Center
                    )
                }
            }
        ) { measurables, constraints ->
            val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }

            val width = constraints.maxWidth
            val startOffsetPx = tailLength.toPx()
            val endOffsetPx = width - startOffsetPx
            val trackDotsWidthPx = endOffsetPx - startOffsetPx
            val segmentsCount = steps.size - 1

            val height = placeables.maxOfOrNull { it.height } ?: 0

            layout(width, height) {
                placeables.forEachIndexed { i, placeable ->
                    val dotCenterPx = startOffsetPx + (i * (trackDotsWidthPx / segmentsCount))

                    placeable.placeRelative(
                        x = (dotCenterPx - placeable.width / 2).toInt(),
                        y = 0
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.HigherValuesShow),
            color = AppTheme.colors.textPrimary,
            fontSize = 12.sp,
            style = AppTheme.typography.helveticaNeueRegular,
        )
    }
}