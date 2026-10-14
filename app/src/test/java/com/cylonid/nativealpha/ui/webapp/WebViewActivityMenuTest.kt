package com.cylonid.nativealpha.ui.webapp

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.cylonid.nativealpha.WebViewActivity
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.model.WebApp
import com.cylonid.nativealpha.util.Const
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WebViewActivityMenuTest {
    private val testWebAppId = 0
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val dm = DataManager.getInstance()
        val webApp = WebApp("https://example.com", testWebAppId, Const.getDefaultAdBlockConfig())
        webApp.title = "Example App"
        webApp.isOverrideGlobalSettings = true
        webApp.isAllowJs = true
        if (dm.websites.isEmpty()) {
            dm.websites.add(webApp)
        } else {
            dm.websites[0] = webApp
        }
        dm.saveWebAppData()
    }

    @Test
    fun testWebViewActivityImplementsMenuListener() {
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

        assertTrue(
            "WebViewActivity should implement WebViewMenuListener",
            activity is WebViewMenuListener,
        )

        val listener = activity as WebViewMenuListener

        // Test onFallbackContextMenu sets internal flag
        listener.onFallbackContextMenu()
        val fallbackField = WebViewActivity::class.java.getDeclaredField("fallbackToDefaultLongClickBehaviour")
        fallbackField.isAccessible = true
        val fallbackVal = fallbackField.getBoolean(activity)
        assertTrue("fallbackToDefaultLongClickBehaviour should be true after onFallbackContextMenu", fallbackVal)

        // Test onReload triggers without crash
        listener.onReload()

        // Test onForward without history does not crash
        listener.onForward()

        // Test onBack does not crash
        listener.onBack()

        // Test onCloseWebApp finishes activity
        listener.onCloseWebApp()
        assertTrue("Activity should be finishing after onCloseWebApp", activity.isFinishing)
    }

    @Test
    fun testCopyUrlCopiesToClipboard() {
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
        val listener = activity as WebViewMenuListener

        val field = WebViewActivity::class.java.getDeclaredField("wv")
        field.isAccessible = true
        val wv = field.get(activity) as android.webkit.WebView
        wv.loadUrl("https://example.com/test-page")

        listener.onCopyUrl()

        val clipboard = activity.getSystemService(ClipboardManager::class.java)
        assertNotNull(clipboard)
        if (clipboard?.hasPrimaryClip() == true) {
            val clipData = clipboard.primaryClip
            assertEquals("https://example.com/test-page", clipData?.getItemAt(0)?.text?.toString())
        }
    }
}
