package org.telegram.divo.screen.reg_select_role

import androidx.annotation.StringRes
import org.telegram.divo.common.arch.ViewEffect
import org.telegram.divo.common.arch.ViewIntent
import org.telegram.divo.common.arch.ViewState
import org.telegram.messenger.R

data class RoleSelectionState(
    val intent: UserIntent? = null,         // главный экран — 3 двери
    val hiringType: HiringType? = null,     // Q1 для LOOKING_FOR_TALENT
    val subRole: SubRole? = null,           // финальный выбор роли
    val hasModelingExperience: Boolean? = null, // Q2 только для Model

    val resetPagerToFirstPage: Boolean = false,
) : ViewState

sealed class RoleSelectionIntent : ViewIntent {
    data class OnUserIntentSelected(val intent: UserIntent) : RoleSelectionIntent()
    data class OnHiringTypeSelected(val hiringType: HiringType) : RoleSelectionIntent()
    data class OnSubRoleSelected(val subRole: SubRole) : RoleSelectionIntent()
    data class OnModelingExperienceSelected(val hasExperience: Boolean) : RoleSelectionIntent()
    data object OnContinue : RoleSelectionIntent()
    data object OnBack : RoleSelectionIntent()
    data object OnChangeDifferentRole : RoleSelectionIntent()
    data object OnFirstPageReached : RoleSelectionIntent()
}

sealed class RoleSelectionEffect : ViewEffect {
    data class ShowError(val message: String) : RoleSelectionEffect()
    data class NavigateToQuiz(val intent: UserIntent) : RoleSelectionEffect()
    data class NavigateToResult(val subRole: SubRole) : RoleSelectionEffect()
    data object NavigateBack : RoleSelectionEffect()
}

data class QuizOption(
    val iconRes: Int = -1,
    val subRole: SubRole,
    @StringRes val descriptionRes: Int = -1
)

// Дверь на главном экране
enum class UserIntent {
    FAN,
    GET_HIRED,
    LOOKING_FOR_TALENT
}

// Промежуточный вопрос только внутри LOOKING_FOR_TALENT
enum class HiringType {
    COMPANY,
    INDIVIDUAL
}

enum class SubRole(@StringRes val labelRes: Int) {
    // Companies (A)
    MODELING_AGENCY(R.string.SubRoleModelingAgency),
    FASHION_BRAND(R.string.SubRoleFashionBrand),
    BEAUTY_BRAND(R.string.SubRoleBeautyBrand),
    BRAND_OR_BUSINESS(R.string.SubRoleBrandOrBusiness),
    EVENT_AGENCY(R.string.SubRoleEventAgency),
    MAGAZINE(R.string.SubRoleMagazine),
    // Industry Pros (B)
    SCOUT(R.string.SubRoleScout),
    BOOKER(R.string.SubRoleBooker),
    CASTING_DIRECTOR(R.string.SubRoleCastingDirector),
    TALENT_MANAGER(R.string.SubRoleTalentManager),
    // Creative (C)
    PHOTOGRAPHER(R.string.SubRolePhotographer),
    STYLIST(R.string.SubRoleStylist),
    MUA(R.string.SubRoleMakeupArtist),
    HAIR_STYLIST(R.string.SubRoleHairStylist),
    VIDEOGRAPHER(R.string.SubRoleVideographer),
    CREATIVE_DIRECTOR(R.string.SubRoleCreativeDirector),
    FASHION_DESIGNER(R.string.SubRoleFashionDesigner),
    STUDIO(R.string.SubRoleStudio),
    // Talent (D)
    MODEL(R.string.SubRoleModel),
    NEW_TALENT(R.string.SubRoleNewTalent),
    ACTOR(R.string.SubRoleActor),
    DANCER(R.string.SubRoleDancer),
    SINGER(R.string.SubRoleSingerOrPerformer),
    // Audience (E)
    FAN(R.string.SubRoleFan),
}
