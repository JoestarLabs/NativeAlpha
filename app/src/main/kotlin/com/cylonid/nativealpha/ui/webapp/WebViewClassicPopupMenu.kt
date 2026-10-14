package com.cylonid.nativealpha.ui.webapp

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.forEach
import com.cylonid.nativealpha.R

object WebViewClassicPopupMenu {
    @JvmStatic
    fun show(
        context: Context,
        anchor: View,
        url: String,
        canGoForward: Boolean,
        listener: WebViewMenuListener,
    ): PopupMenu {
        val popup = PopupMenu(context, anchor, Gravity.CENTER)
        popup.setForceShowIcon(true)
        popup.menuInflater.inflate(R.menu.wv_context_menu, popup.menu)
        tintMenuIcons(context, popup.menu)

        val menu = popup.menu
        val displayTitle = url.ifBlank { "Native Alpha" }
        val truncated = if (displayTitle.length < 32) displayTitle else displayTitle.take(32) + "…"
        menu.findItem(R.id.cmUrl)?.title = truncated

        if (canGoForward) {
            menu.findItem(R.id.cmItemForward)?.isVisible = true
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.cmItemBack -> {
                    listener.onBack()
                    true
                }
                R.id.cmItemForward -> {
                    listener.onForward()
                    true
                }
                R.id.cmItemReload -> {
                    listener.onReload()
                    true
                }
                R.id.cmItemShareUrl -> {
                    listener.onShare()
                    true
                }
                R.id.cmMainMenu -> {
                    listener.onMainMenu()
                    true
                }
                R.id.cmItemCopyUrl -> {
                    listener.onCopyUrl()
                    true
                }
                R.id.cmFallbackContextmenuTemp -> {
                    listener.onFallbackContextMenu()
                    true
                }
                R.id.cmItemCloseWebApp -> {
                    listener.onCloseWebApp()
                    true
                }
                else -> false
            }
        }

        popup.show()
        return popup
    }

    private fun tintMenuIcons(
        context: Context,
        menu: Menu,
    ) {
        val typedValue = TypedValue()
        val theme = context.theme
        val resolved =
            theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true) ||
                theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)

        if (!resolved) return
        val tintColor = typedValue.data

        fun tint(m: Menu) {
            m.forEach { item ->
                item.icon?.let { icon ->
                    val wrapped = DrawableCompat.wrap(icon.mutate())
                    DrawableCompat.setTint(wrapped, tintColor)
                    item.icon = wrapped
                }
                if (item.hasSubMenu()) {
                    item.subMenu?.let { tint(it) }
                }
            }
        }

        tint(menu)
    }
}
