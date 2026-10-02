package com.cylonid.nativealpha.ui.webapp

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Process
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AppShortcut
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Cookie
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Javascript
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cylonid.nativealpha.MainActivity
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.ShortcutDialogFragment
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.model.WebApp
import com.cylonid.nativealpha.ui.components.RoundedCardContainer
import com.cylonid.nativealpha.ui.components.SettingsActionItem
import com.cylonid.nativealpha.ui.components.SettingsSectionHeader
import com.cylonid.nativealpha.ui.components.SettingsSwitchItem
import com.cylonid.nativealpha.util.Const
import com.cylonid.nativealpha.util.ProcessUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WebAppSettingsScreen(
    webappId: Int,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val dataManager = remember { DataManager.getInstance() }
    val isGlobalWebApp = webappId == dataManager.settings.globalWebApp.ID

    val initialWebApp =
        remember {
            if (isGlobalWebApp) {
                dataManager.settings.globalWebApp
            } else {
                dataManager.getWebAppIgnoringGlobalOverride(webappId, true)
            }
        }

    if (initialWebApp == null) {
        onNavigateBack()
        return
    }

    var webapp by remember { mutableStateOf(WebApp(initialWebApp)) }

    fun saveAndFinish() {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        if (isGlobalWebApp) {
            if (activityManager != null) {
                ProcessUtils.closeAllWebAppsAndProcesses(activityManager)
            }
            dataManager.settings.globalWebApp = webapp
            dataManager.saveGlobalSettings()
        } else {
            if (activityManager != null) {
                for (task in activityManager.appTasks) {
                    val id = task.taskInfo?.baseIntent?.getIntExtra(Const.INTENT_WEBAPPID, -1) ?: -1
                    if (id == webappId) task.finishAndRemoveTask()
                }
                for (processInfo in activityManager.runningAppProcesses) {
                    if (processInfo.processName.contains("web_sandbox_" + webapp.containerId)) {
                        Process.killProcess(processInfo.pid)
                    }
                }
            }
            dataManager.replaceWebApp(webapp)
        }

        val intent =
            Intent(context, MainActivity::class.java).apply {
                putExtra(Const.INTENT_WEBAPP_CHANGED, true)
            }
        (context as? Activity)?.finish()
        context.startActivity(intent)
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val screenTitle =
        if (isGlobalWebApp) {
            stringResource(R.string.global_web_app_settings)
        } else {
            webapp.title.ifBlank { stringResource(R.string.web_app_settings) }
        }

    val screenSubtitle =
        if (isGlobalWebApp) {
            "Defaults applied to all web apps"
        } else {
            webapp.baseUrl
        }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = {
                    Text(
                        text = screenTitle,
                        fontWeight = FontWeight.Bold,
                    )
                },
                subtitle = {
                    Text(
                        text = screenSubtitle,
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { saveAndFinish() },
                        shapes =
                            IconButtonDefaults.shapes(
                                shape = CircleShape,
                                pressedShape = RoundedCornerShape(percent = 18),
                            ),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.cancel),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding(),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Global Info Banner ──
            if (isGlobalWebApp) {
                Card(
                    colors =
                        CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceBright,
                        ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp),
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text =
                                stringResource(
                                    R.string.these_settings_are_applied_globally_and_override_app_specific_settings,
                                ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            } else {
                // ── App Identity Section ──
                SettingsSectionHeader("Web App Details")

                RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                    ) {
                        OutlinedTextField(
                            value = webapp.title,
                            onValueChange = { newTitle ->
                                webapp = webapp.copy().apply { title = newTitle }
                            },
                            label = { Text("Web App Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = webapp.baseUrl,
                            onValueChange = { newUrl ->
                                webapp = webapp.copy().apply { baseUrl = newUrl }
                            },
                            label = { Text("Base URL") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    SettingsActionItem(
                        title = "Re-create Shortcut",
                        description = "Add or update home screen launcher shortcut",
                        icon = Icons.Rounded.AppShortcut,
                        onClick = {
                            val activity = context as? AppCompatActivity
                            if (activity != null) {
                                val frag = ShortcutDialogFragment.newInstance(webapp)
                                frag.show(activity.supportFragmentManager, "SCFetcher-${webapp.ID}")
                            }
                        },
                    )
                }

                Spacer(Modifier.height(16.dp))

                // ── Override Switch ──
                SettingsSectionHeader("Global Configuration Override")

                RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
                    SettingsSwitchItem(
                        title = stringResource(R.string.override_global_settings),
                        description = "Customize behavior specifically for this web app instead of following global defaults",
                        checked = webapp.isOverrideGlobalSettings,
                        icon = Icons.Rounded.Tune,
                        onCheckedChange = { isChecked ->
                            webapp = webapp.copy().apply { isOverrideGlobalSettings = isChecked }
                        },
                    )
                }

                Spacer(Modifier.height(16.dp))
            }

            val isSettingsEnabled = isGlobalWebApp || webapp.isOverrideGlobalSettings

            // ── Browsing & Display ──
            SettingsSectionHeader("Browsing & Display")

            RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
                SettingsSwitchItem(
                    title = "Allow JavaScript",
                    description = "Enable JavaScript execution for interactive sites",
                    checked = webapp.isAllowJs,
                    icon = Icons.Rounded.Javascript,
                    enabled = isSettingsEnabled,
                    onCheckedChange = { isChecked ->
                        webapp =
                            webapp.copy().apply {
                                isAllowJs = isChecked
                                if (!isChecked) {
                                    isRequestDesktop = false
                                    isUseAdblock = false
                                }
                            }
                    },
                )

                SettingsSwitchItem(
                    title = "Desktop Site",
                    description = "Request desktop version of web pages",
                    checked = webapp.isRequestDesktop,
                    icon = Icons.Rounded.DesktopWindows,
                    enabled = isSettingsEnabled && webapp.isAllowJs,
                    onCheckedChange = { isChecked ->
                        webapp =
                            webapp.copy().apply {
                                isRequestDesktop = isChecked
                                if (isChecked) {
                                    isUseCustomUserAgent = false
                                }
                            }
                    },
                )

                SettingsSwitchItem(
                    title = stringResource(R.string.activate_two_finger_zoom),
                    description = "Allow pinch gestures to zoom web content",
                    checked = webapp.isEnableZooming,
                    icon = Icons.Rounded.ZoomIn,
                    enabled = isSettingsEnabled,
                    onCheckedChange = { isChecked ->
                        webapp = webapp.copy().apply { isEnableZooming = isChecked }
                    },
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Privacy & Adblocking ──
            SettingsSectionHeader("Privacy & Security")

            RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
                SettingsSwitchItem(
                    title = "Allow Cookies",
                    description = "Store first-party website session cookies",
                    checked = webapp.isAllowCookies,
                    icon = Icons.Rounded.Cookie,
                    enabled = isSettingsEnabled,
                    onCheckedChange = { isChecked ->
                        webapp =
                            webapp.copy().apply {
                                isAllowCookies = isChecked
                                if (!isChecked) {
                                    isAllowThirdPartyCookies = false
                                }
                            }
                    },
                )

                SettingsSwitchItem(
                    title = "Allow Third-Party Cookies",
                    description = "Allow cookies from external cross-site domains",
                    checked = webapp.isAllowThirdPartyCookies,
                    icon = Icons.Rounded.Cookie,
                    enabled = isSettingsEnabled && webapp.isAllowCookies,
                    onCheckedChange = { isChecked ->
                        webapp = webapp.copy().apply { isAllowThirdPartyCookies = isChecked }
                    },
                )

                SettingsSwitchItem(
                    title = stringResource(R.string.enable_adblock_experimental),
                    description = "Filter ads and trackers using active adblock provider rules",
                    checked = webapp.isUseAdblock,
                    icon = Icons.Rounded.Block,
                    enabled = isSettingsEnabled && webapp.isAllowJs,
                    onCheckedChange = { isChecked ->
                        webapp = webapp.copy().apply { isUseAdblock = isChecked }
                    },
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Advanced Behavior ──
            SettingsSectionHeader("Media & Controls")

            RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
                SettingsSwitchItem(
                    title = "Background Media Playback",
                    description = "Continue playing audio when switching apps or locking the screen",
                    checked = webapp.isAllowMediaPlaybackInBackground,
                    icon = Icons.Rounded.MusicNote,
                    enabled = isSettingsEnabled,
                    onCheckedChange = { isChecked ->
                        webapp =
                            webapp.copy().apply {
                                isAllowMediaPlaybackInBackground = isChecked
                            }
                    },
                )

                SettingsSwitchItem(
                    title = "Standard Context Menu",
                    description = "Always use the system default text selection & context menu",
                    checked = webapp.alwaysUseFallbackContextMenu,
                    icon = Icons.Rounded.Menu,
                    enabled = isSettingsEnabled,
                    onCheckedChange = { isChecked ->
                        webapp =
                            webapp.copy().apply {
                                alwaysUseFallbackContextMenu = isChecked
                            }
                    },
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
