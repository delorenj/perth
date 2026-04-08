package sh.delo.perth.feature.voice.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.ui.ErrorBanner
import sh.delo.perth.core.ui.ErrorMapper

/**
 * Voice control panel composable intended to sit at the bottom of the Terminal screen.
 *
 * Responsibilities:
 * - Runtime RECORD_AUDIO permission request at point of use (not on app start).
 * - Permission rationale dialog on first denial; settings redirect on permanent denial.
 * - Pulsing recording indicator while audio is being captured.
 * - Mode selector (Transcription / Task / Command), persisted via DataStore.
 * - Transcription preview with editable field and Send / Cancel actions.
 *
 * [activePaneId] is forwarded to [VoiceViewModel.onSend] so text lands in the correct pane.
 */
@Composable
fun VoiceControlPanel(
    activePaneId: PaneId?,
    onCommandTranscript: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VoiceViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // One-shot: show task write success snackbar.
    LaunchedEffect(state.taskWriteSuccess) {
        if (state.taskWriteSuccess) {
            snackbarHostState.showSnackbar(
                message = "task.md written successfully.",
                duration = SnackbarDuration.Short,
            )
            viewModel.onDismissTaskWriteSuccess()
        }
    }

    // One-shot: open Android app settings (for mic permission or API key).
    LaunchedEffect(state.pendingSettingsIntent) {
        if (state.pendingSettingsIntent) {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", context.packageName, null),
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
            viewModel.onSettingsIntentConsumed()
        }
    }

    // Track whether we should show a rationale before re-requesting permission.
    var showRationale by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        viewModel.onPermissionResult(granted)
        if (granted) {
            viewModel.startRecording()
        } else {
            showRationale = true
        }
    }

    if (showRationale) {
        PermissionRationaleDialog(
            onConfirm = {
                showRationale = false
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            },
            onDismiss = { showRationale = false },
        )
    }

    // Task mode confirmation dialog and success signal.
    TaskModeHandler(
        state = state,
        activePaneId = activePaneId,
        viewModel = viewModel,
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Mode selector row.
            VoiceModeSelector(
                selectedMode = state.selectedMode,
                onModeSelected = viewModel::onModeSelected,
                enabled = !state.isRecording && !state.isProcessing,
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Microphone button / processing indicator.
            MicrophoneButton(
                captureState = state.captureState,
                onMicClick = {
                    when {
                        state.isRecording -> viewModel.stopRecording()
                        state.isProcessing -> Unit // no-op while processing
                        else -> {
                            val permissionGranted = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO,
                            ) == PackageManager.PERMISSION_GRANTED

                            if (permissionGranted) {
                                viewModel.onPermissionResult(true)
                                viewModel.startRecording()
                            } else {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    }
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Error banner — replaces the plain snackbar for structured recovery.
            AnimatedVisibility(
                visible = state.error != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                state.error?.let { exception ->
                    val presentation = ErrorMapper.map(exception)
                    ErrorBanner(
                        message = presentation.message,
                        actions = presentation.recoveryActions,
                        onAction = viewModel::onRecoveryAction,
                    )
                }
            }

            // Transcription preview — only visible in ReadyToSend state.
            AnimatedVisibility(
                visible = state.showSendPanel,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut(),
            ) {
                Column {
                    TranscriptionPreview(
                        editedText = state.editedTranscription,
                        onTextChanged = viewModel::onTranscriptionEdited,
                        // In Task mode the send action is gated by the confirmation dialog;
                        // in other modes it sends to the terminal directly.
                        onSend = {
                            if (state.showTaskConfirmRow) {
                                viewModel.onRequestWriteTask()
                            } else {
                                viewModel.onSend(activePaneId, onCommandTranscript)
                            }
                        },
                        onCancel = viewModel::onCancel,
                    )

                    // Task-mode: "Write to task.md" / "Cancel" row replaces the
                    // generic Send button. The write itself is gated behind the
                    // confirmation dialog owned by the top-level TaskModeHandler call.
                    if (state.showTaskConfirmRow) {
                        TaskActionRow(
                            onWriteToTask = { viewModel.onRequestWriteTask() },
                            onCancel = { viewModel.onCancel() },
                        )
                    }
                }
            }

            // Snackbar for brief success messages.
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Internal composables
// -----------------------------------------------------------------------------

@Composable
private fun MicrophoneButton(
    captureState: CaptureState,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(72.dp),
    ) {
        when (captureState) {
            CaptureState.Processing -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(56.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                )
            }
            CaptureState.Recording -> {
                PulsingMicButton(onClick = onMicClick)
            }
            else -> {
                IdleMicButton(
                    onClick = onMicClick,
                    enabled = captureState == CaptureState.Idle,
                )
            }
        }
    }
}

@Composable
private fun PulsingMicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "mic_pulse_scale",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        // Pulsing halo behind the button.
        Box(
            modifier = Modifier
                .size(56.dp)
                .scale(scale)
                .background(
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.24f),
                    shape = CircleShape,
                )
        )

        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = MaterialTheme.colorScheme.error,
                    shape = CircleShape,
                ),
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Stop recording",
                tint = Color.White,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun IdleMicButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (enabled) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .size(56.dp)
            .background(color = containerColor, shape = CircleShape),
    ) {
        Icon(
            imageVector = if (enabled) Icons.Default.Mic else Icons.Default.MicOff,
            contentDescription = if (enabled) "Start recording" else "Microphone unavailable",
            tint = if (enabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun PermissionRationaleDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Microphone access needed") },
        text = {
            Text(
                "Perth needs microphone access to capture your voice commands. " +
                    "Please grant the permission to continue."
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onConfirm) {
                Text("Grant permission")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
