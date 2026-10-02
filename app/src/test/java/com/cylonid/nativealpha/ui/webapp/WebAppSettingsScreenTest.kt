package com.cylonid.nativealpha.ui.webapp

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.model.WebApp
import com.cylonid.nativealpha.ui.theme.NativeAlphaTheme
import com.cylonid.nativealpha.util.Const
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WebAppSettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val testWebAppId = 0
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val dm = DataManager.getInstance()
        val webApp = WebApp("https://example.com", testWebAppId, Const.getDefaultAdBlockConfig())
        webApp.title = "Example App"
        webApp.isOverrideGlobalSettings = true
        webApp.isAllowJs = true
        webApp.isEnableZooming = false
        if (dm.websites.isEmpty()) {
            dm.websites.add(webApp)
        } else {
            dm.websites[0] = webApp
        }
        dm.saveWebAppData()
    }

    @Test
    fun testOverrideGlobalSettingsToggleChangesVisually() {
        composeTestRule.setContent {
            NativeAlphaTheme {
                WebAppSettingsScreen(
                    webappId = testWebAppId,
                    onNavigateBack = {},
                )
            }
        }

        val overrideStr = context.getString(R.string.override_global_settings)
        val jsStr = context.getString(R.string.allow_javascript)

        val overrideToggle = composeTestRule.onNodeWithText(overrideStr).performScrollTo()
        overrideToggle.assertIsDisplayed()
        overrideToggle.assertIsOn()

        // Click to toggle off
        overrideToggle.performClick()
        overrideToggle.assertIsOff()

        // When override global settings is off, per-app settings should be disabled
        val jsToggle = composeTestRule.onNodeWithText(jsStr).performScrollTo()
        jsToggle.assertIsNotEnabled()

        // Click to toggle back on
        overrideToggle.performScrollTo().performClick()
        overrideToggle.assertIsOn()
        jsToggle.performScrollTo().assertIsEnabled()
    }

    @Test
    fun testToggleInWebAppSettingsFlipsState() {
        composeTestRule.setContent {
            NativeAlphaTheme {
                WebAppSettingsScreen(
                    webappId = testWebAppId,
                    onNavigateBack = {},
                )
            }
        }

        val zoomStr = context.getString(R.string.activate_two_finger_zoom)
        val zoomToggle = composeTestRule.onNodeWithText(zoomStr).performScrollTo()
        zoomToggle.assertIsDisplayed()
        zoomToggle.assertIsOff()

        zoomToggle.performClick()
        zoomToggle.assertIsOn()

        zoomToggle.performClick()
        zoomToggle.assertIsOff()
    }
}
