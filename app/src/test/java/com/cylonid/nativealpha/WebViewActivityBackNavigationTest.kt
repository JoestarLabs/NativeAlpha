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

        val subframePdfRequest =
            createWebResourceRequest(
                uri = Uri.parse("https://example.com/document.pdf"),
                isForMainFrame = false,
            )

        val wvField = WebViewActivity::class.java.getDeclaredField("wv")
        wvField.isAccessible = true
        val wv = wvField.get(activity) as WebView

        val result = client.shouldOverrideUrlLoading(wv, subframePdfRequest)
        assertFalse("Subframes (including subframe PDFs) should not be overridden", result)
    }

    @Test
    fun testShouldOverrideUrlLoadingRoutesGetRequests() {
        val activity = createActivity()
        val customBrowserField =
            activity.javaClass.declaredClasses.firstOrNull { it.simpleName == "CustomBrowser" }
                ?: error("CustomBrowser class not found")

        val constructor = customBrowserField.getDeclaredConstructor(WebViewActivity::class.java)
        constructor.isAccessible = true
        val client = constructor.newInstance(activity) as WebViewClient

        val mainFrameGetRequest =
            createWebResourceRequest(
                uri = Uri.parse("https://example.com/another-page"),
                isForMainFrame = true,
                method = "GET",
            )

        val wvField = WebViewActivity::class.java.getDeclaredField("wv")
        wvField.isAccessible = true
        val wv = wvField.get(activity) as WebView

        val resultGet = client.shouldOverrideUrlLoading(wv, mainFrameGetRequest)
        assertTrue("Main-frame HTTP(S) GET navigation should be routed through loadURL", resultGet)

        val mainFramePostRequest =
            createWebResourceRequest(
                uri = Uri.parse("https://example.com/submit-form"),
                isForMainFrame = true,
                method = "POST",
            )
        val resultPost = client.shouldOverrideUrlLoading(wv, mainFramePostRequest)
        assertFalse("Non-GET navigations should return false to preserve POST payload", resultPost)
    }

    @Test
    fun testUrlSchemeHelpers() {
        // isHttpUrl
        assertTrue(WebViewActivity.isHttpUrl("http://example.com"))
        assertTrue(WebViewActivity.isHttpUrl("HTTP://example.com/path"))
        assertTrue(WebViewActivity.isHttpUrl("Http://example.com?query=https://other.com"))
        assertFalse(WebViewActivity.isHttpUrl("https://example.com"))
        assertFalse(WebViewActivity.isHttpUrl("file:///doc.pdf"))
        assertFalse(WebViewActivity.isHttpUrl(null))

        // isHttpOrHttpsUrl
        assertTrue(WebViewActivity.isHttpOrHttpsUrl("http://example.com"))
        assertTrue(WebViewActivity.isHttpOrHttpsUrl("HTTP://example.com"))
        assertTrue(WebViewActivity.isHttpOrHttpsUrl("https://example.com"))
        assertTrue(WebViewActivity.isHttpOrHttpsUrl("HTTPS://example.com"))
        assertFalse(WebViewActivity.isHttpOrHttpsUrl("ftp://example.com"))
        assertFalse(WebViewActivity.isHttpOrHttpsUrl("intent://example.com"))
        assertFalse(WebViewActivity.isHttpOrHttpsUrl(null))

        // isSupportedPdfScheme
        assertTrue(WebViewActivity.isSupportedPdfScheme("http://example.com/doc.pdf"))
        assertTrue(WebViewActivity.isSupportedPdfScheme("HTTP://example.com/doc.pdf"))
        assertTrue(WebViewActivity.isSupportedPdfScheme("https://example.com/doc.pdf"))
        assertTrue(WebViewActivity.isSupportedPdfScheme("HTTPS://example.com/doc.pdf"))
        assertTrue(WebViewActivity.isSupportedPdfScheme("file:///sdcard/doc.pdf"))
        assertTrue(WebViewActivity.isSupportedPdfScheme("content://media/external/doc.pdf"))
        assertFalse(WebViewActivity.isSupportedPdfScheme("intent://example.com/doc.pdf#Intent;"))
        assertFalse(WebViewActivity.isSupportedPdfScheme("blob:uuid-1234"))
        assertFalse(WebViewActivity.isSupportedPdfScheme("tel:12345.pdf"))
        assertFalse(WebViewActivity.isSupportedPdfScheme(null))
    }

    @Test
    fun testShouldOverrideUrlLoadingIgnoresUnsupportedPdfScheme() {
        val activity = createActivity()
        val customBrowserField =
            activity.javaClass.declaredClasses.firstOrNull { it.simpleName == "CustomBrowser" }
                ?: error("CustomBrowser class not found")

        val constructor = customBrowserField.getDeclaredConstructor(WebViewActivity::class.java)
        constructor.isAccessible = true
        val client = constructor.newInstance(activity) as WebViewClient

        val unsupportedPdfRequest =
            createWebResourceRequest(
                uri = Uri.parse("intent://example.com/document.pdf#Intent;action=VIEW;scheme=example;end"),
                isForMainFrame = true,
                method = "GET",
            )

        val wvField = WebViewActivity::class.java.getDeclaredField("wv")
        wvField.isAccessible = true
        val wv = wvField.get(activity) as WebView

        val result = client.shouldOverrideUrlLoading(wv, unsupportedPdfRequest)
        assertFalse("Unsupported schemes ending with .pdf should not be intercepted as built-in PDF", result)
    }

    private fun createWebResourceRequest(
        uri: Uri,
        isForMainFrame: Boolean,
        method: String = "GET",
    ): WebResourceRequest {
        val handler =
            InvocationHandler { _, m, _ ->
                when (m.name) {
                    "getUrl" -> uri
                    "isForMainFrame" -> isForMainFrame
                    "getMethod" -> method
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
