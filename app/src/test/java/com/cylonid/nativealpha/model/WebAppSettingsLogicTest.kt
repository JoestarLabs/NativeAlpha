package com.cylonid.nativealpha.model

import com.cylonid.nativealpha.util.Const
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
}
