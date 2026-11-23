package com.cylonid.nativealpha.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.cylonid.nativealpha.MainActivity
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.util.Const
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

object SettingsBackupCoordinator {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val operationInProgress = AtomicBoolean(false)

    var isRestoring by mutableStateOf(false)
        private set

    fun exportBackup(
        context: Context,
        uri: Uri,
        onComplete: (Boolean) -> Unit,
    ) {
        if (!operationInProgress.compareAndSet(false, true)) return
        val dataManager = DataManager.getInstance()
        isRestoring = true
        scope.launch {
            val success =
                try {
                    withContext(Dispatchers.IO) {
                        dataManager.saveGlobalSettings()
                        dataManager.saveSharedPreferencesToFile(uri)
                    }
                } finally {
                    operationInProgress.set(false)
                    isRestoring = false
                }
            onComplete(success)
        }
    }

    fun restoreBackup(
        context: Context,
        uri: Uri,
        onFailure: () -> Unit,
        onSuccess: () -> Unit = {},
    ) {
        if (!operationInProgress.compareAndSet(false, true)) return
        val appContext = context.applicationContext ?: context
        val dataManager = DataManager.getInstance()
        isRestoring = true
        scope.launch {
            val success =
                try {
                    withContext(Dispatchers.IO) {
                        val loaded = dataManager.loadSharedPreferencesFromFile(uri)
                        if (loaded) {
                            dataManager.loadAppData()
                        }
                        loaded
                    }
                } catch (t: Throwable) {
                    operationInProgress.set(false)
                    isRestoring = false
                    throw t
                }
            if (!success) {
                operationInProgress.set(false)
                isRestoring = false
                onFailure()
                return@launch
            }

            try {
                WebStorage.getInstance().deleteAllData()
                CookieManager.getInstance().removeAllCookies(null)
                val intent =
                    Intent(appContext, MainActivity::class.java).apply {
                        putExtra(Const.INTENT_BACKUP_RESTORED, true)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                appContext.startActivity(intent)
            } finally {
                operationInProgress.set(false)
                isRestoring = false
            }
            onSuccess()
        }
    }
}
