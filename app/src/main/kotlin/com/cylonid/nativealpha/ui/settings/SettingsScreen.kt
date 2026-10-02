package com.cylonid.nativealpha.ui.settings

import android.app.Activity
import android.content.Intent
import android.webkit.CookieManager
import android.webkit.WebStorage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.SmartButton
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cylonid.nativealpha.MainActivity
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.WebAppSettingsActivity
import com.cylonid.nativealpha.activities.AdblockConfigActivity
import com.cylonid.nativealpha.model.DataManager
import com.cylonid.nativealpha.ui.components.RoundedCardContainer
import com.cylonid.nativealpha.ui.components.SettingsActionItem
import com.cylonid.nativealpha.ui.components.SettingsSectionHeader
import com.cylonid.nativealpha.ui.components.SettingsSwitchItem
import com.cylonid.nativealpha.util.Const
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val dataManager = remember { DataManager.getInstance() }

    var settings by remember { mutableStateOf(dataManager.settings.copy()) }

    fun updateSettings(modify: () -> Unit) {
        modify()
        settings = settings.copy()
        dataManager.settings = settings
    }

    val exportLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("*/*"),
        ) { uri ->
            if (uri != null) {
                dataManager.saveGlobalSettings()
                val success = dataManager.saveSharedPreferencesToFile(uri)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(
                        message =
                            context.getString(
                                if (success) R.string.export_success else R.string.export_failed,
                            ),
                    )
                }
            }
        }

    val importLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
        ) { uri ->
            if (uri != null) {
                val success = dataManager.loadSharedPreferencesFromFile(uri)
                if (!success) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.import_failed),
                        )
                    }
                } else {
                    WebStorage.getInstance().deleteAllData()
                    CookieManager.getInstance().removeAllCookies(null)
                    dataManager.loadAppData()
                    val intent =
                        Intent(context, MainActivity::class.java).apply {
                            putExtra(Const.INTENT_BACKUP_RESTORED, true)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }
            }
        }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.global_settings),
                        fontWeight = FontWeight.Bold,
                    )
                },
                subtitle = {
                    Text(
                        text =
                            stringResource(
                                R.string.these_settings_are_applied_globally_and_override_app_specific_settings,
                            ),
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            // ── Section 1: Configuration & Defaults ──
            SettingsSectionHeader(stringResource(R.string.general))

            RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
                SettingsActionItem(
                    title = stringResource(R.string.adblock_config),
                    icon = Icons.Rounded.Security,
                    trailingIcon = Icons.AutoMirrored.Rounded.OpenInNew,
                    onClick = {
                        val intent =
                            Intent(context, AdblockConfigActivity::class.java).apply {
                                action = Intent.ACTION_VIEW
                            }
                        context.startActivity(intent)
                    },
                )

                SettingsActionItem(
                    title = stringResource(R.string.global_web_app_settings),
                    description =
                        stringResource(
                            R.string.these_settings_are_applied_globally_and_override_app_specific_settings,
                        ),
                    icon = Icons.Rounded.Language,
                    trailingIcon = Icons.AutoMirrored.Rounded.OpenInNew,
                    onClick = {
                        val intent =
                            Intent(context, WebAppSettingsActivity::class.java).apply {
                                putExtra(Const.INTENT_WEBAPPID, settings.globalWebApp.ID)
                                action = Intent.ACTION_VIEW
                            }
                        context.startActivity(intent)
                    },
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Section 2: Browsing & Privacy ──
            SettingsSectionHeader("Browsing & Display")

            RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
                SettingsSwitchItem(
                    title = stringResource(R.string.clear_cache_after_usage),
                    checked = settings.isClearCache,
                    icon = Icons.Rounded.CleaningServices,
                    onCheckedChange = { isChecked ->
                        updateSettings { settings.isClearCache = isChecked }
                    },
                )

                SettingsSwitchItem(
                    title = stringResource(R.string.show_progress_bar_during_page_load),
                    checked = settings.isShowProgressbar,
                    icon = Icons.Rounded.HourglassTop,
                    onCheckedChange = { isChecked ->
                        updateSettings { settings.isShowProgressbar = isChecked }
                    },
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Section 3: Gestures & Navigation ──
            SettingsSectionHeader("Gestures & Controls")

            RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
                SettingsSwitchItem(
                    title = stringResource(R.string.global_settings_multitouch_reload),
                    checked = settings.isMultitouchReload,
                    icon = Icons.Rounded.Refresh,
                    onCheckedChange = { isChecked ->
                        updateSettings { settings.isMultitouchReload = isChecked }
                    },
                )

                SettingsSwitchItem(
                    title =
                        stringResource(
                            R.string.use_two_finger_swipes_for_browser_forward_and_backward_navigation,
                        ),
                    checked = settings.isTwoFingerMultitouch,
                    icon = Icons.Rounded.Swipe,
                    onCheckedChange = { isChecked ->
                        updateSettings { settings.isTwoFingerMultitouch = isChecked }
                    },
                )

                SettingsSwitchItem(
                    title =
                        stringResource(
                            R.string.use_three_finger_swipes_to_switch_between_web_apps_experimental,
                        ),
                    checked = settings.isThreeFingerMultitouch,
                    icon = Icons.Rounded.ViewCarousel,
                    onCheckedChange = { isChecked ->
                        updateSettings { settings.isThreeFingerMultitouch = isChecked }
                    },
                )

                SettingsSwitchItem(
                    title = stringResource(R.string.always_show_software_buttons),
                    checked = settings.alwaysShowSoftwareButtons,
                    icon = Icons.Rounded.SmartButton,
                    onCheckedChange = { isChecked ->
                        updateSettings { settings.alwaysShowSoftwareButtons = isChecked }
                    },
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Section 4: Backup & Restore ──
            SettingsSectionHeader(stringResource(R.string.backup))

            RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
                SettingsActionItem(
                    title = stringResource(R.string.export_settings_web_apps),
                    icon = Icons.Rounded.CloudUpload,
                    trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    onClick = {
                        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        val currentDateTime = sdf.format(Date())
                        exportLauncher.launch("NativeAlpha_$currentDateTime")
                    },
                )

                SettingsActionItem(
                    title = stringResource(R.string.import_settings_web_apps),
                    icon = Icons.Rounded.CloudDownload,
                    trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    onClick = {
                        importLauncher.launch(arrayOf("*/*"))
                    },
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
