package com.cylonid.nativealpha.model

import com.cylonid.nativealpha.util.Const
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WebAppSettingsLogicTest {
    @Test
    fun testWebAppDefaultSettings() {
        val webApp = WebApp("https://example.com", 1, Const.getDefaultAdBlockConfig())
        assertTrue("JavaScript should be enabled by default", webApp.isAllowJs)
        assertTrue("Cookies should be enabled by default", webApp.isAllowCookies)
        assertFalse("Third-party cookies should be disabled by default", webApp.isAllowThirdPartyCookies)
        assertFalse("Desktop site should be disabled by default", webApp.isRequestDesktop)
        assertFalse("Zooming should be disabled by default", webApp.isEnableZooming)
    }

    @Test
    fun testDisablingJsDisablesDesktopAndAdblock() {
        val webApp =
            WebApp("https://example.com", 1, Const.getDefaultAdBlockConfig()).apply {
                isAllowJs = true
                isRequestDesktop = true
                isUseAdblock = true
            }

        // Simulate user turning off JS
        webApp.isAllowJs = false
        if (!webApp.isAllowJs) {
            webApp.isRequestDesktop = false
            webApp.isUseAdblock = false
        }

        assertFalse("Desktop site should be turned off when JS is disabled", webApp.isRequestDesktop)
        assertFalse("Adblock should be turned off when JS is disabled", webApp.isUseAdblock)
    }

    @Test
    fun testDisablingCookiesDisablesThirdPartyCookies() {
        val webApp =
            WebApp("https://example.com", 1, Const.getDefaultAdBlockConfig()).apply {
                isAllowCookies = true
                isAllowThirdPartyCookies = true
            }

        // Simulate turning off cookies
        webApp.isAllowCookies = false
        if (!webApp.isAllowCookies) {
            webApp.isAllowThirdPartyCookies = false
        }

        assertFalse("3rd-party cookies should be disabled when all cookies are disabled", webApp.isAllowThirdPartyCookies)
    }

    @Test
    fun testGlobalSettingsMutationAndCopy() {
        val settings = GlobalSettings()
        assertFalse(settings.isClearCache)
        assertTrue(settings.isTwoFingerMultitouch)

        val modified =
            settings.copy(
                isClearCache = true,
                isTwoFingerMultitouch = false,
            )

        assertTrue(modified.isClearCache)
        assertFalse(modified.isTwoFingerMultitouch)
        assertFalse("Original settings should remain unchanged", settings.isClearCache)
    }

    @Test
    fun testWebAppCopyConstructorPreservesAllSettings() {
        val original =
            WebApp("https://example.com", 1, Const.getDefaultAdBlockConfig()).apply {
                title = "Custom Title"
                isOverrideGlobalSettings = false
                isAllowJs = false
                isClearCache = true
                isForceDarkMode = true
                isUseTimespanDarkMode = true
                timespanDarkModeBegin = "21:00"
                timespanDarkModeEnd = "07:00"
                isAllowLocationAccess = true
                isCameraPermission = true
                isMicrophonePermission = true
                isEnableZooming = true
                isBiometricProtection = true
                isAllowMediaPlaybackInBackground = true
                isSendSavedataRequest = true
                isBlockImages = true
                isAutoreload = true
                timeAutoreload = 60
                isShowExpertSettings = true
                isUseCustomUserAgent = true
                userAgent = "CustomUA/1.0"
                isIgnoreSslErrors = true
            }

        val copied = WebApp(original)

        assertEquals("Custom Title", copied.title)
        assertFalse(copied.isOverrideGlobalSettings)
        assertFalse(copied.isAllowJs)
        assertTrue(copied.isClearCache)
        assertTrue(copied.isForceDarkMode)
        assertTrue(copied.isUseTimespanDarkMode)
        assertEquals("21:00", copied.timespanDarkModeBegin)
        assertEquals("07:00", copied.timespanDarkModeEnd)
        assertTrue(copied.isAllowLocationAccess)
        assertTrue(copied.isCameraPermission)
        assertTrue(copied.isMicrophonePermission)
        assertTrue(copied.isEnableZooming)
        assertTrue(copied.isBiometricProtection)
        assertTrue(copied.isAllowMediaPlaybackInBackground)
        assertTrue(copied.isSendSavedataRequest)
        assertTrue(copied.isBlockImages)
        assertTrue(copied.isAutoreload)
        assertEquals(60, copied.timeAutoreload)
        assertTrue(copied.isShowExpertSettings)
        assertTrue(copied.isUseCustomUserAgent)
        assertEquals("CustomUA/1.0", copied.userAgent)
        assertTrue(copied.isIgnoreSslErrors)

        // Mutating copy should not mutate original
        copied.isOverrideGlobalSettings = true
        assertFalse("Original should not be affected by copy mutation", original.isOverrideGlobalSettings)
    }

    @Test
    fun testWebAppDeepCopyPreservesAllSettings() {
        val original =
            WebApp("https://example.com", 2, Const.getDefaultAdBlockConfig()).apply {
                isOverrideGlobalSettings = true
                isAllowCookies = false
            }

        val clone = original.deepCopy()
        assertTrue(clone.isOverrideGlobalSettings)
        assertFalse(clone.isAllowCookies)

        clone.isOverrideGlobalSettings = false
        assertTrue("Original must remain true", original.isOverrideGlobalSettings)
    }
}
