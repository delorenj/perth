package sh.delo.perth.feature.settings.ui

import sh.delo.perth.core.domain.model.ConnectionState
import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.domain.repository.SettingsRepository
import sh.delo.perth.core.result.AppException

data class SettingsUiState(
    val isLoading: Boolean = true,
    val serverUrl: String = "",
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val isConnecting: Boolean = false,
    val voiceMode: SettingsRepository.VoiceMode = SettingsRepository.VoiceMode.Transcription,
    val hasLlmApiKey: Boolean = false,
    /** True when a stored key has been validated successfully against the OpenAI API. */
    val llmApiKeyValid: Boolean = false,
    /** True while an API key validation request is in-flight. */
    val isValidatingApiKey: Boolean = false,
    val isSaving: Boolean = false,
    val error: AppException? = null,
    val saveSuccess: Boolean = false,
    val navigateToSessions: Boolean = false,
    val recentSessions: List<ZellijSession> = emptyList(),
)
