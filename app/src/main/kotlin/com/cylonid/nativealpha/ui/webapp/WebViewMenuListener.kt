package com.cylonid.nativealpha.ui.webapp

interface WebViewMenuListener {
    fun onBack()

    fun onForward()

    fun onReload()

    fun onShare()

    fun onCopyUrl()

    fun onMainMenu()

    fun onFallbackContextMenu()

    fun onCloseWebApp()
}
