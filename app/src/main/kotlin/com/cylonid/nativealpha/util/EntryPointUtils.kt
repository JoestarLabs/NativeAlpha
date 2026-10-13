package com.cylonid.nativealpha.util

import android.app.Activity
import com.cylonid.nativealpha.model.DataManager

object EntryPointUtils {
    @JvmStatic
    fun entryPointReached(a: Activity) {
        DataManager.getInstance().loadAppData()
    }
}
