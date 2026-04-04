package sh.delo.perth.feature.terminal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Root screen for Stories 2.1 / 2.2 / 2.3.
 *
 * Layout (top-to-bottom):
 *   TopAppBar       - session name, back nav
 *   TabPager        - HorizontalPager with tab chip indicator bar (Story 2.1)
 *                     Each page renders PaneGrid (Story 2.2)
 *   HorizontalDivider
 *   TerminalInputBar - typed input to active pane (Story 2.3)
 */
@Composable
fun TerminalScreen(
    sessionId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TerminalViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar("Error: ${it.message}") }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TerminalTopBar(
                sessionName = state.sessionName,
                onNavigateBack = onNavigateBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            // Story 1.4: Disconnected banner — shown whenever the connection is not healthy.
            // The last-known pane output remains visible beneath it (cached in state).
            if (state.showDisconnectedBanner) {
                DisconnectedBanner(
                    isReconnecting = state.isReconnecting,
                    reconnectFailed = state.reconnectFailed,
                    onReconnect = viewModel::onManualReconnect,
                )
            }

            // Main content area (tabs + panes): takes all remaining space above the input bar
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.isLoading -> LoadingIndicator()
                    state.activeTabs.isEmpty() -> EmptyMessage()
                    else -> TabPager(
                        tabs = state.activeTabs,
                        selectedTabIndex = state.activeTabIndex,
                        activePaneId = state.activePaneId,
                        paneOutput = state.paneOutput,
                        onTabSelected = viewModel::onTabSelected,
                        onPaneSelected = viewModel::onPaneSelected,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }

            HorizontalDivider()

            // Typed input bar - enabled only when an active pane has been resolved
            TerminalInputBar(
                onSend = viewModel::onSendInput,
                enabled = state.activePaneId != null,
            )
        }
    }
}

/**
 * Story 1.4: Banner shown when the WebSocket connection is lost.
 * Displays a reconnecting spinner while auto-reconnect is in progress, or a manual
 * Reconnect button once all automatic attempts have been exhausted.
 */
@Composable
private fun DisconnectedBanner(
    isReconnecting: Boolean,
    reconnectFailed: Boolean,
    onReconnect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = when {
                    isReconnecting -> "Reconnecting…"
                    reconnectFailed -> "Disconnected"
                    else -> "Disconnected"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
        when {
            isReconnecting -> CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            reconnectFailed -> Button(
                onClick = onReconnect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = "Reconnect",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TerminalTopBar(
    sessionName: String,
    onNavigateBack: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                text = sessionName.ifBlank { "Terminal" },
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No active session",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
