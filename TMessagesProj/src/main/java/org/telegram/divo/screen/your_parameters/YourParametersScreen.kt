package org.telegram.divo.screen.your_parameters

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.screen.your_parameters.components.ParameterBottomSheet
import org.telegram.divo.screen.your_parameters.components.ParameterItem
import org.telegram.divo.screen.your_parameters.components.ParametersBlock
import org.telegram.divo.screen.your_parameters.components.ParametersTopBar
import org.telegram.divo.screen.your_parameters.components.ParametersType
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YourParametersScreen(
    viewModel: YourParametersViewModel = viewModel(),
    showTitle: Boolean = true,
    isFromAgency: Boolean = false,
    onSaved: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val parametersSavedText = stringResource(R.string.ParametersSaved)

    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var currentParam by remember { mutableStateOf<ParametersType?>(null) }
    var currentParamOptions by remember { mutableStateOf<List<String>?>(null) }
    var currentValue by remember { mutableStateOf("") }

    val openBottomSheet = { param: ParametersType, options: List<String>?, value: String ->
        currentParam = param
        currentParamOptions = options
        currentValue = value
        showBottomSheet = true
    }

    LaunchedEffect(Unit) {
        viewModel.setIntent(YourParametersIntent.OnLoad)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is YourParametersEffect.SaveSuccess -> {
                    Toast.makeText(context, parametersSavedText, Toast.LENGTH_SHORT).show()
                    onSaved()
                }
                is YourParametersEffect.NavigateBack -> {
                    onBack()
                }
                is YourParametersEffect.Error -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    if (showBottomSheet) {
        ParameterBottomSheet(
            sheetState = sheetState,
            paramType = currentParam,
            options = currentParamOptions,
            initialValue = currentValue,
            onDismiss = { showBottomSheet = false },
            onSave = { selectedValue ->
                currentParam?.let { paramType ->
                    viewModel.setIntent(YourParametersIntent.OnParamValueChanged(paramType, selectedValue))
                }
                showBottomSheet = false
            },
            onDelete = {
                currentParam?.let { paramType ->
                    viewModel.setIntent(YourParametersIntent.OnParamCleared(paramType))
                }
                showBottomSheet = false
            }
        )
    }

    Column(
        modifier = Modifier.background(AppTheme.colors.backgroundLight)
    ) {
        if (isFromAgency) {
            ParametersTopBar(
                onSaveClicked = { viewModel.setIntent(YourParametersIntent.OnSaveClicked(isFromAgency)) },
                onBack = { viewModel.setIntent(YourParametersIntent.OnBackClicked) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LottieProgressIndicator()
                }
            }
            state.isError -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Не удалось загрузить данные",
                            color = Color.Gray,
                            fontSize = 24.sp,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        UIButtonNew(
                            text = "Повторить",
                            onClick = { viewModel.setIntent(YourParametersIntent.OnLoad) }
                        )
                    }
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    val headerHeight = 44.dp

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (showTitle) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(headerHeight)
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.YourParameters).uppercase(),
                                    style = AppTheme.typography.helveticaNeueLtCom,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 12.dp)
                                )
                            }
                        }
                        val genders = stringArrayResource(R.array.GenderItems).toList()
                        val breastSizes = stringArrayResource(R.array.BreastSizeItems).toList()

                        ParameterItem(
                            param = state.gender,
                            onClick = { openBottomSheet(ParametersType.GENDER, genders, state.gender?.value.orEmpty()) }
                        )
                        Spacer(Modifier.height(16.dp))
                        ParametersBlock(
                            items = state.blockParams,
                            onClick = { openBottomSheet(it.type, null, it.value) }
                        )
                        Spacer(Modifier.height(16.dp))
                        ParameterItem(
                            param = state.hairLength,
                            onClick = { openBottomSheet(ParametersType.HAIR_LENGTH, state.hairLengthOptions.map { it.title.orEmpty() }, state.hairLength?.value.orEmpty()) }
                        )
                        Spacer(Modifier.height(16.dp))
                        ParameterItem(
                            param = state.hairColor,
                            onClick = { openBottomSheet(ParametersType.HAIR_COLOR, state.hairColorOptions.map { it.title.orEmpty() }, state.hairColor?.value.orEmpty()) }
                        )
                        Spacer(Modifier.height(16.dp))
                        ParameterItem(
                            param = state.eyeColor,
                            onClick = { openBottomSheet(ParametersType.EYE_COLOR, state.eyeColorOptions.map { it.title.orEmpty() }, state.eyeColor?.value.orEmpty()) }
                        )
                        Spacer(Modifier.height(16.dp))
                        ParameterItem(
                            param = state.skinColor,
                            onClick = { openBottomSheet(ParametersType.SKIN_COLOR, state.skinColorOptions.map { it.title.orEmpty() }, state.skinColor?.value.orEmpty()) }
                        )

                        val gender = state.gender
                        if (gender != null && gender.value == stringResource(R.string.Female)) {
                            Spacer(Modifier.height(16.dp))
                            ParameterItem(
                                param = state.breastSize,
                                onClick = { openBottomSheet(ParametersType.BREAST_SIZE, breastSizes, state.breastSize?.value.orEmpty()) }
                            )
                        }

                        Spacer(Modifier.height(96.dp))
                    }

                    UIButtonNew(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .then(
                                if (isFromAgency) Modifier.navigationBarsPadding() else Modifier
                            )
                            .padding(bottom = 8.dp, start = 16.dp, end = 16.dp),
                        enabled = !state.isSaving,
                        onClick = { viewModel.setIntent(YourParametersIntent.OnSaveClicked(showTitle)) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun YourParametersScreenPreview() {
    YourParametersScreen()
}