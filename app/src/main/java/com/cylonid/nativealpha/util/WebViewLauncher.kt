package com.cylonid.nativealpha.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.model.WebApp
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.processphoenix.ProcessPhoenix
import java.lang.NullPointerException

object WebViewLauncher {
    @JvmStatic
    fun startWebView(
        webapp: WebApp,
        c: Context,
    ) {
        try {
            c.startActivity(createWebViewIntent(webapp, c))
        } catch (e: NullPointerException) {
            NotificationUtils.showInfoSnackbar(
                c as AppCompatActivity,
                c.getString(R.string.webview_activity_launch_failed),
                Snackbar.LENGTH_LONG,
            )
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun startWebViewInNewProcess(
        webapp: WebApp,
        a: Activity,
    ) {
        try {
            ProcessPhoenix.triggerRebirth(a, createWebViewIntent(webapp, a))
        } catch (e: NullPointerException) {
            NotificationUtils.showInfoSnackbar(
                a,
                a.getString(R.string.webview_activity_launch_failed),
                Snackbar.LENGTH_LONG,
            )
            e.printStackTrace()
        }
    }

    @JvmStatic
    fun createWebViewIntent(
        webapp: WebApp,
        c: Context?,
    ): Intent? {
        val packageName = "com.cylonid.nativealpha"
        var webViewClass: Class<*>? = null
        try {
            webViewClass =
                if (webapp.containerId != Const.NO_CONTAINER) {
                    Class.forName("$packageName.WebSandboxActivity${webapp.containerId}")
                } else {
                    Class.forName("$packageName.WebViewActivity")
                }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        if (webViewClass == null) {
            return null
        }
        val intent = Intent(c, webViewClass)
        if (webapp.isBiometricProtection) intent.flags = Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        intent.putExtra(Const.INTENT_WEBAPPID, webapp.ID)
        intent.data = (webapp.baseUrl + webapp.ID).toUri()
        intent.action = Intent.ACTION_VIEW
        return intent
    }
}
