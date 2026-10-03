package com.cylonid.nativealpha.ui.webapplist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cylonid.nativealpha.BuildConfig
import com.cylonid.nativealpha.R
import com.cylonid.nativealpha.model.WebApp
import com.cylonid.nativealpha.ui.webapplist.components.AddWebAppDialog
import com.cylonid.nativealpha.ui.webapplist.components.WebAppListEmptyState
import com.cylonid.nativealpha.ui.webapplist.components.WebAppListItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WebAppListScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onOpenWebApp: (WebApp) -> Unit,
    onOpenWebAppSettings: (WebApp) -> Unit,
    onRequestCreateShortcut: (WebApp) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WebAppListViewModel = remember { WebAppListViewModel() },
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val reorderState =
        rememberReorderableLazyListState(
            lazyListState = listState,
            onMove = { fromIndex, toIndex ->
                viewModel.reorderWebApps(fromIndex, toIndex)
            },
            onDragEnd = {
                viewModel.commitReorder()
            },
        )

    val nestedScrollConnection =
        remember(reorderState.isDragging, scrollBehavior) {
            if (reorderState.isDragging) {
                object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {}
            } else {
                scrollBehavior.nestedScrollConnection
            }
        }

    val isFabExpanded by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 30
        }
    }

    val removedMessageFormat = stringResource(R.string.x_was_removed)
    val undoLabel = stringResource(R.string.undo)

    Scaffold(
        modifier = modifier.nestedScroll(nestedScrollConnection),
        topBar = {
            if (uiState.isSearchActive) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = uiState.searchQuery,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = { Text(stringResource(R.string.search_webapps)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = stringResource(R.string.cancel),
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = CircleShape,
                            colors =
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                ),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(end = 8.dp),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.setSearchActive(false) }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.cancel),
                            )
                        }
                    },
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        ),
                )
            } else {
                val appTitle =
                    if (BuildConfig.FLAVOR == "extended") {
                        stringResource(R.string.app_name_plus)
                    } else {
                        stringResource(R.string.app_name)
                    }

                val activeCount = uiState.allWebApps.size
                val subtitleText =
                    if (activeCount == 1) {
                        stringResource(R.string.active_webapps_single)
                    } else if (activeCount > 1) {
                        stringResource(R.string.active_webapps_count, activeCount)
                    } else {
                        null
                    }

                MediumFlexibleTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.native_alpha_white_foreground),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(46.dp),
                            )
                            Text(
                                text = appTitle,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    },
                    subtitle = {
                        if (subtitleText != null) {
                            Text(
                                text = subtitleText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    actions = {
                        if (uiState.allWebApps.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchActive(true) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = stringResource(R.string.search_webapps),
                                )
                            }
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = stringResource(R.string.action_settings),
                            )
                        }
                        IconButton(onClick = onNavigateToAbout) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = stringResource(R.string.app_info),
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors =
                        TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        ),
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = uiState.allWebApps.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.add_webapp)) },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    expanded = isFabExpanded,
                    onClick = { showAddDialog = true },
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
        ) {
            when {
                uiState.allWebApps.isEmpty() -> {
                    WebAppListEmptyState(
                        onAddClick = { showAddDialog = true },
                    )
                }
                uiState.filteredWebApps.isEmpty() -> {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.no_webapps_found),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "\"${uiState.searchQuery}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        itemsIndexed(
                            items = uiState.filteredWebApps,
                            key = { _, item -> item.ID },
                        ) { index, webApp ->
                            val isDraggingThisItem = reorderState.draggedIndex == index
                            WebAppListItem(
                                webApp = webApp,
                                onClick = { onOpenWebApp(webApp) },
                                onOpenSettings = { onOpenWebAppSettings(webApp) },
                                onCreateShortcut = { onRequestCreateShortcut(webApp) },
                                onDelete = {
                                    viewModel.deleteWebApp(context, webApp)
                                    coroutineScope.launch {
                                        val result =
                                            snackbarHostState.showSnackbar(
                                                message = String.format(removedMessageFormat, webApp.title),
                                                actionLabel = undoLabel,
                                                duration = SnackbarDuration.Short,
                                            )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoDelete(context)
                                        }
                                    }
                                },
                                modifier =
                                    Modifier
                                        .then(if (isDraggingThisItem) Modifier else Modifier.animateItem())
                                        .reorderableItem(reorderState, index),
                                dragHandleModifier = Modifier.dragHandle(reorderState, index),
                                isReordering = reorderState.isDragging,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddWebAppDialog(
            onConfirm = { url, createShortcut ->
                showAddDialog = false
                val newApp = viewModel.addWebApp(url, context)
                if (createShortcut) {
                    onRequestCreateShortcut(newApp)
                }
            },
            onDismiss = { showAddDialog = false },
        )
    }
}
