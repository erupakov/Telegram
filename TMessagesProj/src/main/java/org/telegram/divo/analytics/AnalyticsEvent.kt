package org.telegram.divo.analytics

/**
 * Base sealed class for all analytics events to ensure strict typing.
 *
 * @param eventName The exact name of the event as it will appear in Firebase.
 * @param screenName Optional screen name from where the event was triggered (for global filtering).
 */
sealed class AnalyticsEvent(
    val eventName: String,
    val screenName: String? = null
) {
    val parameters: MutableMap<String, Any> = mutableMapOf()

    init {
        screenName?.let { parameters["screen_name"] = it }
    }

    // ==========================================
    // 1. Onboarding & Registration
    // ==========================================
    class OnboardingStarted : AnalyticsEvent("onboarding_started")
    class OnboardingCompleted : AnalyticsEvent("onboarding_completed")
    class SignUpStart(method: String) : AnalyticsEvent("sign_up_start") {
        init { parameters["method"] = method }
    }
    
    class SignUpRoleSelected(role: String) : AnalyticsEvent("sign_up_role_selected") {
        init { parameters["role"] = role }
    }
    
    class SignUpComplete : AnalyticsEvent("sign_up_complete")
    class LoginSuccess : AnalyticsEvent("login_success")


    // ==========================================
    // 2. Profile & Portfolio
    // ==========================================
    class ProfileTabViewed(tabName: String) : AnalyticsEvent("profile_tab_viewed") {
        init { parameters["tab_name"] = tabName }
    }
    
    class ProfileEditSaved : AnalyticsEvent("profile_edit_saved")
    class ParametersUpdated : AnalyticsEvent("parameters_updated")
    
    class GalleryMediaUploaded(mediaType: String) : AnalyticsEvent("gallery_media_uploaded") {
        init { parameters["media_type"] = mediaType } // "photo" or "video"
    }

    class WorkHistoryCreated : AnalyticsEvent("work_history_created")
    class WorkHistoryEdited : AnalyticsEvent("work_history_edited")
    class WorkHistoryDeleted : AnalyticsEvent("work_history_deleted")


    // ==========================================
    // 3. Agency Functionality
    // ==========================================
    class AddModelStarted : AnalyticsEvent("add_model_started")
    class AddModelSuccess : AnalyticsEvent("add_model_success")


    // ==========================================
    // 4. Search (Text & Face Search)
    // ==========================================
    class FaceSearchStarted : AnalyticsEvent("face_search_started")
    
    class FaceSearchSuccess(matchesCount: Int) : AnalyticsEvent("face_search_success") {
        init { parameters["matches_count"] = matchesCount }
    }
    
    class SimilarProfileOpened : AnalyticsEvent("similar_profile_opened")
    
    class SearchPerformed(target: String, hasResults: Boolean) : AnalyticsEvent("search_performed") {
        init {
            parameters["target"] = target // "models", "agencies", "events"
            parameters["has_results"] = hasResults
        }
    }
    
    class SearchFiltersApplied : AnalyticsEvent("search_filters_applied")


    // ==========================================
    // 5. Events
    // ==========================================
    class EventListOpened : AnalyticsEvent("event_list_opened")
    class EventCreateStarted : AnalyticsEvent("event_create_started")
    class EventCreateSuccess : AnalyticsEvent("event_create_success")
    class EventEdited : AnalyticsEvent("event_edited")
    
    // ==========================================
    // Events added in Blocks 3-7
    // ==========================================
    class SearchQueryEntered(queryLength: Int) : AnalyticsEvent("search_query_entered") {
        init { parameters["query_length"] = queryLength }
    }
    class SearchModelTapped : AnalyticsEvent("search_model_tapped")
    
    class EventWithdraw : AnalyticsEvent("event_withdraw")
    
    class LikeToggled : AnalyticsEvent("like_toggled")
    class BookmarkToggled : AnalyticsEvent("bookmark_toggled")
    class StatsTabViewed(tabName: String) : AnalyticsEvent("stats_tab_viewed") {
        init { parameters["tab_name"] = tabName }
    }
    
    class EventDeleted : AnalyticsEvent("event_deleted")
    
    class EventDetailsViewed(eventId: Long, screenName: String? = null) : AnalyticsEvent("event_details_viewed", screenName) {
        init { parameters["event_id"] = eventId }
    }

    class EventFavoriteToggled(eventId: Long, isFavorite: Boolean, screenName: String? = null) : AnalyticsEvent("event_favorite_toggled", screenName) {
        init {
            parameters["event_id"] = eventId
            parameters["is_favorite"] = isFavorite
        }
    }

    class EventApplyStarted(eventId: Long) : AnalyticsEvent("event_apply_started") {
        init { parameters["event_id"] = eventId }
    }
    
    class EventApplyConfirmed(eventId: Long) : AnalyticsEvent("event_apply_confirmed") {
        init { parameters["event_id"] = eventId }
    }


    // ==========================================
    // 6. Social Interactions
    // ==========================================
    class ModelsFeedViewed : AnalyticsEvent("models_feed_viewed")
    class ProfileFollowed(targetUserId: Long) : AnalyticsEvent("profile_followed") {
        init { parameters["target_user_id"] = targetUserId }
    }
    class ProfileUnfollowed(targetUserId: Long) : AnalyticsEvent("profile_unfollowed") {
        init { parameters["target_user_id"] = targetUserId }
    }
    class DirectMessageStarted(targetUserId: Long) : AnalyticsEvent("direct_message_started") {
        init { parameters["target_user_id"] = targetUserId }
    }


    // ==========================================
    // 7. Settings & Security
    // ==========================================
    class AppLanguageChanged(languageCode: String) : AnalyticsEvent("app_language_changed") {
        init { parameters["language_code"] = languageCode }
    }

    class MeasurementSystemChanged(system: String) : AnalyticsEvent("measurement_system_changed") {
        init { parameters["system"] = system } // "metric" or "imperial"
    }

    class UserReported(targetUserId: Long, reason: String) : AnalyticsEvent("user_reported") {
        init { 
            parameters["target_user_id"] = targetUserId
            parameters["reason"] = reason 
        }
    }

    class UserBlocked(targetUserId: Long) : AnalyticsEvent("user_blocked") {
        init { parameters["target_user_id"] = targetUserId }
    }
    
    class UserUnblocked(targetUserId: Long) : AnalyticsEvent("user_unblocked") {
        init { parameters["target_user_id"] = targetUserId }
    }


    class ContentShared(contentType: String, contentId: Long) : AnalyticsEvent("content_shared") {
        init {
            parameters["content_type"] = contentType
            parameters["content_id"] = contentId
        }
    }
}
