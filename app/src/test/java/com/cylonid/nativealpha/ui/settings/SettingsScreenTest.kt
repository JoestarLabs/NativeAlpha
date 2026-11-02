package com.cylonid.nativealpha.ui.settings

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.ui.theme.NativeAlphaTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val dm = DataManager.getInstance()
        dm.settings.isClearCache = false
        dm.settings.isShowProgressbar = false
    }

    @Test
    fun testSettingsScreenToggleChangesVisuallyAndUpdatesModel() {
        val dm = DataManager.getInstance()

        composeTestRule.setContent {
            NativeAlphaTheme {
                SettingsScreen(
                    onNavigateBack = {},
                )
            }
        }

        val clearCacheStr = context.getString(R.string.clear_cache_after_usage)
        val clearCacheToggle = composeTestRule.onNodeWithText(clearCacheStr)
        clearCacheToggle.performScrollTo()
        clearCacheToggle.assertIsDisplayed()
        clearCacheToggle.assertIsOff()

        // Toggle on
        clearCacheToggle.performClick()
        clearCacheToggle.assertIsOn()
        assertTrue("DataManager settings.isClearCache should be updated to true", dm.settings.isClearCache)

        // Toggle off
        clearCacheToggle.performClick()
        clearCacheToggle.assertIsOff()
        assertFalse("DataManager settings.isClearCache should be updated to false", dm.settings.isClearCache)
    }

    @Test
    fun testMaterialYouToggleUpdatesModelAndTriggersCallback() {
        val dm = DataManager.getInstance()
        dm.settings.isDynamicColor = false

        var callbackInvokedWith: Boolean? = null

        composeTestRule.setContent {
            NativeAlphaTheme {
                SettingsScreen(
                    onNavigateBack = {},
                    onDynamicColorChanged = { callbackInvokedWith = it },
                )
            }
        }

        val dynamicThemeStr = context.getString(R.string.material_you_theme)
        val dynamicThemeToggle = composeTestRule.onNodeWithText(dynamicThemeStr)
        dynamicThemeToggle.assertIsDisplayed()
        dynamicThemeToggle.assertIsOff()

        // Toggle on
        dynamicThemeToggle.performClick()
        dynamicThemeToggle.assertIsOn()
        assertTrue("DataManager settings.isDynamicColor should be updated to true", dm.settings.isDynamicColor)
        assertTrue("Callback should be invoked with true", callbackInvokedWith == true)

        // Toggle off
        dynamicThemeToggle.performClick()
        dynamicThemeToggle.assertIsOff()
        assertFalse("DataManager settings.isDynamicColor should be updated to false", dm.settings.isDynamicColor)
        assertTrue("Callback should be invoked with false", callbackInvokedWith == false)
    }

    @Test
    @Config(sdk = [30])
    fun testMaterialYouDisabledOnOlderAndroid() {
        composeTestRule.setContent {
            NativeAlphaTheme {
                SettingsScreen(
                    onNavigateBack = {},
                )
            }
        }

        val requiresAndroid12Str = context.getString(R.string.material_you_requires_android_12)
        composeTestRule.onNodeWithText(requiresAndroid12Str).assertIsDisplayed()
    }

    @Test
    fun testWebAppMenuStyleSelectionUpdatesModel() {
        val dm = DataManager.getInstance()
        dm.settings.webAppMenuStyle = com.cylonid.nativealpha.ui.webapp.WebAppMenuStyle.BOTTOM_SHEET.id

        composeTestRule.setContent {
            NativeAlphaTheme {
                SettingsScreen(
                    onNavigateBack = {},
                )
            }
        }

        val menuStyleStr = context.getString(R.string.webapp_menu_style)
        val menuStyleItem = composeTestRule.onNodeWithText(menuStyleStr)
        menuStyleItem.performScrollTo()
        menuStyleItem.assertIsDisplayed()

        // Click to open dialog
        menuStyleItem.performClick()

        // Verify summary text in dialog header is displayed
        val menuStyleSummaryStr = context.getString(R.string.webapp_menu_style_summary)
        composeTestRule.onNodeWithText(menuStyleSummaryStr).assertIsDisplayed()

        // Check Floating Dialog option exists and click it
        val floatingDialogStr = context.getString(R.string.webapp_menu_style_floating_dialog)
        val floatingOption = composeTestRule.onNodeWithText(floatingDialogStr)
        floatingOption.assertIsDisplayed()
        floatingOption.performClick()

        org.junit.Assert.assertEquals(
            "DataManager settings.webAppMenuStyle should be updated to FLOATING_DIALOG",
            com.cylonid.nativealpha.ui.webapp.WebAppMenuStyle.FLOATING_DIALOG.id,
            dm.settings.webAppMenuStyle,
        )
    }

    @Test
    fun testBuiltinPdfViewerToggle() {
        val dm = DataManager.getInstance()
        dm.settings.isUseBuiltinPdfViewer = true

        composeTestRule.setContent {
            NativeAlphaTheme {
                SettingsScreen(
                    onNavigateBack = {},
                )
            }
        }

        val pdfViewerStr = context.getString(R.string.pref_pdf_viewer_title)
        val pdfViewerToggle = composeTestRule.onNodeWithText(pdfViewerStr)
        pdfViewerToggle.performScrollTo()
        pdfViewerToggle.assertIsDisplayed()
        pdfViewerToggle.assertIsOn()

        // Toggle off
        pdfViewerToggle.performClick()
        pdfViewerToggle.assertIsOff()
        assertFalse(dm.settings.isUseBuiltinPdfViewer)
        assertFalse(dm.settings.globalWebApp.isUseBuiltinPdfViewer)

        // Toggle on
        pdfViewerToggle.performClick()
        pdfViewerToggle.assertIsOn()
        assertTrue(dm.settings.isUseBuiltinPdfViewer)
        assertTrue(dm.settings.globalWebApp.isUseBuiltinPdfViewer)
    }
}
