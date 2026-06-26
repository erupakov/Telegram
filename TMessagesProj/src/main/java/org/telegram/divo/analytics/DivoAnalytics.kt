package org.telegram.divo.analytics

import android.os.Bundle
import org.telegram.messenger.ApplicationLoader
import com.google.firebase.analytics.FirebaseAnalytics
import com.facebook.appevents.AppEventsLogger

/**
 * Singleton for logging analytics events.
 * Currently uses a mock logger, but can easily be swapped with FirebaseAnalytics.
 */
object DivoAnalytics {

    private var firebaseAnalytics: FirebaseAnalytics? = null
    private var fbLogger: AppEventsLogger? = null

    fun init() {
        firebaseAnalytics = FirebaseAnalytics.getInstance(ApplicationLoader.applicationContext)
        try {
            fbLogger = AppEventsLogger.newLogger(ApplicationLoader.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Logs an strongly typed analytics event.
     */
    fun logEvent(event: AnalyticsEvent) {
        val bundle = Bundle().apply {
            // Add global parameters here if needed
            
            // Add specific event parameters
            event.parameters.forEach { (key, value) ->
                when (value) {
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Long -> putLong(key, value)
                    is Double -> putDouble(key, value)
                    is Boolean -> putBoolean(key, value)
                }
            }
        }

        firebaseAnalytics?.logEvent(event.eventName, bundle)
        android.util.Log.i("DivoAnalytics", "📊 EVENT SENT: ${event.eventName} | Params: ${event.parameters}")
        
        // Log specific events to Facebook Meta SDK
        val fbEventName = when (event.eventName) {
            "profile_media_uploaded" -> "gallery_media_uploaded" // Map to Meta specs
            else -> event.eventName
        }
        
        if (fbEventName == "sign_up_start" || fbEventName == "sign_up_complete" || fbEventName == "gallery_media_uploaded") {
            fbLogger?.logEvent(fbEventName, bundle)
            android.util.Log.i("DivoAnalytics", "🚀 EVENT SENT TO META: $fbEventName | Params: $bundle")
        }
    }

    /**
     * Sets a global user property.
     * This property will be attached to ALL subsequent events (including automatic session timers).
     * Useful for splitting standard metrics (like session duration) by user role.
     */
    fun setUserProperty(key: String, value: String) {
        println("📊 [DivoAnalytics] Setting User Property: $key = $value")
        if (firebaseAnalytics == null) {
            println("❌ [DivoAnalytics] ERROR: FirebaseAnalytics is NULL! Property will not be set.")
        }
        firebaseAnalytics?.setUserProperty(key, value)
    }
}
