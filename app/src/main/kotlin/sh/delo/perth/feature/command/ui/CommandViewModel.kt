package sh.delo.perth.feature.command.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.domain.repository.SettingsRepository
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import sh.delo.perth.feature.command.domain.CommandPlan
import sh.delo.perth.feature.command.domain.ExecuteCommandUseCase
import sh.delo.perth.feature.command.domain.InterpretCommandUseCase
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the command interpretation and execution flow (Stories 5.1-5.4).
 *
 * Exposes a single [StateFlow] of [CommandUiState]. All user actions are handled
 * as intent functions that drive state transitions through [CommandPhase].
 */
@HiltViewModel
class CommandViewModel @Inject constructor(
    private val interpretCommandUseCase: InterpretCommandUseCase,
    private val executeCommandUseCase: ExecuteCommandUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CommandUiState())
    val state: StateFlow<CommandUiState> = _state.asStateFlow()

    init {
        checkApiKey()
    }

    // ---------------------------------------------------------------------------
    // Intent handlers
    // ---------------------------------------------------------------------------

    /**
     * Called by the Terminal screen when a voice transcript is ready for
     * command interpretation (Story 5.2).
     */
    fun onTranscriptReceived(transcript: String) {
        if (transcript.isBlank()) {
            setError(
                AppException.Command("Transcript is empty. Please try again."),
                "Could not determine a command. Try rephrasing.",
            )
            return
        }
        _state.update { it.copy(transcript = transcript, phase = CommandPhase.Interpreting) }
        interpretTranscript(transcript)
    }

    /** Retries interpretation of the last transcript. */
    fun onRetryInterpret() {
        val transcript = _state.value.transcript
        if (transcript.isBlank()) return
        _state.update { it.copy(phase = CommandPhase.Interpreting) }
        interpretTranscript(transcript)
    }

    /**
     * Toggles the approved state of the step at [index] in the current plan
     * (Story 5.3).
     */
    fun onToggleStepApproval(index: Int) {
        val plan = _state.value.currentPlan ?: return
        val updated = plan.toggleStep(index)
        _state.update { it.copy(phase = CommandPhase.ReviewPlan(updated)) }
    }

    /** Approves all steps in the current plan (Story 5.3). */
    fun onApproveAll() {
        val plan = _state.value.currentPlan ?: return
        _state.update { it.copy(phase = CommandPhase.ReviewPlan(plan.approveAll())) }
    }

    /** Rejects all steps in the current plan (Story 5.3). */
    fun onRejectAll() {
        val plan = _state.value.currentPlan ?: return
        _state.update { it.copy(phase = CommandPhase.ReviewPlan(plan.rejectAll())) }
    }

    /**
     * Executes all approved steps in the current plan against [paneId] (Story 5.4).
     * The safety gate has already run during interpretation; this just executes.
     */
    fun onExecuteApproved(paneId: PaneId, sessionId: String) {
        val plan = _state.value.currentPlan ?: return
        if (!plan.hasApprovedSteps) {
            Timber.w("CommandViewModel: execute called with no approved steps")
            return
        }
        _state.update { it.copy(phase = CommandPhase.Executing) }
        executeApprovedSteps(plan, paneId, sessionId)
    }

    /**
     * After execution fails mid-plan, the user may choose to continue with
     * remaining steps. This resumes from the step after the failure.
     */
    fun onContinueAfterFailure(paneId: PaneId, sessionId: String) {
        val completionPhase = _state.value.phase as? CommandPhase.ExecutionComplete ?: return
        val failedIndex = completionPhase.failedIndex ?: return
        val originalPlan = _state.value.currentPlan ?: return

        // Build a continuation plan from the steps after the failure
        val remainingSteps = originalPlan.approvedSteps.drop(failedIndex + 1)
        if (remainingSteps.isEmpty()) {
            resetToIdle()
            return
        }

        val continuationPlan = CommandPlan(
            originalTranscript = originalPlan.originalTranscript,
            steps = remainingSteps,
        )
        _state.update { it.copy(phase = CommandPhase.Executing) }
        executeApprovedSteps(continuationPlan, paneId, sessionId)
    }

    /** Resets the command flow back to idle (e.g., after completion or cancel). */
    fun resetToIdle() {
        _state.update { CommandUiState(isApiKeyConfigured = it.isApiKeyConfigured) }
    }

    fun onDismissError() {
        _state.update { it.copy(phase = CommandPhase.Idle) }
    }

    // ---------------------------------------------------------------------------
    // Internal helpers
    // ---------------------------------------------------------------------------

    private fun checkApiKey() {
        viewModelScope.launch {
            val hasKey = settingsRepository.getLlmApiKey() != null
            _state.update { it.copy(isApiKeyConfigured = hasKey) }
        }
    }

    private fun interpretTranscript(transcript: String) {
        viewModelScope.launch {
            Timber.d("CommandViewModel: interpreting transcript='%s'", transcript)
            val result = interpretCommandUseCase(transcript)
            when (result) {
                is AppResult.Success -> {
                    val plan = result.data
                    if (plan.steps.isEmpty()) {
                        setError(
                            AppException.Command("No commands produced."),
                            "Could not determine a command. Try rephrasing.",
                        )
                    } else {
                        _state.update { it.copy(phase = CommandPhase.ReviewPlan(plan)) }
                        Timber.d("CommandViewModel: plan ready with %d steps", plan.steps.size)
                    }
                }
                is AppResult.Error -> {
                    val ex = result.exception
                    Timber.e(ex, "CommandViewModel: interpretation failed")
                    val message = when (ex) {
                        is AppException.Command -> ex.message ?: "Command interpretation failed."
                        is AppException.Network -> "Network error. Check connection and retry."
                        else -> "Command interpretation failed. Check API key or retry."
                    }
                    setError(ex, message)
                }
            }
        }
    }

    private fun executeApprovedSteps(plan: CommandPlan, paneId: PaneId, sessionId: String) {
        viewModelScope.launch {
            Timber.d(
                "CommandViewModel: executing %d approved steps for session=%s pane=%s",
                plan.approvedSteps.size,
                sessionId,
                paneId.value,
            )

            val results = executeCommandUseCase(plan, paneId, sessionId)
            val failedIndex = results.indexOfFirst {
                it.outcome is ExecuteCommandUseCase.StepOutcome.Failure
            }.takeIf { it >= 0 }

            _state.update {
                it.copy(
                    phase = CommandPhase.ExecutionComplete(
                        results = results,
                        failedIndex = failedIndex,
                    ),
                )
            }

            if (failedIndex != null) {
                Timber.w(
                    "CommandViewModel: execution halted at step %d",
                    failedIndex,
                )
            } else {
                Timber.d("CommandViewModel: all steps completed successfully")
            }
        }
    }

    private fun setError(exception: AppException, message: String) {
        _state.update { it.copy(phase = CommandPhase.Error(exception, message)) }
    }
}
