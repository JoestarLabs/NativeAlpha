package com.cylonid.nativealpha.util

import android.content.Context
import android.content.res.Configuration
import androidx.test.core.app.ApplicationProvider
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.model.WebApp
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DarkModeUtilsTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        DataManager.getInstance().websites.clear()
    }

    private fun calendarAt(
        hour: Int,
        minute: Int,
    ): Calendar =
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

    @Test
    fun testShouldForceDarkModeRespectsMasterAndSystemDarkFlags() {
        val app = WebApp("https://example.com", 0, 0)
        assertFalse(DarkModeUtils.shouldForceDarkMode(app, isSystemDark = false))
        assertFalse(DarkModeUtils.shouldForceDarkMode(app, isSystemDark = true))

        app.isForceDarkMode = true
        app.isForceDarkOnlyWhenSystemDark = false
        assertTrue(DarkModeUtils.shouldForceDarkMode(app, isSystemDark = false))
        assertTrue(DarkModeUtils.shouldForceDarkMode(app, isSystemDark = true))

        app.isForceDarkOnlyWhenSystemDark = true
        assertFalse(DarkModeUtils.shouldForceDarkMode(app, isSystemDark = false))
        assertTrue(DarkModeUtils.shouldForceDarkMode(app, isSystemDark = true))
    }

    @Test
    fun testShouldForceDarkModeComposesSystemDarkWithTimespan() {
        val app =
            WebApp("https://example.com", 0, 0).apply {
                isForceDarkMode = true
                isForceDarkOnlyWhenSystemDark = true
                isUseTimespanDarkMode = true
                timespanDarkModeBegin = "22:00"
                timespanDarkModeEnd = "06:00"
            }

        val insideTimespan = calendarAt(23, 30)
        val outsideTimespan = calendarAt(14, 0)

        assertTrue(DarkModeUtils.shouldForceDarkMode(app, isSystemDark = true, now = insideTimespan))
        assertFalse(DarkModeUtils.shouldForceDarkMode(app, isSystemDark = false, now = insideTimespan))
        assertFalse(DarkModeUtils.shouldForceDarkMode(app, isSystemDark = true, now = outsideTimespan))
        assertFalse(DarkModeUtils.shouldForceDarkMode(app, isSystemDark = false, now = outsideTimespan))
    }

    @Test
    fun testIsSystemDarkModeReadsApplicationConfiguration() {
        val config = context.applicationContext.resources.configuration
        val originalUiMode = config.uiMode
        try {
            config.uiMode = (originalUiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_YES
            assertTrue(DarkModeUtils.isSystemDarkMode(context))

            config.uiMode = (originalUiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
            assertFalse(DarkModeUtils.isSystemDarkMode(context))
        } finally {
            config.uiMode = originalUiMode
        }
    }

    @Test
    fun testGlobalSettingsInheritanceAndPerAppOverride() {
        val dm = DataManager.getInstance()
        val prevGlobalForce = dm.settings.globalWebApp.isForceDarkMode
        val prevGlobalSystemOnly = dm.settings.globalWebApp.isForceDarkOnlyWhenSystemDark
        try {
            dm.settings.globalWebApp.isForceDarkMode = true
            dm.settings.globalWebApp.isForceDarkOnlyWhenSystemDark = true

            val inheritedApp =
                WebApp("https://inherited.example", 0, 0).apply {
                    isOverrideGlobalSettings = false
                    isForceDarkMode = false
                    isForceDarkOnlyWhenSystemDark = false
                }
            val overriddenApp =
                WebApp("https://override.example", 1, 1).apply {
                    isOverrideGlobalSettings = true
                    isForceDarkMode = true
                    isForceDarkOnlyWhenSystemDark = false
                }
            dm.websites.addAll(listOf(inheritedApp, overriddenApp))

            val resolvedInherited = dm.getWebApp(0)!!
            assertTrue(resolvedInherited.isForceDarkMode)
            assertTrue(resolvedInherited.isForceDarkOnlyWhenSystemDark)

            val resolvedOverridden = dm.getWebApp(1)!!
            assertTrue(resolvedOverridden.isForceDarkMode)
            assertFalse(resolvedOverridden.isForceDarkOnlyWhenSystemDark)
        } finally {
            dm.settings.globalWebApp.isForceDarkMode = prevGlobalForce
            dm.settings.globalWebApp.isForceDarkOnlyWhenSystemDark = prevGlobalSystemOnly
        }
    }
}
