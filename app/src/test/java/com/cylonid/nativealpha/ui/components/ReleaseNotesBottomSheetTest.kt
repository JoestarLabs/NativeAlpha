package com.cylonid.nativealpha.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.cylonid.nativealpha.model.ReleaseNotes
import com.cylonid.nativealpha.model.ReleaseNotesSection
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ReleaseNotesBottomSheetTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testExpressiveSquigglyDividerRenders() {
        composeTestRule.setContent {
            ExpressiveSquigglyDivider()
        }
        // Verify composition succeeds without exceptions
    }

    @Test
    fun testReleaseNotesBottomSheetDisplaysContentAndHandlesDismiss() {
        var dismissed = false
        val testNotes =
            ReleaseNotes(
                versionName = "1.6.4",
                releaseUrl = "https://github.com/JoestarLabs/NativeAlpha/releases/tag/v1.6.4",
                sections =
                    listOf(
                        ReleaseNotesSection(
                            title = "Features",
                            items = listOf("Awesome dynamic release notes"),
                        ),
                    ),
            )

        composeTestRule.setContent {
            ReleaseNotesBottomSheet(
                releaseNotes = testNotes,
                onDismiss = { dismissed = true },
            )
        }

        composeTestRule
            .onNodeWithText("Awesome dynamic release notes")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("GitHub")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Got it")
            .assertIsDisplayed()
            .performClick()

        assertTrue(dismissed)
    }
}
