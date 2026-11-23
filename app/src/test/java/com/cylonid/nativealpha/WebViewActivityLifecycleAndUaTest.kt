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
        controller.pause().stop().destroy()
    }
}
