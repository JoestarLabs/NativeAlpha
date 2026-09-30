package com.cylonid.nativealpha.util

import android.content.Context
import android.content.pm.ShortcutManager
import com.cylonid.nativealpha.R

object ShortcutIconUtils {
    @JvmStatic
    fun deleteShortcuts(
        removableWebAppIds: List<Int>,
        context: Context,
    ) {
        val manager =
            context.getSystemService(
                ShortcutManager::class.java,
            )
        for (info in manager.pinnedShortcuts) {
            val id =
                info.intent!!
                    .getIntExtra(Const.INTENT_WEBAPPID, -1)
            if (removableWebAppIds.contains(id)) {
                manager.disableShortcuts(
                    listOf(info.id),
                    context.getString(R.string.webapp_already_deleted),
                )
            }
        }
    }

    @JvmStatic
    fun getWidthFromIcon(sizeString: String): Int {
        var xIndex = sizeString.indexOf("x")
        if (xIndex == -1) xIndex = sizeString.indexOf("×")
        if (xIndex == -1) xIndex = sizeString.indexOf("*")

        if (xIndex == -1) return 1
        val width = sizeString.substring(0, xIndex)

        return width.toInt()
    }
}
