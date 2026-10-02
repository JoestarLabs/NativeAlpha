package com.cylonid.nativealpha.ui.about

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Gavel
import androidx.compose.material.icons.rounded.Payment
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cylonid.nativealpha.BuildConfig
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.ui.components.RoundedCardContainer
import com.cylonid.nativealpha.ui.components.SettingsActionItem
import com.cylonid.nativealpha.ui.components.SettingsSectionHeader
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import java.time.Year

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showLicenses by rememberSaveable { mutableStateOf(false) }
    var showEulaDialog by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val context = LocalContext.current

    BackHandler(enabled = showLicenses) {
        showLicenses = false
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = {
                    Text(
                        text =
                            if (showLicenses) {
                                stringResource(R.string.open_source_libs)
                            } else {
                                stringResource(R.string.app_info)
                            },
                        fontWeight = FontWeight.Bold,
                    )
                },
                subtitle = {
                    Text(
                        text =
                            if (showLicenses) {
                                stringResource(R.string.open_source_dependencies_desc)
                            } else {
                                stringResource(R.string.app_name)
                            },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (showLicenses) {
                                showLicenses = false
                            } else {
                                onNavigateBack()
                            }
                        },
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
                        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) { innerPadding ->
        AnimatedContent(
            targetState = showLicenses,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "AboutScreenContentTransition",
            modifier = Modifier.padding(innerPadding),
        ) { isViewingLicenses ->
            if (isViewingLicenses) {
                OpenSourceLicensesList(
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                AboutOverviewContent(
                    onOpenUrl = { url ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                        }
                    },
                    onShowEula = { showEulaDialog = true },
                    onShowLicenses = { showLicenses = true },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }

    if (showEulaDialog) {
        AlertDialog(
            onDismissRequest = { showEulaDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.eula_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = stringResource(R.string.eula_content),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showEulaDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
            shape = RoundedCornerShape(28.dp),
        )
    }
}

@Composable
private fun AboutOverviewContent(
    onOpenUrl: (String) -> Unit,
    onShowEula: () -> Unit,
    onShowLicenses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
    ) {
        Spacer(Modifier.height(16.dp))

        // ── App Hero / Branding Header ──
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    tonalElevation = 2.dp,
                    modifier = Modifier.size(96.dp),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Image(
                            painter = painterResource(R.drawable.native_alpha_foreground),
                            contentDescription = stringResource(R.string.app_name),
                            modifier = Modifier.size(80.dp),
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = "by cylonid © ${Year.now().value}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // ── Section 1: Links & Community ──
        SettingsSectionHeader(stringResource(R.string.about_section_community))

        RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
            SettingsActionItem(
                title = "GitHub",
                description = "cylonid/NativeAlphaForAndroid",
                icon = Icons.Rounded.Code,
                trailingIcon = Icons.AutoMirrored.Rounded.OpenInNew,
                onClick = { onOpenUrl("https://github.com/cylonid/NativeAlphaForAndroid") },
            )

            if (BuildConfig.FLAVOR != "extended") {
                SettingsActionItem(
                    title = "Google Play",
                    description = "Native Alpha Plus",
                    icon = Icons.Rounded.ShoppingBag,
                    trailingIcon = Icons.AutoMirrored.Rounded.OpenInNew,
                    onClick = {
                        onOpenUrl("market://details?id=com.cylonid.nativealpha.pro")
                    },
                )
            }

            SettingsActionItem(
                title = stringResource(R.string.privacy_policy),
                description = "github.com/cylonid/NativeAlphaForAndroid",
                icon = Icons.Rounded.Security,
                trailingIcon = Icons.AutoMirrored.Rounded.OpenInNew,
                onClick = {
                    onOpenUrl("https://github.com/cylonid/NativeAlphaForAndroid/blob/dev/privacy_policy.md")
                },
            )
        }

        if (BuildConfig.FLAVOR == "extendedGithub") {
            Spacer(Modifier.height(16.dp))

            // ── Section 2: Support Development ──
            SettingsSectionHeader(stringResource(R.string.support_development))

            RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
                SettingsActionItem(
                    title = stringResource(R.string.support_liberapay),
                    description = "liberapay.com/cylonid",
                    icon = Icons.Rounded.Favorite,
                    trailingIcon = Icons.AutoMirrored.Rounded.OpenInNew,
                    onClick = { onOpenUrl("https://liberapay.com/cylonid") },
                )

                SettingsActionItem(
                    title = stringResource(R.string.paypal),
                    description = "paypal.me/cylonid",
                    icon = Icons.Rounded.Payment,
                    trailingIcon = Icons.AutoMirrored.Rounded.OpenInNew,
                    onClick = { onOpenUrl("https://paypal.me/cylonid") },
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Section 3: Legal & Open Source ──
        SettingsSectionHeader(stringResource(R.string.license))

        RoundedCardContainer(modifier = Modifier.fillMaxWidth()) {
            SettingsActionItem(
                title = stringResource(R.string.eula_title),
                description = "End User License Agreement",
                icon = Icons.Rounded.Gavel,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                onClick = onShowEula,
            )

            SettingsActionItem(
                title = stringResource(R.string.gnu_license),
                description = "GNU General Public License v3.0",
                icon = Icons.Rounded.Description,
                trailingIcon = Icons.AutoMirrored.Rounded.OpenInNew,
                onClick = { onOpenUrl("https://www.gnu.org/licenses/gpl-3.0.txt") },
            )

            SettingsActionItem(
                title = stringResource(R.string.open_source_libs),
                description = stringResource(R.string.open_source_dependencies_desc),
                icon = Icons.Rounded.Extension,
                trailingIcon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                onClick = onShowLicenses,
            )
        }

        Spacer(Modifier.height(28.dp))
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OpenSourceLicensesList(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val libsState by produceLibraries(R.raw.aboutlibraries)

    val libs = libsState
    if (libs == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        val librariesList = libs.libraries
        LazyColumn(
            modifier =
                modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Spacer(Modifier.height(6.dp))
            }

            items(librariesList, key = { it.uniqueId }) { library ->
                DependencyItemCard(
                    library = library,
                    onClick = {
                        val url = library.website ?: library.licenses.firstOrNull()?.url
                        if (!url.isNullOrBlank()) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                            }
                        }
                    },
                )
            }

            item {
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DependencyItemCard(
    library: Library,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceBright,
        tonalElevation = 1.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = library.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    val organization = library.organization?.name
                    if (!organization.isNullOrBlank()) {
                        Text(
                            text = organization,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }

                val version = library.artifactVersion
                if (!version.isNullOrBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        Text(
                            text = version,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }
                }
            }

            val description = library.description
            if (!description.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    library.licenses.forEach { license ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        ) {
                            Text(
                                text = license.name,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                }

                val hasWebsite = !library.website.isNullOrBlank() || library.licenses.any { !it.url.isNullOrBlank() }
                if (hasWebsite) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
