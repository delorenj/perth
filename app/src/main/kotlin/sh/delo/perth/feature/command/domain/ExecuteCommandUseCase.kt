package sh.delo.perth.feature.command.domain

import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.domain.repository.CommandAuditRepository
import sh.delo.perth.core.network.ZealotTransport
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

/**
 * Executes the approved steps in a [CommandPlan] sequentially against the
 * active pane via [ZealotTransport.sendCommand].
 *
 * Behaviour:
 * - Only [CommandStep.isApproved] steps are sent.
 * - Each step is logged to the audit table via [CommandAuditRepository] regardless
 *   of outcome (success or failure).
 * - On failure, execution halts and returns the error so the caller can ask the
 *   user whether to continue.
 */
class ExecuteCommandUseCase @Inject constructor(
    private val transport: ZealotTransport,
    private val auditRepository: CommandAuditRepository,
) {
    /**
     * Executes all approved steps in [plan] against [paneId].
     *
     * Returns a list of [StepResult] - one per approved step that was attempted.
     * If a step fails, the list ends at that step (remaining steps are not executed).
     */
    suspend operator fun invoke(
        plan: CommandPlan,
        paneId: PaneId,
        sessionId: String,
    ): List<StepResult> {
        val results = mutableListOf<StepResult>()

        for (step in plan.approvedSteps) {
            Timber.d("ExecuteCommandUseCase: executing command='%s'", step.command)

            val transportResult = transport.sendCommand(paneId, step.command)
            val stepResult = when (transportResult) {
                is AppResult.Success -> {
                    StepResult(
                        step = step,
                        outcome = StepOutcome.Success(output = transportResult.data),
                    )
                }
                is AppResult.Error -> {
                    StepResult(
                        step = step,
                        outcome = StepOutcome.Failure(error = transportResult.exception),
                    )
                }
            }

            // Audit log every attempt, approved or failed
            val auditEntry = CommandAuditRepository.AuditEntry(
                timestamp = Instant.now(),
                sessionId = sessionId,
                paneId = paneId.value,
                transcript = plan.originalTranscript,
                interpretedCommand = step.command,
                userApproved = true,
                executionResult = when (val outcome = stepResult.outcome) {
                    is StepOutcome.Success -> outcome.output
                    is StepOutcome.Failure -> "ERROR: ${outcome.error.message}"
                },
            )
            val auditResult = auditRepository.record(auditEntry)
            auditResult.exceptionOrNull()?.let { e ->
                Timber.e(e, "ExecuteCommandUseCase: audit log write failed for command='%s'", step.command)
            }

            results.add(stepResult)

            // Stop on first failure - caller decides whether to continue
            if (stepResult.outcome is StepOutcome.Failure) {
                Timber.w("ExecuteCommandUseCase: halting after failed step command='%s'", step.command)
                break
            }
        }

        return results
    }

    /** The outcome for a single executed step. */
    data class StepResult(
        val step: CommandStep,
        val outcome: StepOutcome,
    )

    sealed interface StepOutcome {
        data class Success(val output: String) : StepOutcome
        data class Failure(val error: AppException) : StepOutcome
    }
}
