package com.cylonid.nativealpha

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.model.WebApp
import com.cylonid.nativealpha.util.Const
import io.github.edsuns.adblockclient.hasException
import io.github.edsuns.adblockclient.isException
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WebViewActivityAdblockTest {
    private val testWebAppId = 0
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val dm = DataManager.getInstance()
        val webApp = WebApp("https://example.com", testWebAppId, Const.getDefaultAdBlockConfig())
        webApp.title = "Example App"
        webApp.isOverrideGlobalSettings = true
        webApp.isUseAdblock = true
        webApp.isAllowJs = true
        if (dm.websites.isEmpty()) {
            dm.websites.add(webApp)
        } else {
            dm.websites[0] = webApp
        }
        dm.saveWebAppData()
    }

    @Test
    fun testLaunchWebViewWithAdblockEnabled() {
        val intent =
            Intent(context, WebViewActivity::class.java).apply {
                putExtra(Const.INTENT_WEBAPPID, testWebAppId)
            }
        val controller = Robolectric.buildActivity(WebViewActivity::class.java, intent)
        val activity =
            controller
                .create()
                .start()
                .resume()
                .get()
        val field = WebViewActivity::class.java.getDeclaredField("wv")
        field.isAccessible = true
        val wv = field.get(activity) as android.webkit.WebView
        org.junit.Assert.assertNotNull("wv should not be null after onCreate", wv)

        val shadowWv = org.robolectric.Shadows.shadowOf(wv)
        val client = shadowWv.webViewClient
        client.onPageStarted(wv, "https://example.com", null)

        val request =
            object : android.webkit.WebResourceRequest {
                override fun getUrl(): android.net.Uri = android.net.Uri.parse("https://example.com/test.js")

                override fun isForMainFrame(): Boolean = false

                override fun isRedirect(): Boolean = false

                override fun hasGesture(): Boolean = false

                override fun getMethod(): String = "GET"

                override fun getRequestHeaders(): Map<String, String> = emptyMap()
            }
        var response: android.webkit.WebResourceResponse? = null
        var error: Throwable? = null
        val thread =
            Thread {
                try {
                    response = client.shouldInterceptRequest(wv, request)
                } catch (t: Throwable) {
                    error = t
                }
            }
        thread.start()
        org.robolectric.shadows.ShadowLooper
            .idleMainLooper()
        thread.join(5000)
        if (error != null) {
            throw error!!
        }
        println("Intercept response: $response")
    }

    @Test
    fun testLaunchWebViewWhenAdblockPreviouslyCrashedRecovers() {
        val dm = DataManager.getInstance()
        dm.hasAdblockCrashed = true

        val intent =
            Intent(context, WebViewActivity::class.java).apply {
                putExtra(Const.INTENT_WEBAPPID, testWebAppId)
            }
        val controller = Robolectric.buildActivity(WebViewActivity::class.java, intent)
        val activity =
            controller
                .create()
                .start()
                .resume()
                .get()
        val field = WebViewActivity::class.java.getDeclaredField("wv")
        field.isAccessible = true
        val wv = field.get(activity) as android.webkit.WebView
        org.junit.Assert.assertNotNull("wv should not be null after recovery", wv)
        org.junit.Assert.assertFalse("hasAdblockCrashed should be reset after recovery", dm.hasAdblockCrashed)
    }

    @Test
    fun testShouldInterceptRequestHandlesNullGracefully() {
        val intent =
            Intent(context, WebViewActivity::class.java).apply {
                putExtra(Const.INTENT_WEBAPPID, testWebAppId)
            }
        val controller = Robolectric.buildActivity(WebViewActivity::class.java, intent)
        val activity =
            controller
                .create()
                .start()
                .resume()
                .get()
        val field = WebViewActivity::class.java.getDeclaredField("wv")
        field.isAccessible = true
        val wv = field.get(activity) as android.webkit.WebView
        val shadowWv = org.robolectric.Shadows.shadowOf(wv)
        val client = shadowWv.webViewClient

        // Passing null request should not crash
        val response = client.shouldInterceptRequest(wv, null as android.webkit.WebResourceRequest?)
        org.junit.Assert.assertNull("Should return null for null request", response)
    }

    @Test
    fun testMatchResultBinaryCompatibility() {
        val result =
            io.github.edsuns.adblockclient
                .MatchResult(false, null, null)
        org.junit.Assert.assertFalse(result.isException)
        org.junit.Assert.assertFalse(result.hasException)

        val exceptionResult =
            io.github.edsuns.adblockclient
                .MatchResult(false, null, "@@||example.com")
        org.junit.Assert.assertTrue(exceptionResult.isException)
        org.junit.Assert.assertTrue(exceptionResult.hasException)

        // Verify the exact static method expected by ad-filter DetectorImpl.shouldBlock exists
        val method =
            Class
                .forName("io.github.edsuns.adblockclient.MatchResultKt")
                .getMethod("isException", io.github.edsuns.adblockclient.MatchResult::class.java)
        org.junit.Assert.assertNotNull(method)
        org.junit.Assert.assertTrue(method.invoke(null, exceptionResult) as Boolean)
    }
}
