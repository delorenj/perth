package sh.delo.perth.feature.voice.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.delo.perth.core.domain.model.PaneId

/**
 * Handles task-mode UI flow: confirmation dialog, overwrite-vs-append choice,
 * and the success/failure feedback hooks.
 *
 * This composable owns no state itself — it observes [VoiceUiState] from its
 * parent and delegates all decisions back to [VoiceViewModel]. It reuses the
 * existing transcription pipeline; the only task-specific step is the
 * confirmation dialog before the write command is sent.
 *
 * Usage: embed inside [VoiceControlPanel] when [VoiceUiState.showTaskConfirmRow]
 * is true, and forward [activePaneId] so the write lands in the correct pane.
 *
 * @param state Current voice UI state.
 * @param activePaneId The pane that will receive the `cat > task.md` command.
 * @param viewModel Caller's [VoiceViewModel] instance.
 * @param onWriteSuccess Called after task.md was written successfully so the
 *   parent can show a snackbar or other feedback.
 */
@Composable
fun TaskModeHandler(
    state: VoiceUiState,
    activePaneId: PaneId?,
    viewModel: VoiceViewModel,
    onWriteSuccess: () -> Unit = {},
) {
    // Consume the one-shot success flag and notify the parent.
    LaunchedEffect(state.taskWriteSuccess) {
        if (state.taskWriteSuccess) {
            onWriteSuccess()
            viewModel.onDismissTaskWriteSuccess()
        }
    }

    // Confirmation dialog — shown when the user tapped the "Write to task.md" button.
    if (state.taskWriteConfirmationPending) {
        TaskWriteConfirmDialog(
            transcriptionPreview = state.editedTranscription,
            activePaneId = activePaneId,
            onConfirmOverwrite = {
                viewModel.onConfirmWriteTask(activePaneId, append = false)
            },
            onConfirmAppend = {
                viewModel.onConfirmWriteTask(activePaneId, append = true)
            },
            onDismiss = { viewModel.onCancelWriteTask() },
        )
    }
}

// -----------------------------------------------------------------------------
// Internal composables
// -----------------------------------------------------------------------------

/**
 * Row of actions shown below the transcription preview in Task mode.
 * Replaces the generic "Send" button with a task-specific "Write to task.md" button.
 *
 * Internal visibility so [VoiceControlPanel] can embed it directly inside the
 * panel [Column] without triggering [TaskModeHandler]'s dialog/success logic again.
 */
@Composable
internal fun TaskActionRow(
    onWriteToTask: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
        ) {
            Text("Cancel")
        }

        Button(
            onClick = onWriteToTask,
            modifier = Modifier.weight(2f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
        ) {
            Text("Write to task.md")
        }
    }
}

/**
 * Confirmation dialog shown before writing task.md.
 *
 * Displays a short preview of the transcription text and offers three choices:
 * - Overwrite: replace any existing task.md.
 * - Append: add to the end of an existing task.md.
 * - Cancel: return to the transcription preview without writing.
 *
 * The overwrite/append distinction is surfaced here rather than as a separate
 * flow because the user may not know whether task.md exists yet. Keeping both
 * options visible in the same dialog avoids an extra round-trip.
 */
@Composable
private fun TaskWriteConfirmDialog(
    transcriptionPreview: String,
    activePaneId: PaneId?,
    onConfirmOverwrite: () -> Unit,
    onConfirmAppend: () -> Unit,
    onDismiss: () -> Unit,
) {
    val preview = transcriptionPreview.take(200).let {
        if (transcriptionPreview.length > 200) "$it…" else it
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Write to task.md?") },
        text = {
            Column {
                Text(
                    text = "The following text will be written to task.md in the active pane" +
                        (activePaneId?.let { " (${it.value})" } ?: "") + ":",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "If task.md already exists, you can overwrite it or append to it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            Button(onClick = onConfirmOverwrite) {
                Text("Overwrite")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onConfirmAppend) {
                    Text("Append")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
    )
}
