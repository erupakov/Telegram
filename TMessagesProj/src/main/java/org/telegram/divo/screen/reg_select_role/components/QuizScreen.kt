package org.telegram.divo.screen.reg_select_role.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.telegram.divo.common.controllers.AppSnackbarHost
import org.telegram.divo.common.controllers.AppSnackbarHostState
import org.telegram.divo.components.inputs.RoundedButton
import org.telegram.divo.components.inputs.UIButton
import org.telegram.divo.screen.reg_select_role.RoleSelectionIntent
import org.telegram.divo.screen.reg_select_role.RoleSelectionState
import org.telegram.divo.screen.reg_select_role.RoleSelectionViewModel
import org.telegram.divo.screen.reg_select_role.SubRole
import org.telegram.divo.screen.reg_select_role.UserIntent
import org.telegram.divo.style.AppTheme
import org.telegram.messenger.AndroidUtilities
import org.telegram.messenger.R

@Composable
fun QuizScreen(
    intent: UserIntent,
    viewModel: RoleSelectionViewModel = viewModel(),
    onComplete: (SubRole) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )
    val snackbarState = remember { AppSnackbarHostState() }

    // Валидация кнопки Continue
    val canContinue = when {
        intent == UserIntent.GET_HIRED && pagerState.currentPage == 0 ->
            state.subRole != null
        intent == UserIntent.GET_HIRED && pagerState.currentPage == 1 ->
            state.hasModelingExperience != null
        intent == UserIntent.LOOKING_FOR_TALENT && pagerState.currentPage == 0 ->
            state.hiringType != null
        intent == UserIntent.LOOKING_FOR_TALENT && pagerState.currentPage == 1 ->
            state.subRole != null
        else -> false
    }

    LaunchedEffect(state.resetPagerToFirstPage) {
        if (state.resetPagerToFirstPage) {
            pagerState.scrollToPage(0)
            viewModel.setIntent(RoleSelectionIntent.OnFirstPageReached)
        }
    }

    BackHandler {
        if (pagerState.currentPage == 0) onBack()
        else scope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }

    Scaffold(
        topBar = {
            val totalPages = when {
                intent == UserIntent.GET_HIRED && (state.subRole == SubRole.MODEL || state.subRole == SubRole.NEW_TALENT) -> pagerState.pageCount
                intent == UserIntent.LOOKING_FOR_TALENT -> pagerState.pageCount
                else -> 1
            }

            TopBar(
                currentPage = pagerState.currentPage + 1,
                totalPages = totalPages,
                onBack = {
                    if (pagerState.currentPage == 0) onBack()
                    else scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
            )
        },
        containerColor = AppTheme.colors.backgroundLight,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = {
            AppSnackbarHost(state = snackbarState, bottomPadding = 76.dp)
        },
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
            ) { page ->
                when (intent) {
                    UserIntent.GET_HIRED -> GetHiredPage(
                        page = page,
                        state = state,
                        onSubRoleSelected = { viewModel.setIntent(RoleSelectionIntent.OnSubRoleSelected(it)) },
                        onExperienceSelected = { viewModel.setIntent(RoleSelectionIntent.OnModelingExperienceSelected(it)) }
                    )
                    UserIntent.LOOKING_FOR_TALENT -> LookingForTalentPage(
                        page = page,
                        state = state,
                        onHiringTypeSelected = { viewModel.setIntent(RoleSelectionIntent.OnHiringTypeSelected(it)) },
                        onSubRoleSelected = { viewModel.setIntent(RoleSelectionIntent.OnSubRoleSelected(it)) }
                    )
                    else -> Unit
                }
            }

            UIButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = (8 + AndroidUtilities.navigationBarHeight / AndroidUtilities.density).dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.BottomCenter),
                text = stringResource(R.string.ButtonContinue),
                enabled = canContinue,
                onClick = {
                    scope.launch {
                        val isLastPage = pagerState.currentPage == pagerState.pageCount - 1

                        when {
                            intent == UserIntent.GET_HIRED
                                    && pagerState.currentPage == 0
                                    && state.subRole == SubRole.MODEL -> {
                                pagerState.animateScrollToPage(1)
                            }

                            intent == UserIntent.GET_HIRED
                                    && pagerState.currentPage == 0
                                    && state.subRole != SubRole.MODEL -> {
                                val finalRole = state.subRole
                                if (finalRole != null) onComplete(finalRole)
                            }

                            intent == UserIntent.LOOKING_FOR_TALENT
                                    && pagerState.currentPage == 0 -> {
                                pagerState.animateScrollToPage(1)
                            }

                            isLastPage -> {
                                val finalRole = resolveFinalSubRole(state)
                                if (finalRole != null) onComplete(finalRole)
                            }
                        }
                    }
                }
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
                text = stringResource(R.string.QuizStepProgress, currentPage, totalPages),
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

private fun resolveFinalSubRole(state: RoleSelectionState): SubRole? {
    return when (state.subRole) {
        SubRole.MODEL -> when (state.hasModelingExperience) {
            true -> SubRole.MODEL
            false -> SubRole.NEW_TALENT
            null -> null
        }
        else -> state.subRole
    }
}