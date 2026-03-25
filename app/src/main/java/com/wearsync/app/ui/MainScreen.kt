package com.wearsync.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AppBlocking
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PhonelinkOff
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material.icons.outlined.WatchOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wearsync.app.domain.AppInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Watch,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("WearSync", style = MaterialTheme.typography.titleLarge)
                        }
                    },
                    actions = {
                        if (uiState is UiState.Success) {
                            val showSystem = (uiState as UiState.Success).showSystemApps
                            IconButton(onClick = { viewModel.toggleSystemApps() }) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = if (showSystem) "Hide system apps" else "Show system apps",
                                    tint = if (showSystem) MaterialTheme.colorScheme.primary
                                           else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                val batchProgress = (uiState as? UiState.Success)?.batchInstallProgress
                AnimatedVisibility(visible = batchProgress != null, enter = fadeIn(), exit = fadeOut()) {
                    batchProgress?.let {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                            LinearProgressIndicator(
                                progress = { it.current.toFloat() / it.total },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Text(
                                text = "Opening Play Store: ${it.current} of ${it.total}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = uiState) {
                is UiState.Loading -> LoadingContent()
                is UiState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.loadData() }
                )
                is UiState.Success -> SuccessContent(
                    result = state,
                    onRefresh = { viewModel.loadData() },
                    onAppClick = { viewModel.openPlayStore(it.packageName) },
                    onBatchInstall = { viewModel.batchInstall() }
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Scanning apps...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Connecting to your watch",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.WatchOff,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Connection Error",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuccessContent(
    result: UiState.Success,
    onRefresh: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onBatchInstall: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()

    val notOnWatch = result.result.notOnWatch
    val alreadyInstalled = result.result.alreadyOnWatch

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                text = {
                    BadgedBox(
                        badge = {
                            if (notOnWatch.isNotEmpty()) {
                                Badge { Text("${notOnWatch.size}") }
                            }
                        }
                    ) {
                        Text("Not on Watch")
                    }
                }
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                text = {
                    BadgedBox(
                        badge = {
                            if (alreadyInstalled.isNotEmpty()) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.tertiary
                                ) { Text("${alreadyInstalled.size}") }
                            }
                        }
                    ) {
                        Text("Installed")
                    }
                }
            )
        }

        PullToRefreshBox(
            isRefreshing = result.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> NotOnWatchTab(
                        apps = notOnWatch,
                        onAppClick = onAppClick,
                        onBatchInstall = onBatchInstall
                    )
                    1 -> AlreadyInstalledTab(apps = alreadyInstalled)
                }
            }
        }
    }
}

@Composable
private fun NotOnWatchTab(
    apps: List<AppInfo>,
    onAppClick: (AppInfo) -> Unit,
    onBatchInstall: () -> Unit
) {
    if (apps.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.Watch,
            title = "All synced up!",
            subtitle = "Every app on your phone is also on your watch"
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 88.dp, top = 4.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(apps, key = { it.packageName }) { app ->
                AppItem(
                    app = app,
                    trailing = {
                        FilledTonalButton(
                            onClick = { onAppClick(app) },
                        ) {
                            Icon(
                                Icons.Outlined.CloudDownload,
                                contentDescription = "Install",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Get", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                )
            }
        }

        ExtendedFloatingActionButton(
            onClick = onBatchInstall,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Outlined.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Install All")
        }
    }
}

@Composable
private fun AlreadyInstalledTab(apps: List<AppInfo>) {
    if (apps.isEmpty()) {
        EmptyState(
            icon = Icons.Outlined.AppBlocking,
            title = "No matches yet",
            subtitle = "None of your phone apps were found on the watch"
        )
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(vertical = 4.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(apps, key = { it.packageName }) { app ->
            AppItem(
                app = app,
                trailing = {
                    Icon(
                        Icons.Outlined.Watch,
                        contentDescription = "On watch",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}

@Composable
private fun AppItem(
    app: AppInfo,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        app.icon?.let { drawable ->
            Image(
                bitmap = drawable.toBitmap(width = 96, height = 96).asImageBitmap(),
                contentDescription = "${app.appLabel} icon",
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appLabel,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        trailing()
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
