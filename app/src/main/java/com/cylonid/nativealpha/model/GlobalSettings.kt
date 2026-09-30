package com.cylonid.nativealpha.model

import com.cylonid.nativealpha.util.Const

data class GlobalSettings(
    var isClearCache: Boolean = false,
    var isTwoFingerMultitouch: Boolean = true,
    var isThreeFingerMultitouch: Boolean = false,
    var isShowProgressbar: Boolean = false,
    var isMultitouchReload: Boolean = true,
    var themeId: Int = 0,
    var globalWebApp: WebApp = WebApp("about:blank", Int.MAX_VALUE, Const.getDefaultAdBlockConfig()),
    var alwaysShowSoftwareButtons: Boolean = false,
    var clear_cookies: Boolean = false,
) {
    fun setClearCookies(clear_cookies: Boolean) {
        this.clear_cookies = clear_cookies
    }
}
