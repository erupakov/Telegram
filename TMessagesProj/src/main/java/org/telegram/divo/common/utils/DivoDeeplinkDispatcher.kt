package org.telegram.divo.common.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.navigation.NavController
import org.telegram.divo.screen.event_list.EventRoute
import org.telegram.divo.screen.models.ModelsRoute
import org.telegram.messenger.ApplicationLoader
import org.telegram.messenger.UserConfig
import org.telegram.ui.ActionBar.INavigationLayout
import java.lang.ref.WeakReference

object DivoDeeplinkDispatcher {

    private const val PREFS_NAME = "divo_pending_deeplinks"
    private const val PENDING_URL_KEY = "pending_url"

    val prefs: SharedPreferences? by lazy {
        ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var pendingEventId by mutableStateOf<Int?>(null)
    var pendingProfileId by mutableStateOf<Int?>(null)

    private var _eventsNavController: WeakReference<NavController>? = null
    private var _modelsNavController: WeakReference<NavController>? = null

    var eventsNavController: NavController?
        get() = _eventsNavController?.get()
        set(value) { _eventsNavController = value?.let { WeakReference(it) } }

    var modelsNavController: NavController?
        get() = _modelsNavController?.get()
        set(value) { _modelsNavController = value?.let { WeakReference(it) } }

    fun consumePendingEventId(): Int? {
        val id = pendingEventId
        pendingEventId = null
        return id
    }

    fun consumePendingProfileId(): Int? {
        val id = pendingProfileId
        pendingProfileId = null
        return id
    }

    fun processPendingDeeplink(actionBarLayout: INavigationLayout?) {
        val pendingUrl = prefs?.getString(PENDING_URL_KEY, null)
        if (!pendingUrl.isNullOrEmpty()) {
            prefs?.edit {remove(PENDING_URL_KEY)}

            val fakeIntent = Intent(Intent.ACTION_VIEW, pendingUrl.toUri())
            handleIntent(fakeIntent, actionBarLayout)
        }
    }

    @JvmStatic
    fun handleIntent(intent: Intent?, actionBarLayout: INavigationLayout?): Boolean {
        if (intent == null || intent.action != Intent.ACTION_VIEW) return false
        val data: Uri = intent.data ?: return false

        val scheme = data.scheme
        val host = data.host

        if (scheme == "https" && (host == "api.divo.fashion" || host == "api-stage.divo.fashion")) {
            if (!UserConfig.getInstance(UserConfig.selectedAccount).isClientActivated) {
                prefs?.edit { putString(PENDING_URL_KEY, data.toString()) }
                return false
            }

            val path = data.pathSegments.firstOrNull() ?: ""

            when (path) {
                "profile" -> {
                    val profileId = data.pathSegments.getOrNull(1)?.toIntOrNull()
                    if (profileId != null) {
                        val mainTabs = actionBarLayout?.fragmentStack?.lastOrNull { it is org.telegram.ui.MainTabsActivity } as? org.telegram.ui.MainTabsActivity
                        mainTabs?.switchToTabPosition(0)
                        pendingProfileId = profileId
                    }
                }
                "event" -> {
                    val eventId = data.pathSegments.getOrNull(1)?.toIntOrNull()
                    if (eventId != null) {
                        val mainTabs = actionBarLayout?.fragmentStack?.lastOrNull { it is org.telegram.ui.MainTabsActivity } as? org.telegram.ui.MainTabsActivity
                        mainTabs?.switchToTabPosition(1)
                        pendingEventId = eventId
                    }
                }
                "channel" -> {
                    // Todo реализовать в будущем
                    val mainTabs = actionBarLayout?.fragmentStack?.lastOrNull { it is org.telegram.ui.MainTabsActivity } as? org.telegram.ui.MainTabsActivity
                    mainTabs?.switchToTabPosition(2)
                }
                else -> {
                    val mainTabs = actionBarLayout?.fragmentStack?.lastOrNull { it is org.telegram.ui.MainTabsActivity } as? org.telegram.ui.MainTabsActivity
                    mainTabs?.switchToTabPosition(0)
                }
            }
            return true
        }

        return false
    }
}