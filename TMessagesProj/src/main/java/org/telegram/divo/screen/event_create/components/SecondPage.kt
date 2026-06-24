package org.telegram.divo.screen.event_create.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.telegram.divo.common.utils.getFormatedOptions
import org.telegram.divo.components.DivoTextField
import org.telegram.divo.components.items.ParameterBottomSheet
import org.telegram.divo.components.items.ParameterItem
import org.telegram.divo.components.items.ParametersBlock
import org.telegram.divo.components.items.ParametersType
import org.telegram.divo.screen.event_create.Intent
import org.telegram.divo.screen.event_create.State
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecondPage(
    state: State,
    onIntent: (Intent) -> Unit,
) {
    val context = LocalContext.current

    val draftBlockParams = state.blockParams.ifEmpty { state.getDefaultBlockParams() }

    var showParametersSheet by remember { mutableStateOf(false) }
    var currentParam by remember { mutableStateOf<ParametersType?>(null) }
    var currentOptions by remember { mutableStateOf<List<String>?>(null) }
    var currentValue by remember { mutableStateOf("") }

    var showParticipantsSheet by remember { mutableStateOf(false) }

    val openSheet = { type: ParametersType, options: List<String>?, value: String ->
        currentParam = type
        currentOptions = options
        currentValue = value
        showParametersSheet = true
    }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        EventItem(
            title = stringResource(R.string.EventWhoCanApply),
            value = if (state.role.value.isEmpty()) stringResource(R.string.EventChooseTalent) else state.role.getFormatedOptions(),
            placeholder = stringResource(R.string.EventChooseTalent),
            onClick = {
                openSheet(
                    ParametersType.ROLE,
                    context.resources.getStringArray(R.array.ModelNewTalentAgency).toList(),
                    state.role.value
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        // Max participants
        EventItem(
            title = stringResource(R.string.EventMaxParticipants),
            value = if (state.maxParticipants > 0) state.maxParticipants.toString() else "",
            placeholder = stringResource(R.string.EventSelectMaxParticipants),
            onClick = { showParticipantsSheet = true }
        )

        Spacer(Modifier.height(16.dp))

        // Requirements
        SectionHeader(text = stringResource(R.string.EventRequirements))
        Spacer(Modifier.height(6.dp))
        DivoTextField(
            value = state.eventRequirements,
            onValueChange = { onIntent(Intent.OnRequirementsChanged(it)) },
            placeholder = stringResource(R.string.EventEnterRequirements),
            backgroundColor = AppTheme.colors.onBackground,
            cornerRadius = 16.dp,
            height = 114.dp,
            placeholderColor = AppTheme.colors.textPrimary.copy(0.4f),
            minLines = 5,
            maxLines = 5,
            textStyle = AppTheme.typography.bodyLarge.copy(
                color = AppTheme.colors.textPrimary
            ),
            horizontalContentPadding = 16.dp
        )

        Spacer(Modifier.height(16.dp))

        // Parameters for applying
        SectionHeader(text = stringResource(R.string.EventParametersForApplying))
        Spacer(Modifier.height(6.dp))

        ParameterItem(
            param = state.gender.copy(
                value = if (state.gender.value.isEmpty()) stringResource(R.string.AllGendersLabel)
                        else state.gender.getFormatedOptions()
            ),
            onClick = {
                openSheet(
                    ParametersType.GENDER,
                    context.resources.getStringArray(R.array.GenderItems).toList(),
                    state.gender.value
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        ParametersBlock(
            items = draftBlockParams,
            onClick = { param -> openSheet(param.type, null, param.value) }
        )

        Spacer(Modifier.height(16.dp))

        ParameterItem(
            param = state.hairLength.copy(
                value = if (state.hairLength.value.isEmpty()) stringResource(R.string.AllLengthsLabel)
                        else state.hairLength.getFormatedOptions()
            ),
            onClick = {
                openSheet(
                    ParametersType.HAIR_LENGTH,
                    listOf(context.getString(R.string.AllLengthsLabel)) + state.hairLengthOptions.map { it.title.orEmpty() },
                    state.hairLength.value
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        ParameterItem(
            param = state.hairColor.copy(
                value = if (state.hairColor.value.isEmpty()) stringResource(R.string.AllColorsLabel)
                        else state.hairColor.getFormatedOptions()
            ),
            onClick = {
                openSheet(
                    ParametersType.HAIR_COLOR,
                    listOf(context.getString(R.string.AllColorsLabel)) + state.hairColorOptions.map { it.title.orEmpty() },
                    state.hairColor.value
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        ParameterItem(
            param = state.eyeColor.copy(
                value = if (state.eyeColor.value.isEmpty()) stringResource(R.string.AllColorsLabel)
                        else state.eyeColor.getFormatedOptions()
            ),
            onClick = {
                openSheet(
                    ParametersType.EYE_COLOR,
                    listOf(context.getString(R.string.AllColorsLabel)) + state.eyeColorOptions.map { it.title.orEmpty() },
                    state.eyeColor.value
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        ParameterItem(
            param = state.skinColor.copy(
                value = if (state.skinColor.value.isEmpty()) stringResource(R.string.AllColorsLabel)
                        else state.skinColor.getFormatedOptions()
            ),
            onClick = {
                openSheet(
                    ParametersType.SKIN_COLOR,
                    listOf(context.getString(R.string.AllColorsLabel)) + state.skinColorOptions.map { it.title.orEmpty() },
                    state.skinColor.value
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        SwitchItem(
            text = stringResource(R.string.EventNdaRequired),
            checked = state.isNdaRequired,
            onChanged = { onIntent(Intent.OnNdaToggled(it)) }
        )

        Spacer(Modifier.height(76.dp))
    }

    if (showParametersSheet) {
        ParameterBottomSheet(
            paramType = currentParam,
            options = currentOptions,
            isMultiSelect = true,
            initialValue = currentValue,
            iconClose = R.drawable.ic_divo_back,
            useNumericRangeUi = true,
            onDismiss = { showParametersSheet = false },
            onSave = { selectedValue ->
                currentParam?.let { type ->
                    val intent = when (type) {
                        ParametersType.ROLE -> Intent.OnRoleChanged(state.role.copy(value = selectedValue))
                        ParametersType.GENDER -> Intent.OnGenderChanged(state.gender.copy(value = selectedValue))
                        ParametersType.HAIR_LENGTH -> Intent.OnHairLengthChanged(state.hairLength.copy(value = selectedValue))
                        ParametersType.HAIR_COLOR -> Intent.OnHairColorChanged(state.hairColor.copy(value = selectedValue))
                        ParametersType.EYE_COLOR -> Intent.OnEyeColorChanged(state.eyeColor.copy(value = selectedValue))
                        ParametersType.SKIN_COLOR -> Intent.OnSkinColorChanged(state.skinColor.copy(value = selectedValue))
                        else -> {
                            val updated = draftBlockParams.find { it.type == type }?.copy(value = selectedValue)
                            updated?.let { Intent.OnBlockParamChanged(it) }
                        }
                    }
                    intent?.let { onIntent(it) }
                }
                showParametersSheet = false
            },
            onDelete = {
                currentParam?.let { type ->
                    val intent = when (type) {
                        ParametersType.ROLE -> Intent.OnRoleChanged(state.role.copy(value = ""))
                        ParametersType.GENDER -> Intent.OnGenderChanged(state.gender.copy(value = ""))
                        ParametersType.HAIR_LENGTH -> Intent.OnHairLengthChanged(state.hairLength.copy(value = ""))
                        ParametersType.HAIR_COLOR -> Intent.OnHairColorChanged(state.hairColor.copy(value = ""))
                        ParametersType.EYE_COLOR -> Intent.OnEyeColorChanged(state.eyeColor.copy(value = ""))
                        ParametersType.SKIN_COLOR -> Intent.OnSkinColorChanged(state.skinColor.copy(value = ""))
                        else -> {
                            val updated = draftBlockParams.find { it.type == type }?.copy(value = "")
                            updated?.let { Intent.OnBlockParamChanged(it) }
                        }
                    }
                    intent?.let { onIntent(it) }
                }
                showParametersSheet = false
            }
        )
    }

    if (showParticipantsSheet) {
        ParticipantsPickerSheet(
            initialValue = state.maxParticipants,
            onDismiss = { showParticipantsSheet = false },
            onSelected = { value ->
                onIntent(Intent.OnMaxParticipantsChanged(value))
                showParticipantsSheet = false
            }
        )
    }
}