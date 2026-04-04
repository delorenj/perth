package sh.delo.perth.feature.command.domain

import sh.delo.perth.core.domain.repository.LlmRepository
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import javax.inject.Inject

/**
 * Interprets a voice transcript into a [CommandPlan] by calling the LLM and
 * then running every step through [CommandSafetyGate].
 *
 * This use case owns the full pipeline:
 *   transcript -> LLM -> raw plan -> safety classification -> [CommandPlan]
 */
class InterpretCommandUseCase @Inject constructor(
    private val llmRepository: LlmRepository,
    private val safetyGate: CommandSafetyGate,
) {
    /**
     * Returns a [CommandPlan] whose steps have been classified by [CommandSafetyGate].
     * Returns [AppResult.Error] with [AppException.Command] if interpretation fails.
     */
    suspend operator fun invoke(transcript: String): AppResult<CommandPlan> {
        if (transcript.isBlank()) {
            return AppResult.Error(
                AppException.Command("Transcript is empty. Please try again."),
            )
        }

        // Ask the LLM repository for an interpreted plan. LlmRepositoryImpl returns
        // a CommandPlan; the interface currently returns String so the impl is cast.
        return when (val result = llmRepository.interpretCommandPlan(transcript)) {
            is AppResult.Success -> {
                // Apply safety gate to every step in the plan
                val classifiedPlan = safetyGate.applyTo(result.data)
                AppResult.Success(classifiedPlan)
            }
            is AppResult.Error -> result
        }
    }
}
