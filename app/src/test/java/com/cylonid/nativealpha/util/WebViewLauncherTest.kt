package com.cylonid.nativealpha.util

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.test.core.app.ApplicationProvider
import com.cylonid.nativealpha.MainActivity
import com.cylonid.nativealpha.WebSandboxActivity0
import com.cylonid.nativealpha.WebSandboxActivity7
import com.cylonid.nativealpha.WebViewActivity
import com.cylonid.nativealpha.model.WebApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.shadow.api.Shadow

@Implements(ActivityManager.AppTask::class)
class ShadowLauncherTestAppTask {
    var taskInfoValue: ActivityManager.RecentTaskInfo = ActivityManager.RecentTaskInfo()
    var movedToFront: Boolean = false

    @Implementation
    fun getTaskInfo(): ActivityManager.RecentTaskInfo = taskInfoValue

    @Implementation
    fun moveToFront() {
        movedToFront = true
    }
}

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], shadows = [ShadowLauncherTestAppTask::class])
class WebViewLauncherTest {
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun testCreateWebViewIntentRoutesStandardAndSandboxedContainers() {
        val standardApp = WebApp("https://example.com", 4, 0)
        val standardIntent = WebViewLauncher.createWebViewIntent(standardApp, context)
        assertNotNull(standardIntent)
        assertEquals(WebViewActivity::class.java.name, standardIntent!!.component?.className)
        assertEquals(4, standardIntent.getIntExtra(Const.INTENT_WEBAPPID, -1))
        assertEquals(Intent.ACTION_VIEW, standardIntent.action)
        assertEquals("https://example.com4", standardIntent.dataString)
        assertTrue((standardIntent.flags and Intent.FLAG_ACTIVITY_NEW_DOCUMENT) != 0)
        assertTrue((standardIntent.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP) != 0)
        assertTrue((standardIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK) != 0)

        val secondStandardApp = WebApp("https://github.com", 8, 1)
        val secondIntent = WebViewLauncher.createWebViewIntent(secondStandardApp, context)
        assertNotNull(secondIntent)
        assertNotEquals(standardIntent.dataString, secondIntent!!.dataString)
        assertTrue((secondIntent.flags and Intent.FLAG_ACTIVITY_NEW_DOCUMENT) != 0)

        val sandbox0 =
            WebApp("https://s0.example", 5, 1).apply {
                isUseContainer = true
                containerId = 0
            }
        val intent0 = WebViewLauncher.createWebViewIntent(sandbox0, context)
        assertEquals(WebSandboxActivity0::class.java.name, intent0?.component?.className)

        val sandbox7 =
            WebApp("https://s7.example", 6, 2).apply {
                isUseContainer = true
                containerId = 7
                isBiometricProtection = true
            }
        val intent7 = WebViewLauncher.createWebViewIntent(sandbox7, context)
        assertNotNull(intent7)
        assertEquals(WebSandboxActivity7::class.java.name, intent7!!.component?.className)
        assertTrue((intent7.flags and Intent.FLAG_ACTIVITY_NEW_DOCUMENT) != 0)
        assertTrue((intent7.flags and Intent.FLAG_ACTIVITY_SINGLE_TOP) != 0)
        assertTrue((intent7.flags and Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) != 0)
    }

    @Test
    fun testManifestIsolatesWebViewDocumentTasksFromMainActivity() {
        val pm = context.packageManager
        val mainInfo = pm.getActivityInfo(ComponentName(context, MainActivity::class.java), 0)
        val webViewInfo = pm.getActivityInfo(ComponentName(context, WebViewActivity::class.java), 0)
        assertEquals(ActivityInfo.DOCUMENT_LAUNCH_INTO_EXISTING, webViewInfo.documentLaunchMode)
        assertEquals(ActivityInfo.LAUNCH_SINGLE_TOP, webViewInfo.launchMode)
        assertFalse(webViewInfo.taskAffinity.isNullOrEmpty())
        assertNotEquals(mainInfo.taskAffinity, webViewInfo.taskAffinity)

        val sandboxInfo = pm.getActivityInfo(ComponentName(context, WebSandboxActivity0::class.java), 0)
        assertEquals(ActivityInfo.DOCUMENT_LAUNCH_INTO_EXISTING, sandboxInfo.documentLaunchMode)
        assertEquals(ActivityInfo.LAUNCH_SINGLE_TOP, sandboxInfo.launchMode)
        assertFalse(sandboxInfo.taskAffinity.isNullOrEmpty())
        assertNotEquals(mainInfo.taskAffinity, sandboxInfo.taskAffinity)
        assertNotEquals(webViewInfo.taskAffinity, sandboxInfo.taskAffinity)
    }

    @Test
    fun testStartWebViewMovesExistingLiveTaskToFront() {
        val app =
            WebApp("https://amazon.example", 3, 0).apply {
                isUseContainer = true
                containerId = 1
            }
        val expectedIntent = WebViewLauncher.createWebViewIntent(app, context)!!
        val am = context.getSystemService(ActivityManager::class.java)!!
        val shadowAm = Shadows.shadowOf(am)

        val task = Shadow.newInstanceOf(ActivityManager.AppTask::class.java)
        val shadowTask = Shadow.extract<ShadowLauncherTestAppTask>(task)
        shadowTask.taskInfoValue =
            ActivityManager.RecentTaskInfo().apply {
                numActivities = 1
                baseIntent =
                    Intent(expectedIntent).apply {
                        component = expectedIntent.component
                    }
            }
        shadowAm.setAppTasks(listOf(task))

        WebViewLauncher.startWebView(app, context)

        assertTrue(shadowTask.movedToFront)
        assertNull(Shadows.shadowOf(context as android.app.Application).nextStartedActivity)
    }
}
