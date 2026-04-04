package sh.delo.perth.feature.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sh.delo.perth.core.domain.model.ConnectionState
import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.domain.repository.SettingsRepository

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSessions: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Navigate to sessions on successful connect (Story 1.2)
    LaunchedEffect(state.connectionState) {
        if (state.connectionState == ConnectionState.Connected) {
            onNavigateToSessions?.invoke()
            viewModel.onNavigatedToSessions()
        }
    }

    LaunchedEffect(state.error) {
        state.error?.let { snackbarHostState.showSnackbar("Error: ${it.message}") }
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) {
            snackbarHostState.showSnackbar("Settings saved — connecting…")
            viewModel.onDismissSaveSuccess()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            SettingsTopBar(onNavigateBack = onNavigateBack)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ServerSection(
                serverUrl = state.serverUrl,
                connectionState = state.connectionState,
                isSaving = state.isSaving || state.isConnecting,
                onServerUrlChange = viewModel::onServerUrlChange,
                onSave = viewModel::onSaveAndConnect,
            )

            VoiceModeSection(
                selectedMode = state.voiceMode,
                onModeSelected = viewModel::onVoiceModeChange,
            )

            LlmApiKeySection(
                hasKey = state.hasLlmApiKey,
                isValid = state.llmApiKeyValid,
                isValidating = state.isValidatingApiKey,
                isSaving = state.isSaving,
                onSaveKey = viewModel::onSaveLlmApiKey,
            )

            RecentSessionsSection(
                sessions = state.recentSessions,
                onClearHistory = viewModel::onClearRecentSessions,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = { Text("Settings") },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

@Composable
private fun ServerSection(
    serverUrl: String,
    connectionState: ConnectionState,
    isSaving: Boolean,
    onServerUrlChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionLabel(
            icon = {
                val icon = if (connectionState == ConnectionState.Connected) {
                    Icons.Default.Wifi
                } else {
                    Icons.Default.WifiOff
                }
                Icon(icon, contentDescription = null)
            },
            title = "Zealot Server",
        )
        Spacer(Modifier.height(6.dp))
        ConnectionStatusRow(connectionState = connectionState, isBusy = isSaving)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = serverUrl,
            onValueChange = onServerUrlChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Server URL") },
            placeholder = { Text("http://192.168.1.100:7800", fontFamily = FontFamily.Monospace) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done,
            ),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = onSave,
            enabled = serverUrl.isNotBlank() && !isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            } else {
                Text(
                    text = when (connectionState) {
                        ConnectionState.Connected -> "Reconnect"
                        ConnectionState.Connecting -> "Connecting…"
                        else -> "Save & Connect"
                    }
                )
            }
        }
    }
}

@Composable
private fun ConnectionStatusRow(
    connectionState: ConnectionState,
    isBusy: Boolean,
    modifier: Modifier = Modifier,
) {
    val (label, color) = when {
        isBusy -> "Connecting…" to MaterialTheme.colorScheme.tertiary
        else -> when (connectionState) {
            ConnectionState.Connected -> "Connected" to MaterialTheme.colorScheme.primary
            ConnectionState.Connecting -> "Connecting…" to MaterialTheme.colorScheme.tertiary
            ConnectionState.Disconnected -> "Not connected" to MaterialTheme.colorScheme.outline
            ConnectionState.Error -> "Connection failed" to MaterialTheme.colorScheme.error
        }
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (connectionState == ConnectionState.Connected && !isBusy) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp),
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color,
        )
    }
}

@Composable
private fun VoiceModeSection(
    selectedMode: SettingsRepository.VoiceMode,
    onModeSelected: (SettingsRepository.VoiceMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionLabel(icon = null, title = "Voice Mode")
        Spacer(Modifier.height(8.dp))
        SettingsRepository.VoiceMode.entries.forEach { mode ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = mode == selectedMode,
                    onClick = { onModeSelected(mode) },
                )
                Column {
                    Text(
                        text = mode.name,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = when (mode) {
                            SettingsRepository.VoiceMode.Transcription -> "Voice to text, paste into active pane"
                            SettingsRepository.VoiceMode.Task -> "Voice to task.md file"
                            SettingsRepository.VoiceMode.Command -> "Voice to LLM command (requires confirmation)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun LlmApiKeySection(
    hasKey: Boolean,
    isValid: Boolean,
    isValidating: Boolean,
    isSaving: Boolean,
    onSaveKey: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var apiKey by rememberSaveable { mutableStateOf("") }

    Column(modifier = modifier) {
        SectionLabel(
            icon = { Icon(Icons.Default.Key, contentDescription = null) },
            title = "LLM API Key",
        )
        if (hasKey || isValidating) {
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                when {
                    isValidating -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                        Text(
                            text = "Validating key…",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    isValid -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "API key valid",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    hasKey -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "API key stored (not yet validated)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(if (hasKey) "Replace API key" else "OpenAI API Key") },
            placeholder = { Text("sk-…") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                onSaveKey(apiKey)
                apiKey = ""
            },
            enabled = apiKey.isNotBlank() && !isSaving && !isValidating,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (hasKey) "Update API Key" else "Save API Key")
        }
    }
}

/** Story 1.5: Recent sessions list with a clear history action. */
@Composable
private fun RecentSessionsSection(
    sessions: List<ZellijSession>,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            SectionLabel(
                icon = { Icon(Icons.Default.History, contentDescription = null) },
                title = "Recent Sessions",
            )
            if (sessions.isNotEmpty()) {
                TextButton(onClick = onClearHistory) {
                    Text(
                        text = "Clear",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        if (sessions.isEmpty()) {
            Text(
                text = "No recent sessions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
            )
        } else {
            sessions.forEach { session ->
                RecentSessionRow(session = session)
            }
        }
    }
}

@Composable
private fun RecentSessionRow(
    session: ZellijSession,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Outlined.Terminal,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = session.name,
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun SectionLabel(
    icon: (@Composable () -> Unit)?,
    title: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        icon?.invoke()
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
