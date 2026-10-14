package com.cylonid.nativealpha.ui.webapp

import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import com.cylonid.nativealpha.model.WebApp

object WebViewMenuLauncher {
    private var currentPopupMenu: PopupMenu? = null

    @JvmStatic
    fun show(
        activity: FragmentActivity,
        style: WebAppMenuStyle,
        title: String,
        url: String,
        canGoBack: Boolean,
        canGoForward: Boolean,
        webApp: WebApp?,
        anchorView: View?,
        listener: WebViewMenuListener,
    ) {
        dismiss(activity)
        when (style) {
            WebAppMenuStyle.BOTTOM_SHEET -> {
                WebViewMenuBottomSheetDialogFragment.show(
                    activity.supportFragmentManager,
                    title,
                    url,
                    canGoBack,
                    canGoForward,
                    webApp,
                    listener,
                )
            }
            WebAppMenuStyle.FLOATING_DIALOG -> {
                WebViewMenuDialogFragment.show(
                    activity.supportFragmentManager,
                    title,
                    url,
                    canGoBack,
                    canGoForward,
                    webApp,
                    listener,
                )
            }
            WebAppMenuStyle.CLASSIC_DROPDOWN -> {
                val anchor = anchorView ?: activity.window.decorView
                currentPopupMenu =
                    WebViewClassicPopupMenu.show(
                        activity,
                        anchor,
                        url,
                        canGoForward,
                        listener,
                    )
            }
        }
    }

    @JvmStatic
    fun dismiss(activity: FragmentActivity) {
        currentPopupMenu?.dismiss()
        currentPopupMenu = null

        val fm = activity.supportFragmentManager
        (fm.findFragmentByTag("WebViewMenuBottomSheetDialogFragment") as? DialogFragment)
            ?.dismissAllowingStateLoss()
        (fm.findFragmentByTag("WebViewMenuDialogFragment") as? DialogFragment)
            ?.dismissAllowingStateLoss()
    }
}
