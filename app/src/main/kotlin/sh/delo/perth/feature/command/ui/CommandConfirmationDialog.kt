package sh.delo.perth.feature.command.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val DestructiveRed = Color(0xFFC62828)

/**
 * A full-screen dialog presented when the user taps Execute on a plan that
 * contains at least one destructive step (Story 5.3).
 *
 * This dialog is an additional confirmation layer on top of the per-step checkboxes.
 * It cannot be dismissed by tapping outside; the user must choose Proceed or Cancel.
 *
 * @param destructiveCommandCount Number of destructive steps about to be executed.
 * @param onProceed Called when the user explicitly taps "Yes, Execute".
 * @param onCancel Called when the user taps "Cancel".
 */
@Composable
fun DestructiveConfirmationDialog(
    destructiveCommandCount: Int,
    onProceed: () -> Unit,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { /* non-dismissible - user must choose */ },
        icon = {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = DestructiveRed,
                modifier = Modifier.size(36.dp),
            )
        },
        title = {
            Text(
                text = "Destructive Commands Detected",
                color = DestructiveRed,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                Text(
                    text = "Your plan includes $destructiveCommandCount destructive " +
                        "${if (destructiveCommandCount == 1) "command" else "commands"} " +
                        "that may cause irreversible data loss.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Are you absolutely sure you want to proceed?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onProceed,
                colors = ButtonDefaults.buttonColors(containerColor = DestructiveRed),
            ) {
                Text("Yes, Execute", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
        },
    )
}

/**
 * Dialog shown when a command step fails mid-execution, asking the user whether
 * to continue with remaining steps (Story 5.4).
 *
 * @param failedCommand The command text that failed.
 * @param errorMessage The error message from the transport layer.
 * @param remainingCount Number of approved steps that have not yet been executed.
 * @param onContinue Called when the user wants to proceed with remaining steps.
 * @param onStop Called when the user wants to stop execution.
 */
@Composable
fun ExecutionFailureDialog(
    failedCommand: String,
    errorMessage: String,
    remainingCount: Int,
    onContinue: () -> Unit,
    onStop: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { /* non-dismissible */ },
        icon = {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = DestructiveRed,
                modifier = Modifier.size(32.dp),
            )
        },
        title = { Text("Command Failed") },
        text = {
            Column {
                Text(
                    text = "Command failed: $failedCommand",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (remainingCount > 0) {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "$remainingCount ${if (remainingCount == 1) "step" else "steps"} remaining.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "Continue with the remaining commands?",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        },
        confirmButton = {
            if (remainingCount > 0) {
                Button(onClick = onContinue) {
                    Text("Continue")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onStop) {
                Text("Stop Here")
            }
        },
    )
}

/**
 * A simple informational dialog for LLM / API errors (Story 5.2).
 *
 * @param message The user-facing error message to display.
 * @param onRetry Called when the user wants to retry the failed operation.
 * @param onDismiss Called when the user dismisses the dialog.
 */
@Composable
fun CommandErrorDialog(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Command Error") },
        text = {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            Button(onClick = onRetry) {
                Text("Retry")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        },
    )
}
