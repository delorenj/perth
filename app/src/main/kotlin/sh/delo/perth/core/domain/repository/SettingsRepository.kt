package sh.delo.perth.core.domain.repository

import kotlinx.coroutines.flow.Flow
import sh.delo.perth.core.domain.model.ServerConfig
import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.result.AppResult

/** Repository for user preferences and app settings. */
interface SettingsRepository {

    /** Emits the current server configuration whenever it changes. */
    fun serverConfigFlow(): Flow<ServerConfig?>

    /** Returns the currently saved server configuration. */
    suspend fun getServerConfig(): ServerConfig?

    /** Saves the server [config]. */
    suspend fun saveServerConfig(config: ServerConfig): AppResult<Unit>

    /** Clears the saved server configuration. */
    suspend fun clearServerConfig(): AppResult<Unit>

    /** Returns the saved LLM API key, or null if not set. */
    suspend fun getLlmApiKey(): String?

    /** Saves the LLM [apiKey] in encrypted storage. */
    suspend fun saveLlmApiKey(apiKey: String): AppResult<Unit>

    /** Emits the preferred voice mode. */
    fun voiceModeFlow(): Flow<VoiceMode>

    /** Saves the preferred [voiceMode]. */
    suspend fun saveVoiceMode(voiceMode: VoiceMode): AppResult<Unit>

    /**
     * Emits the audit-log retention period in days. Story 8.3.
     * Default is [DEFAULT_AUDIT_RETENTION_DAYS] when unset.
     */
    fun auditRetentionDaysFlow(): Flow<Int>

    /** One-shot read of the retention preference (handy for the worker). */
    suspend fun getAuditRetentionDays(): Int

    /** Saves the audit-log retention period. Caller validates the range. */
    suspend fun saveAuditRetentionDays(days: Int): AppResult<Unit>

    enum class VoiceMode {
        Transcription,
        Task,
        Command,
    }

    companion object {
        /** Default retention period for the command audit log (Story 8.3). */
        const val DEFAULT_AUDIT_RETENTION_DAYS: Int = 90

        /** Lower bound. Anything shorter risks losing recently-relevant audit context. */
        const val MIN_AUDIT_RETENTION_DAYS: Int = 1

        /** Upper bound. Beyond a year, retention should be enforced server-side instead. */
        const val MAX_AUDIT_RETENTION_DAYS: Int = 365
    }
}
