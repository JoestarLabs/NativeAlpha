package com.cylonid.nativealpha.ui.webapplist

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.model.WebApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WebAppListViewModelTest {
    private lateinit var context: Context
    private lateinit var dataManager: DataManager
    private lateinit var viewModel: WebAppListViewModel

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        dataManager = DataManager.getInstance()
        dataManager.websites.clear()

        val app1 = WebApp("https://google.com", 0, 0).apply { title = "Google" }
        val app2 = WebApp("https://github.com", 1, 1).apply { title = "GitHub" }
        val app3 = WebApp("https://reddit.com", 2, 2).apply { title = "Reddit" }

        dataManager.websites.addAll(listOf(app1, app2, app3))
        dataManager.saveWebAppData()

        viewModel = WebAppListViewModel(dataManager)
    }

    @Test
    fun testInitialStateLoadsActiveWebApps() {
        val state = viewModel.uiState.value
        assertEquals(3, state.allWebApps.size)
        assertEquals(3, state.filteredWebApps.size)
        assertEquals("Google", state.filteredWebApps[0].title)
        assertEquals("GitHub", state.filteredWebApps[1].title)
        assertEquals("Reddit", state.filteredWebApps[2].title)
        assertFalse(state.isSearchActive)
        assertEquals("", state.searchQuery)
    }

    @Test
    fun testSearchQueryFiltersByTitleAndUrl() {
        viewModel.setSearchActive(true)
        viewModel.onSearchQueryChange("git")

        val state = viewModel.uiState.value
        assertTrue(state.isSearchActive)
        assertEquals(1, state.filteredWebApps.size)
        assertEquals("GitHub", state.filteredWebApps[0].title)

        // Filter by URL domain
        viewModel.onSearchQueryChange("reddit.com")
        val state2 = viewModel.uiState.value
        assertEquals(1, state2.filteredWebApps.size)
        assertEquals("Reddit", state2.filteredWebApps[0].title)

        // Empty result
        viewModel.onSearchQueryChange("nonexistent")
        val state3 = viewModel.uiState.value
        assertTrue(state3.filteredWebApps.isEmpty())
        assertEquals(3, state3.allWebApps.size)

        // Clear search
        viewModel.setSearchActive(false)
        val state4 = viewModel.uiState.value
        assertFalse(state4.isSearchActive)
        assertEquals(3, state4.filteredWebApps.size)
    }

    @Test
    fun testReorderingWebApps() {
        viewModel.reorderWebApps(0, 2)
        val state = viewModel.uiState.value

        assertEquals("GitHub", state.filteredWebApps[0].title)
        assertEquals("Reddit", state.filteredWebApps[1].title)
        assertEquals("Google", state.filteredWebApps[2].title)

        viewModel.commitReorder()
        assertEquals(0, dataManager.websites.first { it.title == "GitHub" }.order)
        assertEquals(1, dataManager.websites.first { it.title == "Reddit" }.order)
        assertEquals(2, dataManager.websites.first { it.title == "Google" }.order)
    }

    @Test
    fun testDeleteAndUndoWebApp() {
        val toDelete = viewModel.uiState.value.filteredWebApps[1] // GitHub
        viewModel.deleteWebApp(context, toDelete)

        val stateAfterDelete = viewModel.uiState.value
        assertEquals(2, stateAfterDelete.allWebApps.size)
        assertEquals(2, stateAfterDelete.filteredWebApps.size)
        assertNotNull(stateAfterDelete.lastDeletedWebApp)
        assertEquals("GitHub", stateAfterDelete.lastDeletedWebApp?.first?.title)

        // Undo
        viewModel.undoDelete()
        val stateAfterUndo = viewModel.uiState.value
        assertEquals(3, stateAfterUndo.allWebApps.size)
        assertNull(stateAfterUndo.lastDeletedWebApp)
        assertTrue(stateAfterUndo.allWebApps.any { it.title == "GitHub" })
    }

    @Test
    fun testAddWebAppAddsHttpProtocolIfMissing() {
        val newApp = viewModel.addWebApp("wikipedia.org")
        assertTrue(newApp.baseUrl.startsWith("https://wikipedia.org"))

        val state = viewModel.uiState.value
        assertEquals(4, state.allWebApps.size)
        assertTrue(state.allWebApps.any { it.baseUrl == "https://wikipedia.org" })
    }
}
