package com.cylonid.nativealpha.ui.webapplist

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.model.WebApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WebAppListUiState(
    val allWebApps: List<WebApp> = emptyList(),
    val filteredWebApps: List<WebApp> = emptyList(),
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isLoading: Boolean = false,
    val lastDeletedWebApp: Pair<WebApp, Int>? = null,
)

class WebAppListViewModel(
    private val dataManager: DataManager = DataManager.getInstance(),
) : ViewModel() {
    private val _uiState = MutableStateFlow(WebAppListUiState())
    val uiState: StateFlow<WebAppListUiState> = _uiState.asStateFlow()

    init {
        loadWebApps()
    }

    fun loadWebApps() {
        val active = dataManager.activeWebsites.sortedBy { it.order }
        _uiState.update { current ->
            current.copy(
                allWebApps = active,
                filteredWebApps = filterList(active, current.searchQuery),
                isLoading = false,
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredWebApps = filterList(current.allWebApps, query),
            )
        }
    }

    fun setSearchActive(active: Boolean) {
        _uiState.update { current ->
            current.copy(
                isSearchActive = active,
                searchQuery = if (!active) "" else current.searchQuery,
                filteredWebApps = if (!active) current.allWebApps else current.filteredWebApps,
            )
        }
    }

    fun reorderWebApps(
        fromIndex: Int,
        toIndex: Int,
    ) {
        _uiState.update { current ->
            val list = current.filteredWebApps.toMutableList()
            if (fromIndex in list.indices && toIndex in list.indices) {
                val item = list.removeAt(fromIndex)
                list.add(toIndex, item)
                current.copy(
                    filteredWebApps = list,
                    allWebApps = if (current.searchQuery.isBlank()) list else current.allWebApps,
                )
            } else {
                current
            }
        }
    }

    fun commitReorder() {
        viewModelScope.launch {
            val currentList = _uiState.value.allWebApps
            for ((index, webApp) in currentList.withIndex()) {
                val stored = dataManager.websites.firstOrNull { it.ID == webApp.ID }
                if (stored != null) {
                    stored.order = index
                }
            }
            dataManager.saveWebAppData()
        }
    }

    fun deleteWebApp(
        context: Context,
        webApp: WebApp,
    ) {
        val originalIndex = _uiState.value.allWebApps.indexOfFirst { it.ID == webApp.ID }
        webApp.markInactive(context)

        val updatedActive = dataManager.activeWebsites.sortedBy { it.order }
        for ((index, app) in updatedActive.withIndex()) {
            val stored = dataManager.websites.firstOrNull { it.ID == app.ID }
            if (stored != null) {
                stored.order = index
            }
        }
        dataManager.saveWebAppData()

        _uiState.update { current ->
            current.copy(
                allWebApps = updatedActive,
                filteredWebApps = filterList(updatedActive, current.searchQuery),
                lastDeletedWebApp = if (originalIndex >= 0) Pair(webApp, originalIndex) else null,
            )
        }
    }

    fun undoDelete() {
        val lastDeleted = _uiState.value.lastDeletedWebApp ?: return
        val webApp = lastDeleted.first
        webApp.isActiveEntry = true
        dataManager.saveWebAppData()

        loadWebApps()
        _uiState.update { it.copy(lastDeletedWebApp = null) }
    }

    fun addWebApp(url: String): WebApp {
        val trimmed = url.trim()
        val urlWithProtocol =
            if (trimmed.startsWith("https://") || trimmed.startsWith("http://")) {
                trimmed
            } else {
                "https://$trimmed"
            }

        val newSite =
            WebApp(
                urlWithProtocol,
                dataManager.incrementedID,
                dataManager.incrementedOrder,
            )
        newSite.applySettingsForNewWebApp()
        dataManager.addWebsite(newSite)
        dataManager.saveWebAppData()

        loadWebApps()
        return newSite
    }

    private fun filterList(
        list: List<WebApp>,
        query: String,
    ): List<WebApp> {
        if (query.isBlank()) return list
        val lower = query.lowercase().trim()
        return list.filter { app ->
            app.title.lowercase().contains(lower) ||
                app.baseUrl.lowercase().contains(lower)
        }
    }
}
