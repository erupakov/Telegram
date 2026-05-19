package org.telegram.divo.screen.reg_form

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.telegram.divo.components.LottieProgressIndicator
import org.telegram.divo.components.RoundedButton
import org.telegram.divo.components.UIButtonNew
import org.telegram.divo.screen.reg_form.components.StepOne
import org.telegram.divo.screen.reg_form.components.StepPhoto
import org.telegram.divo.screen.reg_form.components.StepThree
import org.telegram.divo.screen.reg_form.components.StepTwo
import org.telegram.divo.screen.reg_select_role.SubRole
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.R

@Composable
fun RegFormsScreen(
    subRole: SubRole,
    viewModel: RegFormsViewModel = viewModel(),
    onFinished: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setIntent(RegFormsIntent.Init(subRole))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RegFormsEffect.FinishRegistration -> onFinished()
                is RegFormsEffect.NavigateBack -> onBack()
            }
        }
    }

    BackHandler {
        viewModel.setIntent(RegFormsIntent.OnBack)
    }

    if (state.formData == null || state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(AppTheme.colors.backgroundLight),
            contentAlignment = Alignment.Center
        ) {
            LottieProgressIndicator(modifier = Modifier.size(32.dp))
        }
    } else {
        RegFormsScreenContent(
            state = state,
            onIntent = { viewModel.setIntent(it) }
        )
    }
}

@Composable
private fun RegFormsScreenContent(
    state: RegFormsState,
    onIntent: (RegFormsIntent) -> Unit
) {
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { state.totalSteps }
    )

    LaunchedEffect(state.currentStepIndex) {
        if (pagerState.currentPage != state.currentStepIndex) {
            pagerState.animateScrollToPage(state.currentStepIndex)
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                currentPage = state.currentStepIndex + 1,
                totalPages = state.totalSteps,
                onBack = { onIntent(RegFormsIntent.OnBack) }
            )
        },
        containerColor = AppTheme.colors.backgroundLight,
        contentWindowInsets = WindowInsets(0),
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
                userScrollEnabled = false,
                verticalAlignment = Alignment.Top
            ) {
                when (state.currentStep) {
                    RegFormStep.COMPANY_INFO,
                    RegFormStep.IDENTITY,
                    RegFormStep.CREATIVE_IDENTITY,
                    RegFormStep.STUDIO_DETAILS,
                    RegFormStep.TALENT_IDENTITY,
                    RegFormStep.FAN_IDENTITY -> StepOne(
                        formData = state.formData ?: return@HorizontalPager,
                        state = state,
                        onIntent = onIntent
                    )

                    RegFormStep.COMPANY_LOCATION,
                    RegFormStep.PERSONAL_DETAILS,
                    RegFormStep.STUDIO_CONTACT,
                    RegFormStep.TALENT_LOCATION -> StepTwo(
                        formData = state.formData ?: return@HorizontalPager,
                        state = state,
                        onIntent = onIntent
                    )

                    RegFormStep.COMPANY_CONTACT,
                    RegFormStep.PROFESSIONAL_LINKS,
                    RegFormStep.TALENT_LINKS,
                    RegFormStep.PORTFOLIO -> StepThree(
                        formData = state.formData ?: return@HorizontalPager,
                        onIntent = onIntent
                    )

                    RegFormStep.COMPANY_PHOTO,
                    RegFormStep.PROFILE_PHOTO,
                    RegFormStep.STUDIO_PHOTO,
                    RegFormStep.TALENT_PHOTO,
                    RegFormStep.FAN_PHOTO -> StepPhoto(
                        formData = state.formData ?: return@HorizontalPager,
                        onIntent = onIntent,
//                        onDone = { onIntent(RegFormsIntent.OnContinue) },
//                        onSkip = if (state.formData?.subRole == SubRole.FAN)
//                        { { onIntent(RegFormsIntent.OnContinue) } }
//                        else null
                    )

                    null -> Unit
                }
            }

            UIButtonNew(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = (8 + AndroidUtilities.navigationBarHeight / AndroidUtilities.density).dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.BottomCenter),
                text = if (state.isLastStep)
                    stringResource(R.string.ButtonDone)
                else
                    stringResource(R.string.ButtonContinue),
                enabled = state.canContinue,
                onClick = { onIntent(RegFormsIntent.OnContinue) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onBack: () -> Unit,
    currentPage: Int,
    totalPages: Int,
) {
    val statusBarHeightPx = if (AndroidUtilities.isTablet()) 0 else AndroidUtilities.statusBarHeight

    CenterAlignedTopAppBar(
        title = {
            Text(
                modifier = Modifier.padding(end = 16.dp),
                text = stringResource(R.string.RegStepProgress, currentPage, totalPages),
                color = AppTheme.colors.textPrimary,
                style = AppTheme.typography.bodyMedium
            )
        },
        navigationIcon = {
            RoundedButton(
                modifier = Modifier.padding(start = 12.dp),
                onClick = onBack
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.colors.backgroundLight
        ),
        windowInsets = WindowInsets(top = statusBarHeightPx)
    )
}