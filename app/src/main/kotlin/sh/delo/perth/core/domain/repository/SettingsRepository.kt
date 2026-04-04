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

    enum class VoiceMode {
        Transcription,
        Task,
        Command,
    }
}
