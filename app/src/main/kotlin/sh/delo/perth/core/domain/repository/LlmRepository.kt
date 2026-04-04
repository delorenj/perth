package sh.delo.perth.core.domain.repository

import sh.delo.perth.core.result.AppResult
import sh.delo.perth.feature.command.domain.CommandPlan

/** Repository for LLM-driven command interpretation. */
interface LlmRepository {

    /** Interprets a natural-language [transcript] into a shell command string. */
    suspend fun interpretCommand(transcript: String): AppResult<String>

    /**
     * Interprets a natural-language [transcript] into a structured [CommandPlan]
     * with ordered steps, each containing a command string and initial risk level.
     * The safety gate re-classifies steps after this call.
     */
    suspend fun interpretCommandPlan(transcript: String): AppResult<CommandPlan>

    /** Validates the stored API key with a lightweight probe request. */
    suspend fun validateApiKey(): AppResult<Unit>

    /** Classifies whether [command] is destructive (requires extra confirmation). */
    suspend fun classifyRisk(command: String): AppResult<RiskLevel>

    enum class RiskLevel {
        Safe,
        Moderate,
        Destructive,
    }
}
