package com.cylonid.nativealpha

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebResourceRequest
import android.webkit.WebViewClient
import androidx.test.core.app.ApplicationProvider
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.model.WebApp
import com.cylonid.nativealpha.util.Const
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowWebSettings
import java.lang.reflect.Proxy

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WebViewActivityLifecycleAndUaTest {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val dm = DataManager.getInstance()
        val webApp =
            WebApp("https://example.com", 0, Const.getDefaultAdBlockConfig()).apply {
                title = "Lifecycle Test App"
                isOverrideGlobalSettings = true
                isUseCustomUserAgent = false
            }
        dm.websites.clear()
        dm.websites.add(webApp)
        dm.saveWebAppData()
    }

    @After
    fun tearDown() {
        ShadowWebSettings.reset()
    }

    private fun buildStartedActivity(): org.robolectric.android.controller.ActivityController<WebViewActivity> {
        val intent =
            Intent(context, WebViewActivity::class.java).apply {
                putExtra(Const.INTENT_WEBAPPID, 0)
            }
        return Robolectric
            .buildActivity(WebViewActivity::class.java, intent)
            .create()
            .start()
            .resume()
    }

    @Test
    fun testUserAgentStripsWebViewToken() {
        ShadowWebSettings.setDefaultUserAgent("Mozilla/5.0 (Linux; Android 14; Pixel 8; wv) AppleWebKit/537.36")
        val controller = buildStartedActivity()
        val activity = controller.get()
        val webView = activity.webView
        assertNotNull(webView)

        val currentUa = webView.settings.userAgentString
        assertNotNull(currentUa)
        assertFalse(currentUa.contains("; wv"))
        assertTrue(currentUa.contains("Pixel 8"))
        controller.pause().stop().destroy()
    }

    @Test
    fun testOnDestroyCleansUpWebViewAndPendingFileCallback() {
        val controller = buildStartedActivity()
        val activity = controller.get()
        var callbackInvokedWithNull = false
        activity.filePathCallback =
            ValueCallback { value ->
                callbackInvokedWithNull = value == null
            }

        controller.pause().stop().destroy()

        assertTrue(callbackInvokedWithNull)
        assertNull(activity.filePathCallback)
        assertNull(activity.webView)
    }

    @Test
    fun testShouldOverrideUrlLoadingHandlesMissingTelActivityGracefully() {
        val controller = buildStartedActivity()
        val activity = controller.get()
        val customBrowserClass =
            WebViewActivity::class.java.declaredClasses.first { it.simpleName == "CustomBrowser" }
        val ctor = customBrowserClass.getDeclaredConstructor(WebViewActivity::class.java).apply { isAccessible = true }
        val client = ctor.newInstance(activity) as WebViewClient

        // Ensure package manager resolves no handler for tel:
        Shadows.shadowOf(context.packageManager)

        val request =
            Proxy.newProxyInstance(
                WebResourceRequest::class.java.classLoader,
                arrayOf(WebResourceRequest::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "getUrl" -> Uri.parse("tel:+15551234567")
                    "isForMainFrame" -> true
                    "getMethod" -> "GET"
                    else -> null
                }
            } as WebResourceRequest

        assertTrue(client.shouldOverrideUrlLoading(activity.webView, request))
        controller.pause().stop().destroy()
    }

    @Test
    fun testBiometricProtectedWebAppDoesNotInitializeWebViewBeforeAuth() {
        val dm = DataManager.getInstance()
        dm.websites[0].isBiometricProtection = true
        dm.saveWebAppData()

        val intent =
            Intent(context, WebViewActivity::class.java).apply {
                putExtra(Const.INTENT_WEBAPPID, 0)
            }
        val controller =
            Robolectric
                .buildActivity(WebViewActivity::class.java, intent)
                .create()
                .start()
                .resume()
        val activity = controller.get()

        assertNull(activity.webView)
        controller.pause().stop().destroy()
    }

    @Test
    fun testHostMatchingRejectsSuffixAndQueryBypass() {
        assertTrue(WebViewActivity.isSameHostOrSubdomain("example.com", "example.com"))
        assertTrue(WebViewActivity.isSameHostOrSubdomain("SUB.EXAMPLE.COM", "example.com"))
        assertFalse(WebViewActivity.isSameHostOrSubdomain("evilexample.com", "example.com"))
        assertFalse(WebViewActivity.isSameHostOrSubdomain(null, "example.com"))

        val dm = DataManager.getInstance()
        dm.websites[0].apply {
            baseUrl = "https://example.com"
            isOpenUrlExternal = true
        }
        dm.saveWebAppData()

        val controller = buildStartedActivity()
        val activity = controller.get()
        val customBrowserClass =
            WebViewActivity::class.java.declaredClasses.first { it.simpleName == "CustomBrowser" }
        val ctor = customBrowserClass.getDeclaredConstructor(WebViewActivity::class.java).apply { isAccessible = true }
        val client = ctor.newInstance(activity) as WebViewClient

        val sneakyExternal =
            Proxy.newProxyInstance(
                WebResourceRequest::class.java.classLoader,
                arrayOf(WebResourceRequest::class.java),
            ) { _, method, _ ->
                when (method.name) {
                    "getUrl" -> Uri.parse("https://evil.com/?q=example.com")
                    "isForMainFrame" -> true
                    "getMethod" -> "GET"
                    else -> null
                }
            } as WebResourceRequest

        assertTrue(client.shouldOverrideUrlLoading(activity.webView, sneakyExternal))
        val startedIntent = Shadows.shadowOf(activity).nextStartedActivity
        assertNotNull(startedIntent)
        assertEquals(Intent.ACTION_VIEW, startedIntent.action)
        assertEquals(Uri.parse("https://evil.com/?q=example.com"), startedIntent.data)
        assertEquals("https://example.com", activity.webView.url)
        controller.pause().stop().destroy()
    }

    @Test
    fun testMultipleConcurrentDrmPermissionRequestsShowSingleDialogAndAutoGrantQueue() {
        val controller = buildStartedActivity()
        val activity = controller.get()
        val chromeClass =
            WebViewActivity::class.java.declaredClasses.first { it.simpleName == "CustomWebChromeClient" }
        val ctor = chromeClass.getDeclaredConstructor(WebViewActivity::class.java).apply { isAccessible = true }
        val chromeClient = ctor.newInstance(activity) as android.webkit.WebChromeClient

        val granted = mutableListOf<List<String>>()

        fun makeDrmRequest(): android.webkit.PermissionRequest =
            object : android.webkit.PermissionRequest() {
                override fun getOrigin(): Uri = Uri.parse("https://example.com")

                override fun getResources(): Array<String> = arrayOf(RESOURCE_PROTECTED_MEDIA_ID)

                override fun grant(resources: Array<String>) {
                    granted.add(resources.toList())
                }

                override fun deny() {}
            }

        chromeClient.onPermissionRequest(makeDrmRequest())
        chromeClient.onPermissionRequest(makeDrmRequest())
        chromeClient.onPermissionRequest(makeDrmRequest())

        val dialogs =
            org.robolectric.shadows.ShadowAlertDialog
                .getShownDialogs()
        assertEquals(1, dialogs.size)

        val alertDialog = dialogs.first() as androidx.appcompat.app.AlertDialog
        alertDialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick()
        org.robolectric.shadows.ShadowLooper
            .idleMainLooper()

        assertEquals(3, granted.size)
        assertEquals(
            1,
            org.robolectric.shadows.ShadowAlertDialog
                .getShownDialogs()
                .size,
        )
        controller.pause().stop().destroy()
    }

    @Test
    fun testCancelingDrmDeniesOnlyDrmBurstAndKeepsQueuedCameraRequest() {
        val dm = DataManager.getInstance()
        dm.websites[0].isCameraPermission = true
        dm.saveWebAppData()

        val controller = buildStartedActivity()
        val activity = controller.get()
        Shadows.shadowOf(activity.application).grantPermissions(android.Manifest.permission.CAMERA)

        val chromeClass =
            WebViewActivity::class.java.declaredClasses.first { it.simpleName == "CustomWebChromeClient" }
        val ctor = chromeClass.getDeclaredConstructor(WebViewActivity::class.java).apply { isAccessible = true }
        val chromeClient = ctor.newInstance(activity) as android.webkit.WebChromeClient

        val granted = mutableListOf<List<String>>()
        var deniedCount = 0

        fun makeRequest(vararg resources: String): android.webkit.PermissionRequest =
            object : android.webkit.PermissionRequest() {
                override fun getOrigin(): Uri = Uri.parse("https://example.com")

                override fun getResources(): Array<String> = arrayOf(*resources)

                override fun grant(grantedResources: Array<String>) {
                    granted.add(grantedResources.toList())
                }

                override fun deny() {
                    deniedCount++
                }
            }

        chromeClient.onPermissionRequest(
            makeRequest(android.webkit.PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID),
        )
        chromeClient.onPermissionRequest(
            makeRequest(android.webkit.PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID),
        )
        chromeClient.onPermissionRequest(
            makeRequest(
                android.webkit.PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID,
                android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE,
            ),
        )

        val dialogs =
            org.robolectric.shadows.ShadowAlertDialog
                .getShownDialogs()
        assertEquals(1, dialogs.size)

        val alertDialog = dialogs.first() as androidx.appcompat.app.AlertDialog
        alertDialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE).performClick()
        org.robolectric.shadows.ShadowLooper
            .idleMainLooper()

        assertEquals(2, deniedCount)
        assertEquals(listOf(listOf(android.webkit.PermissionRequest.RESOURCE_VIDEO_CAPTURE)), granted)
        assertEquals(
            1,
            org.robolectric.shadows.ShadowAlertDialog
                .getShownDialogs()
                .size,
        )
        controller.pause().stop().destroy()
    }

    @Test
    fun testOnPermissionRequestCanceledDismissesActiveDialogAndAdvancesQueue() {
        val controller = buildStartedActivity()
        val activity = controller.get()
        val chromeClass =
            WebViewActivity::class.java.declaredClasses.first { it.simpleName == "CustomWebChromeClient" }
        val ctor = chromeClass.getDeclaredConstructor(WebViewActivity::class.java).apply { isAccessible = true }
        val chromeClient = ctor.newInstance(activity) as android.webkit.WebChromeClient

        val granted = mutableListOf<String>()

        fun makeTaggedDrmRequest(tag: String): android.webkit.PermissionRequest =
            object : android.webkit.PermissionRequest() {
                override fun getOrigin(): Uri = Uri.parse("https://example.com")

                override fun getResources(): Array<String> = arrayOf(RESOURCE_PROTECTED_MEDIA_ID)

                override fun grant(resources: Array<String>) {
                    granted.add(tag)
                }

                override fun deny() {}
            }

        val first = makeTaggedDrmRequest("first")
        val canceledQueued = makeTaggedDrmRequest("canceledQueued")
        val third = makeTaggedDrmRequest("third")

        chromeClient.onPermissionRequest(first)
        chromeClient.onPermissionRequest(canceledQueued)
        chromeClient.onPermissionRequest(third)

        chromeClient.onPermissionRequestCanceled(canceledQueued)

        val dialogsBeforeCancel =
            org.robolectric.shadows.ShadowAlertDialog
                .getShownDialogs()
        assertEquals(1, dialogsBeforeCancel.size)
        val firstDialog = dialogsBeforeCancel.first() as androidx.appcompat.app.AlertDialog
        assertTrue(firstDialog.isShowing)

        chromeClient.onPermissionRequestCanceled(first)
        org.robolectric.shadows.ShadowLooper
            .idleMainLooper()

        assertFalse(firstDialog.isShowing)
        assertFalse(DataManager.getInstance().websites[0].isDrmAllowed)

        val dialogsAfterCancel =
            org.robolectric.shadows.ShadowAlertDialog
                .getShownDialogs()
        assertEquals(2, dialogsAfterCancel.size)
        val secondDialog = dialogsAfterCancel.last() as androidx.appcompat.app.AlertDialog
        secondDialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick()
        org.robolectric.shadows.ShadowLooper
            .idleMainLooper()

        assertEquals(listOf("third"), granted)
        controller.pause().stop().destroy()
    }

    @Test
    fun testOnNewIntentUpdatesIntentAndPreservesWebViewInstance() {
        val controller = buildStartedActivity()
        val activity = controller.get()
        val originalWebView = activity.webView
        assertNotNull(originalWebView)

        val newIntent =
            Intent(activity, WebViewActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra(Const.INTENT_WEBAPPID, 0)
            }
        controller.newIntent(newIntent).resume()

        assertEquals(newIntent, activity.intent)
        assertTrue(activity.webView === originalWebView)
        assertFalse(activity.isFinishing)
        controller.pause().stop().destroy()
    }
}
