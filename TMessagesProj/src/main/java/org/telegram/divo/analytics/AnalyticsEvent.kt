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
    class OnboardingSkipped : AnalyticsEvent("onboarding_skipped")
    class OnboardingCompleted : AnalyticsEvent("onboarding_completed")
    class TermsAccepted : AnalyticsEvent("terms_accepted")
    class TermsDeclined : AnalyticsEvent("terms_declined")
    class TermsLinkClicked : AnalyticsEvent("terms_link_clicked")
    class PrivacyPolicyLinkClicked : AnalyticsEvent("privacy_policy_link_clicked")
    class SignUpStart(method: String) : AnalyticsEvent("sign_up_start") {
        init { parameters["method"] = method }
    }
    
    class SignUpIntentSelected(intent: String) : AnalyticsEvent("sign_up_intent_selected") {
        init { parameters["intent"] = intent }
    }
    
    class SignUpRoleSelected(role: String) : AnalyticsEvent("sign_up_role_selected") {
        init { parameters["role"] = role }
    }
    
    class SignUpStepCompleted(stepName: String, stepNumber: Int, totalSteps: Int) : AnalyticsEvent("sign_up_step_completed") {
        init { 
            parameters["step_name"] = stepName 
            parameters["step_number"] = stepNumber
            parameters["total_steps"] = totalSteps
        }
    }

    class SignUpError(error: String) : AnalyticsEvent("sign_up_error") {
        init { parameters["error_message"] = error }
    }
    
    class SignUpComplete : AnalyticsEvent("sign_up_complete")
    class SignInComplete(method: String) : AnalyticsEvent("sign_in_complete") {
        init { parameters["method"] = method }
    }
    class SignInStart(method: String) : AnalyticsEvent("sign_in_start") {
        init { parameters["method"] = method }
    }
    class AuthMethodSelected(method: String) : AnalyticsEvent("auth_method_selected") {
        init { parameters["method"] = method }
    }


    // ==========================================
    // 2. Profile & Portfolio
    // ==========================================
    class ProfileTabViewed(tabName: String) : AnalyticsEvent("profile_tab_viewed") {
        init { parameters["tab_name"] = tabName }
    }
    
    class ProfileInfoTabViewed(tabName: String) : AnalyticsEvent("profile_info_tab_viewed") {
        init { parameters["tab_name"] = tabName }
    }
    
    class ProfileOpened(targetUserId: Long, source: String? = null) : AnalyticsEvent("profile_opened") {
        init { 
            parameters["target_user_id"] = targetUserId 
            if (source != null) {
                parameters["source"] = source
            }
        }
    }
    
    class ProfileEditSaved : AnalyticsEvent("profile_edit_saved")
    class ParametersUpdated : AnalyticsEvent("parameters_updated")
    
    class ProfileEditOpened : AnalyticsEvent("profile_edit_opened")
    class ProfileAvatarUploaded : AnalyticsEvent("profile_avatar_uploaded")
    class ProfileBackgroundChanged : AnalyticsEvent("profile_background_changed")
    class SocialLinksOpened : AnalyticsEvent("social_links_opened")
    class SocialLinksSaved : AnalyticsEvent("social_links_saved")
    class ProfileEditMenuTapped(targetUserId: Int) : AnalyticsEvent("profile_edit_menu_tapped") {
        init { parameters["target_user_id"] = targetUserId }
    }
    
    class ProfileEditOptionTapped(targetUserId: Int, option: String) : AnalyticsEvent("profile_edit_option_tapped") {
        init {
            parameters["target_user_id"] = targetUserId
            parameters["option"] = option
        }
    }
    


    class WorkHistoryCreated : AnalyticsEvent("work_history_created")
    class WorkHistoryEdited : AnalyticsEvent("work_history_edited")
    class WorkHistoryDeleted : AnalyticsEvent("work_history_deleted")
    class WorkHistoryOpened(targetUserId: Int) : AnalyticsEvent("work_history_opened") {
        init { parameters["target_user_id"] = targetUserId }
    }
    class WorkHistoryCreateOpened : AnalyticsEvent("work_history_create_opened")
    class WorkHistoryEditOpened(workId: Int) : AnalyticsEvent("work_history_edit_opened") {
        init { parameters["work_id"] = workId }
    }


    // ==========================================
    // 3. Agency Functionality
    // ==========================================
    class AddModelStarted(targetUserId: Int) : AnalyticsEvent("add_model_started") {
        init { parameters["target_user_id"] = targetUserId }
    }
    class AddModelSuccess(targetUserId: Int, agencyId: Int) : AnalyticsEvent("add_model_success") {
        init { 
            parameters["target_user_id"] = targetUserId
            parameters["agency_id"] = agencyId
        }
    }
    class RemoveModelSuccess(targetUserId: Int, agencyId: Int) : AnalyticsEvent("remove_model_success") {
        init { 
            parameters["target_user_id"] = targetUserId
            parameters["agency_id"] = agencyId
        }
    }


    // ==========================================
    // 4. Search (Text & Face Search)
    class FaceRecognitionOpened(source: String, targetUserId: Int? = null) : AnalyticsEvent("face_recognition_opened") {
        init {
            parameters["source"] = source
            targetUserId?.let { parameters["target_user_id"] = it }
        }
    }
    class FaceSearchStarted : AnalyticsEvent("face_search_started")
    
    class FaceSearchSuccess(matchesCount: Int) : AnalyticsEvent("face_search_success") {
        init { parameters["matches_count"] = matchesCount }
    }
    
    class FaceSearchFindTapped : AnalyticsEvent("face_search_find_tapped")
    
    class FaceSearchHistoryOpened : AnalyticsEvent("face_search_history_opened")
    class FaceSearchHistoryCleared : AnalyticsEvent("face_search_history_cleared")
    
    class SimilarProfilesScreenOpened(hasResults: Boolean) : AnalyticsEvent("similar_profiles_screen_opened") {
        init { parameters["has_results"] = hasResults }
    }
    
    class SimilarProfilesFiltersOpened : AnalyticsEvent("similar_profiles_filters_opened")
    
    class SimilarProfilesFiltersApplied(activeFilters: String) : AnalyticsEvent("similar_profiles_filters_applied") {
        init { parameters["active_filters"] = activeFilters }
    }
    
    class SimilarProfileOpened : AnalyticsEvent("similar_profile_opened")
    
    class SearchPerformed(target: String, hasResults: Boolean, query: String = "", activeFilters: String = "") : AnalyticsEvent("search_performed") {
        init {
            parameters["target"] = target // "models", "agencies", "events"
            parameters["has_results"] = hasResults
            parameters["query"] = query
            parameters["active_filters"] = activeFilters
        }
    }
    
    class SearchFiltersOpened(target: String) : AnalyticsEvent("search_filters_opened") {
        init { parameters["target"] = target }
    }
    
    class SearchFiltersApplied(target: String, activeFilters: String = "") : AnalyticsEvent("search_filters_applied") {
        init { 
            parameters["target"] = target
            parameters["active_filters"] = activeFilters 
        }
    }

    class ProfileMediaUploadTapped(mediaType: String, targetUserId: Int) : AnalyticsEvent("profile_media_upload_tapped") {
        init { 
            parameters["media_type"] = mediaType
            parameters["target_user_id"] = targetUserId 
        }
    }
    
    class ProfileMediaUploaded(mediaType: String, targetUserId: Int) : AnalyticsEvent("profile_media_uploaded") {
        init { 
            parameters["media_type"] = mediaType
            parameters["target_user_id"] = targetUserId 
        }
    }
    class ChannelCreateStarted(targetUserId: Int) : AnalyticsEvent("channel_create_started") {
        init { parameters["target_user_id"] = targetUserId }
    }
    class ChannelCreateSuccess(targetUserId: Int, channelId: Long) : AnalyticsEvent("channel_create_success") {
        init { 
            parameters["target_user_id"] = targetUserId
            parameters["channel_id"] = channelId
        }
    }
    class ChannelDeleteSuccess(targetUserId: Int, channelId: Long) : AnalyticsEvent("channel_delete_success") {
        init { 
            parameters["target_user_id"] = targetUserId
            parameters["channel_id"] = channelId
        }
    }


    // ==========================================
    // 5. Events
    // ==========================================
    class EventListOpened : AnalyticsEvent("event_list_opened")
    class EventCreateStarted(targetUserId: Int) : AnalyticsEvent("event_create_started") {
        init { parameters["target_user_id"] = targetUserId }
    }
    class EventCreateSuccess(eventId: Long) : AnalyticsEvent("event_create_success") {
        init { parameters["event_id"] = eventId }
    }
    class EventCreatePreviewOpened : AnalyticsEvent("event_create_preview_opened")
    class EventEdited(eventId: Long) : AnalyticsEvent("event_edited") {
        init { parameters["event_id"] = eventId }
    }
    class EventEditStarted(eventId: Long) : AnalyticsEvent("event_edit_started") {
        init { parameters["event_id"] = eventId }
    }
    class EventCancelled(eventId: Long) : AnalyticsEvent("event_cancelled") {
        init { parameters["event_id"] = eventId }
    }
    class EventApplicationsClosed(eventId: Long) : AnalyticsEvent("event_applications_closed") {
        init { parameters["event_id"] = eventId }
    }
    class EventDetailsMenuOpened(eventId: Long) : AnalyticsEvent("event_details_menu_opened") {
        init { parameters["event_id"] = eventId }
    }
    class EventParametersOpened(eventId: Long) : AnalyticsEvent("event_parameters_opened") {
        init { parameters["event_id"] = eventId }
    }
    
    // ==========================================
    // Events added in Blocks 3-7
    // ==========================================
    class SearchQueryEntered(target: String, queryLength: Int) : AnalyticsEvent("search_query_entered") {
        init { 
            parameters["target"] = target
            parameters["query_length"] = queryLength 
        }
    }
    class SearchModelTapped(targetUserId: Int, screenName: String? = null) : AnalyticsEvent("search_model_tapped", screenName) {
        init { parameters["target_user_id"] = targetUserId }
    }
    
    class EventWithdraw(eventId: Long, userId: Long) : AnalyticsEvent("event_withdraw") {
        init {
            parameters["event_id"] = eventId
            parameters["user_id"] = userId
        }
    }
    
    class LikeToggled(targetUserId: Long, isLiked: Boolean, screenName: String? = null) : AnalyticsEvent("like_toggled", screenName) {
        init {
            parameters["target_user_id"] = targetUserId
            parameters["is_liked"] = isLiked
        }
    }
    class BookmarkToggled(targetUserId: Long, isBookmarked: Boolean, screenName: String? = null) : AnalyticsEvent("bookmark_toggled", screenName) {
        init {
            parameters["target_user_id"] = targetUserId
            parameters["is_bookmarked"] = isBookmarked
        }
    }
    class SettingsOptionTapped(option: String) : AnalyticsEvent("settings_option_tapped") {
        init { parameters["option"] = option }
    }
    class EngagementTabViewed(tabName: String, targetUserId: Int) : AnalyticsEvent("engagement_tab_viewed") {
        init { 
            parameters["tab_name"] = tabName 
            parameters["target_user_id"] = targetUserId
        }
    }
    
    class EngagementSearchPerformed(tabName: String, targetUserId: Int, query: String, hasResults: Boolean) : AnalyticsEvent("engagement_search_performed") {
        init { 
            parameters["tab_name"] = tabName 
            parameters["target_user_id"] = targetUserId
            parameters["query"] = query
            parameters["has_results"] = hasResults
        }
    }
    
    class EventDeleted(eventId: Long) : AnalyticsEvent("event_deleted") {
        init { parameters["event_id"] = eventId }
    }
    
    class EventDetailsViewed(eventId: Long, screenName: String? = null) : AnalyticsEvent("event_details_viewed", screenName) {
        init { parameters["event_id"] = eventId }
    }

    class EventFavoriteToggled(eventId: Long, isFavorite: Boolean, screenName: String? = null) : AnalyticsEvent("event_favorite_toggled", screenName) {
        init {
            parameters["event_id"] = eventId
            parameters["is_favorite"] = isFavorite
        }
    }

    class EventApplyStarted(eventId: Long, userId: Long) : AnalyticsEvent("event_apply_started") {
        init { 
            parameters["event_id"] = eventId
            parameters["user_id"] = userId
        }
    }
    
    class EventApplyConfirmed(eventId: Long, userId: Long) : AnalyticsEvent("event_apply_confirmed") {
        init { 
            parameters["event_id"] = eventId
            parameters["user_id"] = userId
        }
    }


    // ==========================================
    // 6. Social Interactions
    // ==========================================
    class ModelsFeedOpened : AnalyticsEvent("models_feed_opened")
    class ModelsSearchOpened : AnalyticsEvent("models_search_opened")
    class ChatsOpened : AnalyticsEvent("chats_opened")
    class ChatViewed(targetUserId: Long, chatId: Long) : AnalyticsEvent("chat_viewed") {
        init {
            if (targetUserId != 0L) parameters["target_user_id"] = targetUserId
            if (chatId != 0L) parameters["chat_id"] = chatId
        }
    }
    class SettingsOpened : AnalyticsEvent("settings_opened")
    class ModelsTabViewed(tabName: String) : AnalyticsEvent("models_tab_viewed") {
        init { parameters["tab_name"] = tabName }
    }
    class GalleryItemViewed(
        targetUserId: Int,
        mediaId: Int,
        mediaType: String,
        screenName: String
    ) : AnalyticsEvent("gallery_item_viewed") {
        init {
            parameters["target_user_id"] = targetUserId
            parameters["media_id"] = mediaId
            parameters["media_type"] = mediaType
            parameters["screen_name"] = screenName
        }
    }
    
    class StoryAddClicked(screenName: String) : AnalyticsEvent("story_add_clicked", screenName)
    class StoryOpened(source: String) : AnalyticsEvent("story_opened") {
        init { parameters["source"] = source }
    }
    class StoryUploaded(isVideo: Boolean) : AnalyticsEvent("story_uploaded") {
        init { parameters["is_video"] = isVideo }
    }

    class DirectMessageStarted(sourceUserId: Int, targetUserId: Long) : AnalyticsEvent("direct_message_started") {
        init { 
            parameters["source_user_id"] = sourceUserId
            parameters["target_user_id"] = targetUserId 
        }
    }
    
    class AttachmentMenuOpened(targetUserId: Long) : AnalyticsEvent("attachment_menu_opened") {
        init { parameters["target_user_id"] = targetUserId }
    }
    
    class CallStarted(targetUserId: Long, isVideo: Boolean) : AnalyticsEvent("call_started") {
        init { 
            parameters["target_user_id"] = targetUserId 
            parameters["is_video"] = isVideo
        }
    }

    class MessageSent(targetUserId: Long, chatId: Long, messageType: String) : AnalyticsEvent("message_sent") {
        init { 
            if (targetUserId != 0L) parameters["target_user_id"] = targetUserId 
            if (chatId != 0L) parameters["chat_id"] = chatId
            parameters["message_type"] = messageType
        }
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
