package com.cylonid.nativealpha.ui.settings

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
}
