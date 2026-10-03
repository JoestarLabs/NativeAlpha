package com.cylonid.nativealpha

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Html
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.cylonid.nativealpha.helper.AdblockLifecycleHelper
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.ui.theme.NativeAlphaTheme
import com.cylonid.nativealpha.ui.webapplist.WebAppListScreen
import com.cylonid.nativealpha.ui.webapplist.WebAppListViewModel
import com.cylonid.nativealpha.util.Const
import com.cylonid.nativealpha.util.EntryPointUtils.entryPointReached
import com.cylonid.nativealpha.util.WebViewLauncher
import io.github.edsuns.adfilter.AdFilter

class MainActivity : AppCompatActivity() {
    private val viewModel: WebAppListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        entryPointReached(this)

        setContent {
            val isDynamicColor by remember {
                mutableStateOf(
                    runCatching { DataManager.getInstance().settings.isDynamicColor }.getOrDefault(false),
                )
            }

            NativeAlphaTheme(dynamicColor = isDynamicColor) {
                WebAppListScreen(
                    viewModel = viewModel,
                    onNavigateToSettings = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onNavigateToAbout = {
                        startActivity(Intent(this, AboutActivity::class.java))
                    },
                    onOpenWebApp = { webApp ->
                        WebViewLauncher.startWebView(webApp, this)
                    },
                    onOpenWebAppSettings = { webApp ->
                        val intent =
                            Intent(this, WebAppSettingsActivity::class.java).apply {
                                putExtra(Const.INTENT_WEBAPPID, webApp.ID)
                                action = Intent.ACTION_VIEW
                            }
                        startActivity(intent)
                    },
                    onRequestCreateShortcut = { webApp ->
                        val frag = ShortcutDialogFragment.newInstance(webApp)
                        frag.show(supportFragmentManager, "SCFetcher-" + webApp.ID)
                    },
                )
            }
        }

        AdblockLifecycleHelper(this).trySyncOperation({ AdFilter.create(applicationContext) })
    }

    override fun onResume() {
        super.onResume()
        DataManager.getInstance().loadAppData()
        viewModel.loadWebApps()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getBooleanExtra(Const.INTENT_BACKUP_RESTORED, false)) {
            viewModel.loadWebApps()
            buildImportSuccessDialog()
            intent.putExtra(Const.INTENT_BACKUP_RESTORED, false)
            intent.putExtra(Const.INTENT_REFRESH_NEW_THEME, false)
        }
        if (intent.getBooleanExtra(Const.INTENT_WEBAPP_CHANGED, false)) {
            viewModel.loadWebApps()
            intent.putExtra(Const.INTENT_WEBAPP_CHANGED, false)
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun updateWebAppList() {
        viewModel.loadWebApps()
    }

    private fun buildImportSuccessDialog() {
        val message =
            """
            ${getString(R.string.import_success_dialog_txt2)}

            ${getString(R.string.import_success_dialog_txt3)}
            """.trimIndent()

        AlertDialog
            .Builder(this)
            .setMessage(message)
            .setCancelable(false)
            .setTitle(
                getString(
                    R.string.import_success,
                    DataManager.getInstance().activeWebsitesCount,
                ),
            ).setPositiveButton(getString(R.string.ok)) { _: DialogInterface?, _: Int ->
                val webapps = DataManager.getInstance().activeWebsites
                for (i in webapps.indices.reversed()) {
                    val webapp = webapps[i]
                    val msg =
                        Html.fromHtml(
                            getString(R.string.restore_shortcut, webapp.title),
                            Html.FROM_HTML_MODE_COMPACT,
                        )
                    AlertDialog
                        .Builder(this)
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok) { _: DialogInterface?, _: Int ->
                            val frag = ShortcutDialogFragment.newInstance(webapp)
                            frag.show(supportFragmentManager, "SCFetcher-" + webapp.ID)
                        }.setNegativeButton(R.string.cancel) { _: DialogInterface?, _: Int -> }
                        .create()
                        .show()
                }
            }.setNegativeButton(getString(R.string.cancel)) { _: DialogInterface?, _: Int -> }
            .create()
            .show()
    }
}
