package com.cylonid.nativealpha.util

import java.util.Locale

object LocaleUtils {
    @JvmStatic
    val fileEnding: String
        get() =
            when (Locale.getDefault().language) {
                "de" -> "de"
                else -> "en"
            }
}
