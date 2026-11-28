package com.cylonid.nativealpha.util

import android.content.Context
import android.content.res.Configuration
import com.cylonid.nativealpha.model.WebApp
import java.util.Calendar

/**
 * Resolves system night-mode state and WebView forced dark mode policy.
 */
object DarkModeUtils {
    /**
     * Returns whether the Android system theme is currently in night mode, reading from the
     * application context so Activity-level `setLocalNightMode` overrides cannot mask system changes.
     */
    @JvmStatic
    fun isSystemDarkMode(context: Context): Boolean {
        val appContext = context.applicationContext ?: context
        val uiMode = appContext.resources.configuration.uiMode
        return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * Returns whether forced WebView darkening should be applied for [webapp] given [isSystemDark] and [now].
     */
    @JvmStatic
    @JvmOverloads
    fun shouldForceDarkMode(
        webapp: WebApp,
        isSystemDark: Boolean,
        now: Calendar = Calendar.getInstance(),
    ): Boolean {
        if (!webapp.isForceDarkMode) {
            return false
        }
        if (webapp.isForceDarkOnlyWhenSystemDark && !isSystemDark) {
            return false
        }
        if (webapp.isUseTimespanDarkMode) {
            val beginCal = DateUtils.convertStringToCalendar(webapp.timespanDarkModeBegin)
            val endCal = DateUtils.convertStringToCalendar(webapp.timespanDarkModeEnd)
            return beginCal != null && endCal != null && DateUtils.isInInterval(beginCal, now, endCal)
        }
        return true
    }
}
