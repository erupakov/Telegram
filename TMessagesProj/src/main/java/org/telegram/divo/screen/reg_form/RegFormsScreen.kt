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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.telegram.divo.common.AppSnackbarHost
import org.telegram.divo.common.AppSnackbarHostState
import org.telegram.divo.common.SnackbarEvent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    currentAccount: Int,
    phoneHash: String,
    phoneNumber: String,
    firebaseUid: String? = null,
    googleEmail: String? = null,
    viewModel: RegFormsViewModel = viewModel(),
    onFinished: (org.telegram.tgnet.TLRPC.TL_auth_authorization) -> Unit,
    onBack: () -> Unit,
    onBackToPhone: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.setIntent(RegFormsIntent.Init(
            subRole = subRole,
            currentAccount = currentAccount,
            phoneHash = phoneHash,
            phoneNumber = phoneNumber,
            firebaseUid = firebaseUid,
            googleEmail = googleEmail,
        ))
    }

    val snackbarHostState = remember { AppSnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RegFormsEffect.FinishRegistration -> onFinished(effect.authResponse)
                is RegFormsEffect.NavigateBack -> onBack()
                is RegFormsEffect.NavigateBackToPhone -> onBackToPhone()
                is RegFormsEffect.ShowError -> {
                    scope.launch {
                        snackbarHostState.show(SnackbarEvent.Error(effect.message))
                    }
                }
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
            snackbarHostState = snackbarHostState,
            onIntent = { viewModel.setIntent(it) }
        )
    }
}

@Composable
private fun RegFormsScreenContent(
    state: RegFormsState,
    snackbarHostState: AppSnackbarHostState,
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
        snackbarHost = { AppSnackbarHost(state = snackbarHostState) },
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