package sh.delo.perth.feature.command.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import sh.delo.perth.feature.command.domain.CommandPlan
import sh.delo.perth.feature.command.domain.CommandStep
import sh.delo.perth.feature.command.domain.ExecuteCommandUseCase
import sh.delo.perth.feature.command.domain.SafetyClassification

// ---------------------------------------------------------------------------
// Color tokens for risk levels
// ---------------------------------------------------------------------------
private val SafeColor = Color(0xFF2E7D32)      // Material green-800
private val CautionColor = Color(0xFFF9A825)   // Material amber-800
private val DestructiveColor = Color(0xFFC62828) // Material red-900

private val SafeContainerColor = Color(0xFFE8F5E9)
private val CautionContainerColor = Color(0xFFFFF8E1)
private val DestructiveContainerColor = Color(0xFFFFEBEE)

// ---------------------------------------------------------------------------
// Public composables
// ---------------------------------------------------------------------------

/**
 * Displays the [CommandPlan] for user review.
 *
 * Each step shows its description, shell command, risk indicator, and a checkbox
 * for approval. Bulk approve/reject buttons appear at the top.
 *
 * Story 5.2 display + Story 5.3 confirmation gate.
 */
@Composable
fun CommandPlanView(
    plan: CommandPlan,
    onToggleStep: (index: Int) -> Unit,
    onApproveAll: () -> Unit,
    onRejectAll: () -> Unit,
    onExecute: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TranscriptHeader(transcript = plan.originalTranscript)

        BulkActionRow(
            hasApprovedSteps = plan.hasApprovedSteps,
            onApproveAll = onApproveAll,
            onRejectAll = onRejectAll,
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false),
        ) {
            itemsIndexed(plan.steps) { index, step ->
                CommandStepCard(
                    step = step,
                    stepNumber = index + 1,
                    onToggle = { onToggleStep(index) },
                )
            }
        }

        if (plan.hasDestructiveSteps) {
            DestructiveWarningBanner()
        }

        ExecuteRow(
            canExecute = plan.hasApprovedSteps,
            onExecute = onExecute,
            onCancel = onCancel,
        )
    }
}

/**
 * Displays the results of command execution (Story 5.4).
 */
@Composable
fun ExecutionResultsView(
    results: List<ExecuteCommandUseCase.StepResult>,
    failedIndex: Int?,
    onContinue: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Execution Results",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )

        results.forEachIndexed { index, stepResult ->
            ExecutionStepCard(stepResult = stepResult, stepNumber = index + 1)
        }

        Spacer(Modifier.height(4.dp))

        if (failedIndex != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDone,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Stop Here")
                }
                Button(
                    onClick = onContinue,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Continue")
                }
            }
        } else {
            Button(
                onClick = onDone,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Done")
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Internal composables
// ---------------------------------------------------------------------------

@Composable
private fun TranscriptHeader(transcript: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = "You said:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = transcript,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun BulkActionRow(
    hasApprovedSteps: Boolean,
    onApproveAll: () -> Unit,
    onRejectAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = onApproveAll,
            modifier = Modifier.weight(1f),
        ) {
            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Approve All")
        }
        OutlinedButton(
            onClick = onRejectAll,
            modifier = Modifier.weight(1f),
        ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Reject All")
        }
    }
}

@Composable
private fun CommandStepCard(
    step: CommandStep,
    stepNumber: Int,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (indicatorColor, containerColor, riskLabel) = when (step.safetyClassification) {
        SafetyClassification.Safe -> Triple(SafeColor, SafeContainerColor, "Safe")
        SafetyClassification.Caution -> Triple(CautionColor, CautionContainerColor, "Caution")
        SafetyClassification.Destructive -> Triple(DestructiveColor, DestructiveContainerColor, "Destructive")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        border = if (step.isApproved) BorderStroke(2.dp, indicatorColor) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (step.isApproved) containerColor else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Checkbox(
                checked = step.isApproved,
                onCheckedChange = { onToggle() },
            )
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                // Step header: number + risk badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "Step $stepNumber",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    RiskBadge(
                        label = riskLabel,
                        color = indicatorColor,
                        classification = step.safetyClassification,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.small,
                ) {
                    Text(
                        text = step.command,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                if (step.safetyClassification == SafetyClassification.Destructive) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Warning: This command may cause irreversible data loss.",
                        style = MaterialTheme.typography.labelSmall,
                        color = DestructiveColor,
                        fontWeight = FontWeight.Medium,
                    )
                } else if (step.safetyClassification == SafetyClassification.Caution) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Review carefully before approving.",
                        style = MaterialTheme.typography.labelSmall,
                        color = CautionColor,
                    )
                }
            }
        }
    }
}

@Composable
private fun RiskBadge(
    label: String,
    color: Color,
    classification: SafetyClassification,
    modifier: Modifier = Modifier,
) {
    val icon = when (classification) {
        SafetyClassification.Safe -> Icons.Default.CheckCircle
        SafetyClassification.Caution -> Icons.Default.Warning
        SafetyClassification.Destructive -> Icons.Default.Error
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun DestructiveWarningBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = DestructiveContainerColor,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = DestructiveColor,
            )
            Text(
                text = "This plan contains destructive commands. Review each step carefully before approving.",
                style = MaterialTheme.typography.bodySmall,
                color = DestructiveColor,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun ExecuteRow(
    canExecute: Boolean,
    onExecute: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
        ) {
            Text("Cancel")
        }
        Button(
            onClick = onExecute,
            enabled = canExecute,
            modifier = Modifier.weight(1f),
        ) {
            Text("Execute")
        }
    }
}

@Composable
private fun ExecutionStepCard(
    stepResult: ExecuteCommandUseCase.StepResult,
    stepNumber: Int,
    modifier: Modifier = Modifier,
) {
    val isSuccess = stepResult.outcome is ExecuteCommandUseCase.StepOutcome.Success
    val (containerColor, statusColor, statusText) = if (isSuccess) {
        Triple(SafeContainerColor, SafeColor, "Success")
    } else {
        Triple(DestructiveContainerColor, DestructiveColor, "Failed")
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, statusColor),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Step $stepNumber",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = statusText,
                    tint = statusColor,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Medium,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = stepResult.step.command,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
            )
            when (val outcome = stepResult.outcome) {
                is ExecuteCommandUseCase.StepOutcome.Success -> {
                    if (outcome.output.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = outcome.output,
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                is ExecuteCommandUseCase.StepOutcome.Failure -> {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Error: ${outcome.error.message}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DestructiveColor,
                    )
                }
            }
        }
    }
}
