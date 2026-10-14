package com.cylonid.nativealpha.ui.webapp

import android.content.Context
import android.view.View
import androidx.test.core.app.ApplicationProvider
import com.cylonid.nativealpha.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WebViewClassicPopupMenuTest {
    private lateinit var context: Context
    private lateinit var anchorView: View

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        context.setTheme(R.style.AppTheme)
        anchorView = View(context)
    }

    @Test
    fun testShowTruncatesLongUrl() {
        val longUrl = "https://verylongdomainnameexamplethatiswaymorethanthirtytwocharacters.com/path"
        val dummyListener =
            object : WebViewMenuListener {
                override fun onBack() {}

                override fun onForward() {}

                override fun onReload() {}

                override fun onShare() {}

                override fun onCopyUrl() {}

                override fun onMainMenu() {}

                override fun onFallbackContextMenu() {}

                override fun onCloseWebApp() {}
            }

        val popup =
            WebViewClassicPopupMenu.show(
                context = context,
                anchor = anchorView,
                url = longUrl,
                canGoForward = false,
                listener = dummyListener,
            )

        val headerItem = popup.menu.findItem(R.id.cmUrl)
        val expected = longUrl.take(32) + "…"
        assertEquals(expected, headerItem?.title?.toString())
        assertFalse(popup.menu.findItem(R.id.cmItemForward)?.isVisible == true)
    }

    @Test
    fun testShowEnablesForwardWhenCanGoForward() {
        val dummyListener =
            object : WebViewMenuListener {
                override fun onBack() {}

                override fun onForward() {}

                override fun onReload() {}

                override fun onShare() {}

                override fun onCopyUrl() {}

                override fun onMainMenu() {}

                override fun onFallbackContextMenu() {}

                override fun onCloseWebApp() {}
            }

        val popup =
            WebViewClassicPopupMenu.show(
                context = context,
                anchor = anchorView,
                url = "https://example.com",
                canGoForward = true,
                listener = dummyListener,
            )

        assertTrue(popup.menu.findItem(R.id.cmItemForward)?.isVisible == true)
    }

    @Test
    fun testMenuItemClickDispatchesToListener() {
        var backClicked = false
        var forwardClicked = false
        var reloadClicked = false
        var shareClicked = false
        var copyClicked = false
        var mainMenuClicked = false
        var fallbackClicked = false
        var closeClicked = false

        val listener =
            object : WebViewMenuListener {
                override fun onBack() {
                    backClicked = true
                }

                override fun onForward() {
                    forwardClicked = true
                }

                override fun onReload() {
                    reloadClicked = true
                }

                override fun onShare() {
                    shareClicked = true
                }

                override fun onCopyUrl() {
                    copyClicked = true
                }

                override fun onMainMenu() {
                    mainMenuClicked = true
                }

                override fun onFallbackContextMenu() {
                    fallbackClicked = true
                }

                override fun onCloseWebApp() {
                    closeClicked = true
                }
            }

        val popup =
            WebViewClassicPopupMenu.show(
                context = context,
                anchor = anchorView,
                url = "https://example.com",
                canGoForward = true,
                listener = listener,
            )

        popup.menu.performIdentifierAction(R.id.cmItemBack, 0)
        assertTrue("onBack should be called", backClicked)

        popup.menu.performIdentifierAction(R.id.cmItemForward, 0)
        assertTrue("onForward should be called", forwardClicked)

        popup.menu.performIdentifierAction(R.id.cmItemReload, 0)
        assertTrue("onReload should be called", reloadClicked)

        popup.menu.performIdentifierAction(R.id.cmItemShareUrl, 0)
        assertTrue("onShare should be called", shareClicked)

        popup.menu.performIdentifierAction(R.id.cmMainMenu, 0)
        assertTrue("onMainMenu should be called", mainMenuClicked)

        // Submenu items
        val moreMenu = popup.menu.findItem(R.id.cmItemMore)?.subMenu
        moreMenu?.performIdentifierAction(R.id.cmItemCopyUrl, 0)
        assertTrue("onCopyUrl should be called", copyClicked)

        moreMenu?.performIdentifierAction(R.id.cmFallbackContextmenuTemp, 0)
        assertTrue("onFallbackContextMenu should be called", fallbackClicked)

        moreMenu?.performIdentifierAction(R.id.cmItemCloseWebApp, 0)
        assertTrue("onCloseWebApp should be called", closeClicked)
    }
}
