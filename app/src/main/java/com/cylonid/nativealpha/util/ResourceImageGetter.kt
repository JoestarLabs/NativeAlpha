package com.cylonid.nativealpha.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.Html.ImageGetter
import androidx.core.content.ContextCompat

class ResourceImageGetter(
    private val context: Context,
) : ImageGetter {
    override fun getDrawable(source: String): Drawable? {
        val resId = context.resIdByName(source, "drawable")
        val res = ContextCompat.getDrawable(context, resId) ?: return null

        res.setBounds(0, 0, res.intrinsicWidth, res.intrinsicHeight)
        return res
    }
}
