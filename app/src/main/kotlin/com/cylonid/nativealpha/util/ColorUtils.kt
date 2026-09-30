package com.cylonid.nativealpha.util

import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorRes

object ColorUtils {
    @JvmStatic
    @ColorRes
    fun getColorResFromThemeAttr(
        context: Context,
        @AttrRes resId: Int,
        @ColorRes fallback: Int,
    ): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        var colorResId = fallback

        val success =
            theme.resolveAttribute(
                resId,
                typedValue,
                true,
            )
        if (success) {
            colorResId = typedValue.resourceId
        }
        return colorResId
    }
}
