package com.cylonid.nativealpha.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.model.WebApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DebugInfoFormatterTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        DataManager.getInstance().websites.clear()
    }

    @Test
    fun testFormatIncludesAllPopulatedFields() {
        val info =
            DebugInfo(
                appVersionName = "1.6.7",
                appVersionCode = 42,
                buildFlavor = "standard",
                buildType = "release",
                androidRelease = "15",
                sdkInt = 35,
                securityPatch = "2026-11-05",
                manufacturer = "Google",
                brand = "google",
                model = "Pixel 8 Pro",
                device = "husky",
                buildDisplay = "AP3A.241105.007",
                romVersion = "One UI 7.0",
                webViewPackage = "com.google.android.webview",
                webViewVersion = "131.0.6778.104",
                appLocale = "en-US",
                themeMode = "System",
                dynamicColor = true,
                ignoringBatteryOptimizations = true,
                activeWebAppCount = 5,
                sandboxedWebAppCount = 2,
                globalAdblockEnabled = true,
                globalBiometricEnabled = false,
            )

        val formatted = DebugInfoFormatter.format(info)
        assertTrue(formatted.contains("App: Native Alpha 1.6.7 (42, standard/release)"))
        assertTrue(formatted.contains("Android: 15 (API 35)"))
        assertTrue(formatted.contains("Security Patch: 2026-11-05"))
        assertTrue(formatted.contains("Device: Google Pixel 8 Pro (google/husky)"))
        assertTrue(formatted.contains("ROM: One UI 7.0"))
        assertTrue(formatted.contains("Build: AP3A.241105.007"))
        assertTrue(formatted.contains("WebView: com.google.android.webview (131.0.6778.104)"))
        assertTrue(formatted.contains("Locale: en-US | Theme: System (DynamicColor=true)"))
        assertTrue(formatted.contains("Battery Optimization Ignored: true"))
        assertTrue(formatted.contains("WebApps: 5 active (2 sandboxed) | Global Adblock=true, Biometric=false"))
    }

    @Test
    fun testFormatOmitsOptionalBlankOrNullFields() {
        val info =
            DebugInfo(
                appVersionName = "1.6.7",
                appVersionCode = 42,
                buildFlavor = "standard",
                buildType = "debug",
                androidRelease = "14",
                sdkInt = 34,
                securityPatch = "",
                manufacturer = "Google",
                brand = "google",
                model = "Pixel",
                device = "generic",
                buildDisplay = "",
                romVersion = null,
                webViewPackage = null,
                webViewVersion = null,
                appLocale = "en",
                themeMode = "Dark",
                dynamicColor = false,
                ignoringBatteryOptimizations = null,
                activeWebAppCount = 0,
                sandboxedWebAppCount = 0,
                globalAdblockEnabled = false,
                globalBiometricEnabled = false,
            )

        val formatted = DebugInfoFormatter.format(info)
        assertFalse(formatted.contains("Security Patch:"))
        assertFalse(formatted.contains("ROM:"))
        assertFalse(formatted.contains("Build:"))
        assertFalse(formatted.contains("WebView:"))
        assertFalse(formatted.contains("Battery Optimization Ignored:"))
        assertTrue(formatted.contains("Locale: en | Theme: Dark (DynamicColor=false)"))
    }

    @Test
    fun testDetectRomVersionSupportsMajorOemSkins() {
        assertEquals(
            "One UI 8.0",
            DebugInfoFormatter.detectRomVersion("Samsung") { key ->
                if (key == "ro.build.version.oneui") "80000" else null
            },
        )
        assertEquals(
            "One UI (S918BXXU)",
            DebugInfoFormatter.detectRomVersion("samsung") { key ->
                if (key == "ro.build.PDA") "S918BXXU" else null
            },
        )
        assertEquals(
            "HyperOS 2.0",
            DebugInfoFormatter.detectRomVersion("Xiaomi") { key ->
                if (key == "ro.mi.os.version.name") "2.0" else null
            },
        )
        assertEquals(
            "MIUI V14",
            DebugInfoFormatter.detectRomVersion("Xiaomi") { key ->
                if (key == "ro.miui.ui.version.name") "V14" else null
            },
        )
        assertEquals(
            "OxygenOS 15.0",
            DebugInfoFormatter.detectRomVersion("OnePlus") { key ->
                if (key == "ro.build.version.oplusrom") "15.0" else null
            },
        )
        assertEquals(
            "ColorOS 15.0",
            DebugInfoFormatter.detectRomVersion("OPPO") { key ->
                if (key == "ro.build.version.oplusrom") "15.0" else null
            },
        )
        assertEquals(
            "Nothing OS 3.0",
            DebugInfoFormatter.detectRomVersion("Nothing") { key ->
                if (key == "ro.nothing.version.id") "3.0" else null
            },
        )
        assertEquals(
            "LineageOS 22.1",
            DebugInfoFormatter.detectRomVersion("Google") { key ->
                if (key == "ro.lineage.version") "22.1" else null
            },
        )
        assertNull(DebugInfoFormatter.detectRomVersion("Google") { null })
    }

    @Test
    fun testFormatOneUiVersion() {
        assertNull(DebugInfoFormatter.formatOneUiVersion(null))
        assertNull(DebugInfoFormatter.formatOneUiVersion("abc"))
        assertNull(DebugInfoFormatter.formatOneUiVersion("9999"))
        assertEquals("One UI 8.0", DebugInfoFormatter.formatOneUiVersion("80000"))
        assertEquals("One UI 6.1.1", DebugInfoFormatter.formatOneUiVersion("60101"))
    }

    @Test
    fun testCollectAggregatesWebAppCountsWithoutLeakingPii() {
        val dm = DataManager.getInstance()
        dm.settings.isDynamicColor = true
        dm.settings.themeId = 1
        dm.settings.globalWebApp.isUseAdblock = true
        dm.settings.globalWebApp.isBiometricProtection = true

        dm.websites.add(
            WebApp("https://secret-amazon.example", 0, 0).apply {
                title = "SecretAmazon"
                isUseContainer = true
                containerId = 0
                isActiveEntry = true
            },
        )
        dm.websites.add(
            WebApp("https://github.example", 1, 1).apply {
                title = "PrivateGitHub"
                isUseContainer = false
                isActiveEntry = true
            },
        )
        dm.websites.add(
            WebApp("https://deleted.example", 2, 2).apply {
                isUseContainer = true
                isActiveEntry = false
            },
        )

        val info = DebugInfoFormatter.collect(context) { null }
        val formatted = DebugInfoFormatter.format(info)

        assertEquals(2, info.activeWebAppCount)
        assertEquals(1, info.sandboxedWebAppCount)
        assertEquals("Dark", info.themeMode)
        assertTrue(info.dynamicColor)
        assertTrue(info.globalAdblockEnabled)
        assertTrue(info.globalBiometricEnabled)
        assertFalse(formatted.contains("secret-amazon.example"))
        assertFalse(formatted.contains("SecretAmazon"))
        assertFalse(formatted.contains("PrivateGitHub"))
    }
}
