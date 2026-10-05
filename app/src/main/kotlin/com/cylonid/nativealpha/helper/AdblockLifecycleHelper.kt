package com.cylonid.nativealpha.helper

import android.app.Activity
import android.content.DialogInterface
import androidx.appcompat.app.AlertDialog
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.model.DataManager
import timber.log.Timber

class AdblockLifecycleHelper(
    private val activity: Activity,
) {
    fun trySyncOperation(callback: AdblockLifecycleCallback) {
        if (DataManager.getInstance().hasAdblockCrashed) {
            showWarningDialog()
            DataManager.getInstance().hasAdblockCrashed = false
        }
        try {
            callback.execute()
            DataManager.getInstance().hasAdblockCrashed = false
        } catch (t: Throwable) {
            Timber.e(t, "Adblock sync operation failed")
            DataManager.getInstance().hasAdblockCrashed = true
        }
    }

    fun beforeAdblockOperation(callback: AdblockLifecycleCallback) {
        if (DataManager.getInstance().hasAdblockCrashed) {
            showWarningDialog()
            DataManager.getInstance().hasAdblockCrashed = false
        }
        try {
            callback.execute()
        } catch (t: Throwable) {
            Timber.e(t, "Adblock beforeAdblockOperation failed")
            DataManager.getInstance().hasAdblockCrashed = true
        }
    }

    fun afterAdblockOperation() {
        DataManager.getInstance().hasAdblockCrashed = false
    }

    private fun showWarningDialog() {
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }
        try {
            AlertDialog
                .Builder(activity)
                .setMessage(activity.getString(R.string.adblock_warning_text))
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(activity.getString(R.string.warning))
                .setPositiveButton(activity.getString(R.string.ok)) { _: DialogInterface?, _: Int ->
                    DataManager.getInstance().apply {
                        settings.globalWebApp.adBlockSettings.clear()
                        saveGlobalSettings()
                    }
                }.setNegativeButton(activity.getString(R.string.cancel)) { _: DialogInterface?, _: Int -> }
                .create()
                .show()
        } catch (t: Throwable) {
            Timber.e(t, "Error displaying adblock warning dialog")
        }
    }

    fun interface AdblockLifecycleCallback {
        fun execute()
    }
}
