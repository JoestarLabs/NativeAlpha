package com.cylonid.nativealpha.ui.about

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.ui.theme.NativeAlphaTheme
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AboutScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testCopyDebugInfoCopiesDiagnosticTextAndShowsSnackbar() {
        composeTestRule.setContent {
            NativeAlphaTheme {
                AboutScreen(onNavigateBack = {})
            }
        }

        val copyTitle = context.getString(R.string.about_debug_info_title)
        val copiedMessage = context.getString(R.string.about_debug_info_copied)

        val copyNode = composeTestRule.onNodeWithText(copyTitle)
        copyNode.performScrollTo()
        copyNode.assertIsDisplayed()
        copyNode.performClick()

        val clipboard = context.getSystemService(ClipboardManager::class.java)!!
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            clipboard.primaryClip != null
        }

        val clipText =
            clipboard.primaryClip!!
                .getItemAt(0)
                .text
                .toString()
        assertNotNull(clipText)
        assertTrue(clipText.contains("App: Native Alpha"))
        assertTrue(clipText.contains("Android:"))
        assertTrue(clipText.contains("Device:"))
        assertTrue(clipText.contains("WebApps:"))

        composeTestRule.onNodeWithText(copiedMessage).assertIsDisplayed()
    }
}
