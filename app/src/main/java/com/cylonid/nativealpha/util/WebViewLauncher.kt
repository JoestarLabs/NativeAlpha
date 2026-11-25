package com.cylonid.nativealpha.util

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.WebSandboxActivity0
import com.cylonid.nativealpha.WebSandboxActivity1
import com.cylonid.nativealpha.WebSandboxActivity2
import com.cylonid.nativealpha.WebSandboxActivity3
import com.cylonid.nativealpha.WebSandboxActivity4
import com.cylonid.nativealpha.WebSandboxActivity5
import com.cylonid.nativealpha.WebSandboxActivity6
import com.cylonid.nativealpha.WebSandboxActivity7
import com.cylonid.nativealpha.WebViewActivity
import com.cylonid.nativealpha.model.WebApp
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.processphoenix.ProcessPhoenix
import timber.log.Timber

object WebViewLauncher {
    private val SANDBOX_ACTIVITIES: Array<Class<out Activity>> =
        arrayOf(
            WebSandboxActivity0::class.java,
            WebSandboxActivity1::class.java,
            WebSandboxActivity2::class.java,
            WebSandboxActivity3::class.java,
            WebSandboxActivity4::class.java,
            WebSandboxActivity5::class.java,
            WebSandboxActivity6::class.java,
            WebSandboxActivity7::class.java,
        )

    private fun showLaunchFailed(context: Context) {
        val msg = context.getString(R.string.webview_activity_launch_failed)
        if (context is Activity) {
            NotificationUtils.showInfoSnackbar(context, msg, Snackbar.LENGTH_LONG)
        } else {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    @JvmStatic
    fun startWebView(
        webapp: WebApp,
        c: Context,
    ) {
        val intent = createWebViewIntent(webapp, c)
        if (intent == null) {
            showLaunchFailed(c)
            Timber.e("Failed to start WebView activity for container %d", webapp.containerId)
            return
        }
        if (moveExistingTaskToFront(webapp, intent, c)) {
            return
        }
        c.startActivity(intent)
    }

    private fun moveExistingTaskToFront(
        webapp: WebApp,
        intent: Intent,
        c: Context,
    ): Boolean {
        val activityManager = c.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return false
        val targetComponent = intent.component ?: return false
        for (task in activityManager.appTasks.orEmpty()) {
            val taskInfo = runCatching { task.taskInfo }.getOrNull() ?: continue
            if (taskInfo.numActivities <= 0) continue
            val baseIntent = taskInfo.baseIntent ?: continue
            val baseId = baseIntent.getIntExtra(Const.INTENT_WEBAPPID, -1)
            if (baseId == webapp.ID && baseIntent.component == targetComponent) {
                val moved = runCatching { task.moveToFront() }.isSuccess
                if (moved) {
                    return true
                }
            }
        }
        return false
    }

    @JvmStatic
    fun startWebViewInNewProcess(
        webapp: WebApp,
        a: Activity,
    ): Boolean {
        val intent = createWebViewIntent(webapp, a)
        if (intent == null) {
            showLaunchFailed(a)
            Timber.e("Failed to start WebView activity in new process for container %d", webapp.containerId)
            return false
        }
        ProcessPhoenix.triggerRebirth(a, intent)
        return true
    }

    @JvmStatic
    fun createWebViewIntent(
        webapp: WebApp,
        c: Context?,
    ): Intent? {
        if (c == null) {
            return null
        }
        val webViewClass: Class<out Activity> =
            if (webapp.containerId != Const.NO_CONTAINER) {
                val sandboxClass = SANDBOX_ACTIVITIES.getOrNull(webapp.containerId)
                if (sandboxClass == null) {
                    Timber.e("WebView activity class not found for container %d", webapp.containerId)
                    return null
                }
                sandboxClass
            } else {
                WebViewActivity::class.java
            }
        val intent = Intent(c, webViewClass)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        if (c !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (webapp.isBiometricProtection) {
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        }
        intent.putExtra(Const.INTENT_WEBAPPID, webapp.ID)
        intent.data = (webapp.baseUrl + webapp.ID).toUri()
        intent.action = Intent.ACTION_VIEW
        return intent
    }
}
