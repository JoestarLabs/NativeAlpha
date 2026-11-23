package com.cylonid.nativealpha.ui.webapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.neverEqualPolicy
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.cylonid.nativealpha.model.WebApp

class WebAppSettingsViewModel : ViewModel() {
    private var loadedWebAppId: Int? = null
    var draftWebApp by mutableStateOf<WebApp?>(null, policy = neverEqualPolicy())
        private set
    var isDirty by mutableStateOf(false)
        private set

    fun ensureLoaded(
        webappId: Int,
        initial: WebApp,
    ): WebApp {
        if (loadedWebAppId != webappId || draftWebApp == null) {
            loadedWebAppId = webappId
            draftWebApp = WebApp(initial)
            isDirty = false
        }
        return draftWebApp!!
    }

    fun updateDraft(modify: WebApp.() -> Unit) {
        val current = draftWebApp ?: return
        val updated = WebApp(current)
        updated.modify()
        draftWebApp = updated
        isDirty = true
    }

    fun markSaved() {
        isDirty = false
    }
}
