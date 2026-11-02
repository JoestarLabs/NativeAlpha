package com.cylonid.nativealpha

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.test.core.app.ApplicationProvider
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.model.WebApp
import com.cylonid.nativealpha.util.Const
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import org.robolectric.shadows.ShadowToast
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WebViewActivityBackNavigationTest {
    private val testWebAppId = 0
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val dm = DataManager.getInstance()
        val webApp = WebApp("https://example.com", testWebAppId, Const.getDefaultAdBlockConfig())
        webApp.title = "Back Nav Test App"
        webApp.isOverrideGlobalSettings = true
        webApp.isAllowJs = true
        if (dm.websites.isEmpty()) {
            dm.websites.add(webApp)
        } else {
            dm.websites[0] = webApp
        }
        dm.saveWebAppData()
    }

    private fun createActivity(): WebViewActivity {
        val intent =
            Intent(context, WebViewActivity::class.java).apply {
                putExtra(Const.INTENT_WEBAPPID, testWebAppId)
            }
        return Robolectric
            .buildActivity(WebViewActivity::class.java, intent)
            .create()
            .start()
            .resume()
            .get()
    }

    @Test
    fun testIsSameOrRedirectUrlComparison() {
        val activity = createActivity()
        val method =
            WebViewActivity::class.java.getDeclaredMethod(
                "isSameOrRedirectUrl",
                String::class.java,
                String::class.java,
            )
        method.isAccessible = true

        fun compare(
            u1: String?,
            u2: String?,
        ): Boolean = method.invoke(activity, u1, u2) as Boolean

        // Exact match
        assertTrue(compare("https://example.com/page", "https://example.com/page"))

        // Trailing slash difference
        assertTrue(compare("https://example.com/page/", "https://example.com/page"))
        assertTrue(compare("https://example.com/page", "https://example.com/page/"))

        // Empty hash fragment
        assertTrue(compare("https://example.com/page#", "https://example.com/page"))

        // HTTP to HTTPS redirect
        assertTrue(compare("http://example.com/page", "https://example.com/page"))

        // Different hash routing SPA routes should NOT be treated as same page
        assertFalse(compare("https://example.com/#/home", "https://example.com/#/settings"))

        // Distinct pages should NOT be treated as same page
        assertFalse(compare("https://example.com/page1", "https://example.com/page2"))
        assertFalse(compare("https://example.com/path", "https://other.com/path"))

        // Null checks
        assertFalse(compare(null, "https://example.com"))
        assertFalse(compare("https://example.com", null))
        assertFalse(compare(null, null))
    }

    @Test
    fun testHandleBackPressedDoubleTapToExit() {
        val activity = createActivity()
        val handleBackPressedMethod = WebViewActivity::class.java.getDeclaredMethod("handleBackPressed")
        handleBackPressedMethod.isAccessible = true

        val quitField = WebViewActivity::class.java.getDeclaredField("quitOnNextBackpress")
        quitField.isAccessible = true

        // Initial state
        assertFalse(quitField.getBoolean(activity))

        // First back press: arms quitOnNextBackpress and shows toast
        handleBackPressedMethod.invoke(activity)
        assertTrue(quitField.getBoolean(activity))
        assertEquals(context.getString(R.string.press_back_again_to_exit), ShadowToast.getTextOfLatestToast())

        // Advancing time past 2000ms should disarm exit
        ShadowLooper.idleMainLooper(2100, TimeUnit.MILLISECONDS)
        assertFalse(quitField.getBoolean(activity))

        // Re-arm
        handleBackPressedMethod.invoke(activity)
        assertTrue(quitField.getBoolean(activity))

        // Second back press within interval should consume quit flag and move to back
        handleBackPressedMethod.invoke(activity)
        assertFalse(quitField.getBoolean(activity))
    }

    @Test
    fun testOnPauseResetsQuitOnNextBackpress() {
        val controller =
            Robolectric
                .buildActivity(
                    WebViewActivity::class.java,
                    Intent(context, WebViewActivity::class.java).apply {
                        putExtra(Const.INTENT_WEBAPPID, testWebAppId)
                    },
                ).create()
                .start()
                .resume()

        val activity = controller.get()
        val handleBackPressedMethod = WebViewActivity::class.java.getDeclaredMethod("handleBackPressed")
        handleBackPressedMethod.isAccessible = true

        val quitField = WebViewActivity::class.java.getDeclaredField("quitOnNextBackpress")
        quitField.isAccessible = true

        // Arm back press
        handleBackPressedMethod.invoke(activity)
        assertTrue(quitField.getBoolean(activity))

        // Pause activity
        controller.pause()
        assertFalse(quitField.getBoolean(activity))
    }

    @Test
    fun testShouldOverrideUrlLoadingIgnoresSubframes() {
        val activity = createActivity()
        val customBrowserField =
            activity.javaClass.declaredClasses.firstOrNull { it.simpleName == "CustomBrowser" }
                ?: error("CustomBrowser class not found")

        val constructor = customBrowserField.getDeclaredConstructor(WebViewActivity::class.java)
        constructor.isAccessible = true
        val client = constructor.newInstance(activity) as WebViewClient

        val subframeRequest =
            createWebResourceRequest(
                uri = Uri.parse("https://example.com/iframe.html"),
                isForMainFrame = false,
            )

        val wvField = WebViewActivity::class.java.getDeclaredField("wv")
        wvField.isAccessible = true
        val wv = wvField.get(activity) as WebView

        val result = client.shouldOverrideUrlLoading(wv, subframeRequest)
        assertFalse("Subframes should not be overridden by WebViewActivity", result)
    }

    @Test
    fun testShouldOverrideUrlLoadingAllowsNormalNavigation() {
        val activity = createActivity()
        val customBrowserField =
            activity.javaClass.declaredClasses.firstOrNull { it.simpleName == "CustomBrowser" }
                ?: error("CustomBrowser class not found")

        val constructor = customBrowserField.getDeclaredConstructor(WebViewActivity::class.java)
        constructor.isAccessible = true
        val client = constructor.newInstance(activity) as WebViewClient

        val mainFrameRequest =
            createWebResourceRequest(
                uri = Uri.parse("https://example.com/another-page"),
                isForMainFrame = true,
            )

        val wvField = WebViewActivity::class.java.getDeclaredField("wv")
        wvField.isAccessible = true
        val wv = wvField.get(activity) as WebView

        val result = client.shouldOverrideUrlLoading(wv, mainFrameRequest)
        assertFalse("Normal HTTPS in-app navigation should return false to let WebView handle it natively", result)
    }

    private fun createWebResourceRequest(
        uri: Uri,
        isForMainFrame: Boolean,
    ): WebResourceRequest {
        val handler =
            InvocationHandler { _, method, _ ->
                when (method.name) {
                    "getUrl" -> uri
                    "isForMainFrame" -> isForMainFrame
                    "getMethod" -> "GET"
                    "getRequestHeaders" -> emptyMap<String, String>()
                    "hasGesture" -> false
                    "isRedirect" -> false
                    else -> null
                }
            }
        return Proxy.newProxyInstance(
            WebResourceRequest::class.java.classLoader,
            arrayOf(WebResourceRequest::class.java),
            handler,
        ) as WebResourceRequest
    }
}
