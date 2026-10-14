package com.cylonid.nativealpha.ui.webapp

import android.view.View
import androidx.fragment.app.FragmentActivity
import com.cylonid.nativealpha.R
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WebViewMenuLauncherTest {
    private lateinit var activity: FragmentActivity
    private lateinit var anchorView: View
    private lateinit var dummyListener: WebViewMenuListener

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(FragmentActivity::class.java).setup().get()
        activity.setTheme(R.style.AppTheme)
        anchorView = View(activity)
        activity.setContentView(anchorView)

        dummyListener =
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
    }

    @Test
    fun testShowBottomSheetDialogFragment() {
        WebViewMenuLauncher.show(
            activity = activity,
            style = WebAppMenuStyle.BOTTOM_SHEET,
            title = "Test Title",
            url = "https://example.com",
            canGoBack = true,
            canGoForward = false,
            webApp = null,
            anchorView = anchorView,
            listener = dummyListener,
        )

        activity.supportFragmentManager.executePendingTransactions()
        val fragment = activity.supportFragmentManager.findFragmentByTag("WebViewMenuBottomSheetDialogFragment")
        assertNotNull("BottomSheet dialog fragment should be present", fragment)
        assertTrue(fragment is WebViewMenuBottomSheetDialogFragment)

        WebViewMenuLauncher.dismiss(activity)
        activity.supportFragmentManager.executePendingTransactions()
        val afterDismiss = activity.supportFragmentManager.findFragmentByTag("WebViewMenuBottomSheetDialogFragment")
        assertNull("BottomSheet dialog fragment should be dismissed", afterDismiss)
    }

    @Test
    fun testShowFloatingDialogFragment() {
        WebViewMenuLauncher.show(
            activity = activity,
            style = WebAppMenuStyle.FLOATING_DIALOG,
            title = "Test Title",
            url = "https://example.com",
            canGoBack = true,
            canGoForward = true,
            webApp = null,
            anchorView = anchorView,
            listener = dummyListener,
        )

        activity.supportFragmentManager.executePendingTransactions()
        val fragment = activity.supportFragmentManager.findFragmentByTag("WebViewMenuDialogFragment")
        assertNotNull("Floating dialog fragment should be present", fragment)
        assertTrue(fragment is WebViewMenuDialogFragment)

        WebViewMenuLauncher.dismiss(activity)
        activity.supportFragmentManager.executePendingTransactions()
        val afterDismiss = activity.supportFragmentManager.findFragmentByTag("WebViewMenuDialogFragment")
        assertNull("Floating dialog fragment should be dismissed", afterDismiss)
    }

    @Test
    fun testShowClassicDropdownAndDismiss() {
        WebViewMenuLauncher.show(
            activity = activity,
            style = WebAppMenuStyle.CLASSIC_DROPDOWN,
            title = "Test Title",
            url = "https://example.com",
            canGoBack = true,
            canGoForward = false,
            webApp = null,
            anchorView = anchorView,
            listener = dummyListener,
        )

        // Dismissing should not crash and should clean up references
        WebViewMenuLauncher.dismiss(activity)
    }

    @Test
    fun testDismissWhenNothingShownDoesNotCrash() {
        // Calling dismiss without having shown anything should be completely safe
        WebViewMenuLauncher.dismiss(activity)
    }
}
