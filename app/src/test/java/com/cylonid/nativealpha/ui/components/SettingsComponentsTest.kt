package com.cylonid.nativealpha.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SettingsComponentsTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSettingsSectionHeaderRendersCorrectly() {
        composeTestRule.setContent {
            SettingsSectionHeader(title = "General Settings")
        }

        composeTestRule
            .onNodeWithText("General Settings")
            .assertIsDisplayed()
    }

    @Test
    fun testSettingsActionItemClickInvokesCallback() {
        var clicked = false

        composeTestRule.setContent {
            SettingsActionItem(
                title = "Adblock Config",
                description = "Configure ad-blocking lists",
                icon = Icons.Rounded.Settings,
                onClick = { clicked = true },
            )
        }

        composeTestRule
            .onNodeWithText("Adblock Config")
            .assertIsDisplayed()
            .performClick()

        assertTrue("Action item click callback should have been called", clicked)
    }

    @Test
    fun testSettingsSwitchItemTogglesValue() {
        var toggleValue = false

        composeTestRule.setContent {
            var checked by remember { mutableStateOf(false) }
            SettingsSwitchItem(
                title = "Clear Cache",
                description = "Clear cache after usage",
                checked = checked,
                onCheckedChange = {
                    checked = it
                    toggleValue = it
                },
            )
        }

        val node = composeTestRule.onNodeWithText("Clear Cache")
        node.assertIsDisplayed()
        node.assertIsOff()

        node.performClick()
        node.assertIsOn()
        assertTrue("Switch state should have toggled to true", toggleValue)
    }

    @Test
    fun testRoundedCardContainerRendersChildren() {
        composeTestRule.setContent {
            RoundedCardContainer {
                Text(text = "Child 1")
                Text(text = "Child 2")
            }
        }

        composeTestRule.onNodeWithText("Child 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Child 2").assertIsDisplayed()
    }
}
