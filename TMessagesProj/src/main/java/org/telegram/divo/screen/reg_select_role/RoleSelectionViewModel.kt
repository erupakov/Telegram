package org.telegram.divo.screen.reg_select_role

import org.telegram.divo.common.arch.BaseViewModel

class RoleSelectionViewModel : BaseViewModel<RoleSelectionState, RoleSelectionIntent, RoleSelectionEffect>() {

    override fun createInitialState(): RoleSelectionState {
        return RoleSelectionState()
    }

    override fun handleIntent(intent: RoleSelectionIntent) {
        when (intent) {
            is RoleSelectionIntent.OnUserIntentSelected -> onUserIntentSelected(intent.intent)
            is RoleSelectionIntent.OnHiringTypeSelected -> onHiringTypeSelected(intent.hiringType)
            is RoleSelectionIntent.OnSubRoleSelected -> onSubRoleSelected(intent.subRole)
            is RoleSelectionIntent.OnModelingExperienceSelected -> onExperienceSelected(intent.hasExperience)
            is RoleSelectionIntent.OnContinue -> onContinue()
            is RoleSelectionIntent.OnBack -> onBack()
            RoleSelectionIntent.OnChangeDifferentRole -> {
                resetState()
                onBack()
            }
            RoleSelectionIntent.OnFirstPageReached -> { setState { copy(resetPagerToFirstPage = false) } }
        }
    }

    // Главный экран — выбор одной из 3 дверей
    private fun onUserIntentSelected(userIntent: UserIntent) {
        setState { copy(intent = userIntent) }
    }

    // Q1 для LOOKING_FOR_TALENT — Company или Individual
    private fun onHiringTypeSelected(hiringType: HiringType) {
        setState { copy(hiringType = hiringType, subRole = null) }
    }

    // Выбор саб-роли — сбрасываем опыт если сменили роль
    private fun onSubRoleSelected(subRole: SubRole) {
        setState {
            copy(
                subRole = subRole,
                hasModelingExperience = if (subRole != SubRole.MODEL) null
                else hasModelingExperience
            )
        }
    }

    // Q2 только для Model
    private fun onExperienceSelected(hasExperience: Boolean) {
        val subRole = if (hasExperience) SubRole.MODEL else SubRole.NEW_TALENT
        setState { copy(subRole = subRole, hasModelingExperience = hasExperience) }
    }

    private fun onContinue() {
        val finalRole = resolveFinalSubRole(state.value) ?: run {
            sendEffect(RoleSelectionEffect.ShowError("Please select a role"))
            return
        }
        org.telegram.divo.analytics.DivoAnalytics.logEvent(org.telegram.divo.analytics.AnalyticsEvent.SignUpRoleSelected(finalRole.name))
        sendEffect(RoleSelectionEffect.NavigateToResult(finalRole))
    }

    private fun onBack() {
        sendEffect(RoleSelectionEffect.NavigateBack)
    }

    private fun resetState() {
        setState {
            copy(resetPagerToFirstPage = true, subRole = null, hasModelingExperience = null)
        }
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
}