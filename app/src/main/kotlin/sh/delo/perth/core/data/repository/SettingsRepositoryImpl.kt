package sh.delo.perth.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import sh.delo.perth.core.data.datastore.UserPreferences
import sh.delo.perth.core.data.secure.SecureStorage
import sh.delo.perth.core.domain.model.ServerConfig
import sh.delo.perth.core.domain.repository.SettingsRepository
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import sh.delo.perth.core.result.runCatchingAppResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val userPreferences: UserPreferences,
    private val secureStorage: SecureStorage,
) : SettingsRepository {

    override fun serverConfigFlow(): Flow<ServerConfig?> =
        userPreferences.serverUrlFlow.map { url ->
            url?.let {
                ServerConfig(
                    url = it,
                    authToken = secureStorage.getString(SecureStorage.KEY_SERVER_AUTH_TOKEN),
                )
            }
        }

    override suspend fun getServerConfig(): ServerConfig? {
        val url = userPreferences.serverUrlFlow.first()
        return url?.let {
            ServerConfig(
                url = it,
                authToken = secureStorage.getString(SecureStorage.KEY_SERVER_AUTH_TOKEN),
            )
        }
    }

    override suspend fun saveServerConfig(config: ServerConfig): AppResult<Unit> =
        runCatchingAppResult(errorMapper = { AppException.Storage(it.message ?: "Save failed", it) }) {
            userPreferences.saveServerUrl(config.url)
            config.authToken?.let { secureStorage.putString(SecureStorage.KEY_SERVER_AUTH_TOKEN, it) }
        }

    override suspend fun clearServerConfig(): AppResult<Unit> =
        runCatchingAppResult(errorMapper = { AppException.Storage(it.message ?: "Clear failed", it) }) {
            userPreferences.clearServerUrl()
            secureStorage.remove(SecureStorage.KEY_SERVER_AUTH_TOKEN)
        }

    override suspend fun getLlmApiKey(): String? =
        secureStorage.getString(SecureStorage.KEY_LLM_API_KEY)

    override suspend fun saveLlmApiKey(apiKey: String): AppResult<Unit> =
        runCatchingAppResult(errorMapper = { AppException.Storage(it.message ?: "Save failed", it) }) {
            secureStorage.putString(SecureStorage.KEY_LLM_API_KEY, apiKey)
        }

    override fun voiceModeFlow(): Flow<SettingsRepository.VoiceMode> =
        userPreferences.voiceModeFlow

    override suspend fun saveVoiceMode(voiceMode: SettingsRepository.VoiceMode): AppResult<Unit> =
        runCatchingAppResult(errorMapper = { AppException.Storage(it.message ?: "Save failed", it) }) {
            userPreferences.saveVoiceMode(voiceMode)
        }
}
