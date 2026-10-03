package com.cylonid.nativealpha.ui.webapplist

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.model.WebApp
import com.cylonid.nativealpha.ui.theme.NativeAlphaTheme
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WebAppListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var dataManager: DataManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        dataManager = DataManager.getInstance()
        dataManager.websites.clear()
    }

    @Test
    fun testEmptyStateDisplaysWhenNoWebApps() {
        val viewModel = WebAppListViewModel(dataManager)

        composeTestRule.setContent {
            NativeAlphaTheme {
                WebAppListScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {},
                    onNavigateToAbout = {},
                    onOpenWebApp = {},
                    onOpenWebAppSettings = {},
                    onRequestCreateShortcut = {},
                )
            }
        }

        val emptyTitle = context.getString(R.string.empty_webapps_title)
        val emptySubtitle = context.getString(R.string.empty_webapps_subtitle)
        val addBtnText = context.getString(R.string.add_webapp)

        composeTestRule.onNodeWithText(emptyTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(emptySubtitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(addBtnText).assertIsDisplayed()
    }

    @Test
    fun testWebAppsRenderedInList() {
        val app1 = WebApp("https://duckduckgo.com", 0, 0).apply { title = "DuckDuckGo" }
        dataManager.websites.add(app1)
        dataManager.saveWebAppData()

        val viewModel = WebAppListViewModel(dataManager)
        var openedWebApp: WebApp? = null

        composeTestRule.setContent {
            NativeAlphaTheme {
                WebAppListScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {},
                    onNavigateToAbout = {},
                    onOpenWebApp = { openedWebApp = it },
                    onOpenWebAppSettings = {},
                    onRequestCreateShortcut = {},
                )
            }
        }

        composeTestRule.onNodeWithText("DuckDuckGo").assertIsDisplayed()
        composeTestRule.onNodeWithText("duckduckgo.com").assertIsDisplayed()

        // Clicking the item opens the web app
        composeTestRule.onNodeWithText("DuckDuckGo").performClick()
        assertTrue(openedWebApp?.title == "DuckDuckGo")
    }

    @Test
    fun testOpenAddDialog() {
        val app1 = WebApp("https://example.com", 0, 0).apply { title = "Example" }
        dataManager.websites.add(app1)
        dataManager.saveWebAppData()

        val viewModel = WebAppListViewModel(dataManager)

        composeTestRule.setContent {
            NativeAlphaTheme {
                WebAppListScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {},
                    onNavigateToAbout = {},
                    onOpenWebApp = {},
                    onOpenWebAppSettings = {},
                    onRequestCreateShortcut = {},
                )
            }
        }

        val addLabel = context.getString(R.string.add_webapp)
        composeTestRule.onNodeWithText(addLabel, useUnmergedTree = true).performClick()

        // Dialog should be displayed with URL label and shortcut toggle
        val urlLabel = context.getString(R.string.url)
        val shortcutLabel = context.getString(R.string.create_shortcut_on_home_screen)

        composeTestRule.onNodeWithText(urlLabel).assertIsDisplayed()
        composeTestRule.onNodeWithText(shortcutLabel).assertIsDisplayed()
    }
}
