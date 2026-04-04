package sh.delo.perth.feature.command.ui

import sh.delo.perth.core.result.AppException
import sh.delo.perth.feature.command.domain.CommandPlan
import sh.delo.perth.feature.command.domain.ExecuteCommandUseCase

/** All possible phases of the command flow. */
sealed interface CommandPhase {
    /** Waiting for a transcript to interpret. */
    data object Idle : CommandPhase

    /** LLM is interpreting the transcript. */
    data object Interpreting : CommandPhase

    /**
     * LLM returned a plan. User must review and approve/reject steps.
     * @param plan The classified command plan.
     */
    data class ReviewPlan(val plan: CommandPlan) : CommandPhase

    /** Approved commands are being sent to the active pane. */
    data object Executing : CommandPhase

    /**
     * Execution finished (all approved steps ran or halted on error).
     * @param results Per-step outcomes.
     * @param failedIndex Index into [results] of the first failure, or null if all succeeded.
     */
    data class ExecutionComplete(
        val results: List<ExecuteCommandUseCase.StepResult>,
        val failedIndex: Int?,
    ) : CommandPhase

    /**
     * A non-recoverable error occurred (LLM failure, no API key, etc.).
     * @param error The underlying exception.
     * @param message User-friendly message to display.
     */
    data class Error(
        val error: AppException,
        val message: String,
    ) : CommandPhase
}

/**
 * Single source of truth for the command feature screen.
 *
 * @param phase Current processing phase.
 * @param transcript The original voice transcript being processed.
 * @param isApiKeyConfigured Whether an OpenAI API key is available.
 */
data class CommandUiState(
    val phase: CommandPhase = CommandPhase.Idle,
    val transcript: String = "",
    val isApiKeyConfigured: Boolean = false,
) {
    val isLoading: Boolean
        get() = phase is CommandPhase.Interpreting || phase is CommandPhase.Executing

    val currentPlan: CommandPlan?
        get() = (phase as? CommandPhase.ReviewPlan)?.plan

    val executionResults: List<ExecuteCommandUseCase.StepResult>?
        get() = (phase as? CommandPhase.ExecutionComplete)?.results

    val error: AppException?
        get() = (phase as? CommandPhase.Error)?.error
}
