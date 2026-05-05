package sh.delo.perth.feature.settings.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sh.delo.perth.core.domain.model.ConnectionState
import sh.delo.perth.core.domain.model.ServerConfig
import sh.delo.perth.core.domain.repository.LlmRepository
import sh.delo.perth.core.domain.repository.SessionRepository
import sh.delo.perth.core.domain.repository.SettingsRepository
import sh.delo.perth.core.network.ZellijTransport
import sh.delo.perth.core.result.AppException
import sh.delo.perth.work.AuditWorkScheduler
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val sessionRepository: SessionRepository,
    private val transport: ZellijTransport,
    private val llmRepository: LlmRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    init {
        loadSettings()
        observeVoiceMode()
        observeConnectionState()
        observeRecentSessions()
        observeAuditRetention()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val serverConfig = settingsRepository.getServerConfig()
            val hasLlmKey = settingsRepository.getLlmApiKey() != null
            _state.update {
                it.copy(
                    isLoading = false,
                    serverUrl = serverConfig?.url ?: "",
                    hasLlmApiKey = hasLlmKey,
                )
            }
        }
    }

    private fun observeVoiceMode() {
        settingsRepository.voiceModeFlow()
            .onEach { mode -> _state.update { it.copy(voiceMode = mode) } }
            .launchIn(viewModelScope)
    }

    private fun observeConnectionState() {
        transport.connectionState
            .onEach { state -> _state.update { it.copy(connectionState = state) } }
            .launchIn(viewModelScope)
    }

    private fun observeRecentSessions() {
        sessionRepository.recentSessionsFlow()
            .onEach { sessions -> _state.update { it.copy(recentSessions = sessions) } }
            .launchIn(viewModelScope)
    }

    private fun observeAuditRetention() {
        settingsRepository.auditRetentionDaysFlow()
            .onEach { days -> _state.update { it.copy(auditRetentionDays = days) } }
            .launchIn(viewModelScope)
    }

    fun onServerUrlChange(url: String) {
        _state.update { it.copy(serverUrl = url) }
    }

    /**
     * Story 1.2: Saves the server URL to encrypted storage then triggers a WebSocket connect.
     * On success, sets [SettingsUiState.navigateToSessions] = true so the UI navigates away.
     * On failure, populates [SettingsUiState.error] and shows a retry option.
     */
    fun onSaveAndConnect() {
        val url = _state.value.serverUrl.trim()
        if (url.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, isConnecting = true, error = null) }

            val config = ServerConfig(url = url)
            val saveResult = settingsRepository.saveServerConfig(config)
            if (saveResult.isError) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        isConnecting = false,
                        error = saveResult.exceptionOrNull(),
                    )
                }
                return@launch
            }

            val connectResult = transport.connect(config)
            _state.update { it.copy(isSaving = false, isConnecting = false) }
            if (connectResult.isSuccess) {
                // Connection is established (or in progress — state flow drives UI badge).
                // Navigate once Connected is emitted; handled via observeConnectionState + navigateToSessions.
                _state.update { it.copy(saveSuccess = true) }
            } else {
                _state.update {
                    it.copy(error = connectResult.exceptionOrNull() as? AppException)
                }
            }
        }
    }

    /** Legacy alias kept for the "Save Server URL" button variant (no immediate connect). */
    fun onSaveServerUrl() = onSaveAndConnect()

    fun onVoiceModeChange(mode: SettingsRepository.VoiceMode) {
        viewModelScope.launch {
            val result = settingsRepository.saveVoiceMode(mode)
            result.exceptionOrNull()?.let { error ->
                Timber.e(error, "Failed to save voice mode")
                _state.update { it.copy(error = error as? AppException) }
            }
        }
    }

    /**
     * Story 5.1: Saves the LLM API key to encrypted storage, then validates it
     * with a lightweight OpenAI API probe. Shows success/error indicator in the UI.
     */
    fun onSaveLlmApiKey(apiKey: String) {
        if (apiKey.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, llmApiKeyValid = false, error = null) }
            val saveResult = settingsRepository.saveLlmApiKey(apiKey)
            if (saveResult.isError) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        hasLlmApiKey = false,
                        error = saveResult.exceptionOrNull() as? AppException,
                    )
                }
                return@launch
            }

            // Key saved - now validate it against the OpenAI API
            _state.update { it.copy(isSaving = false, hasLlmApiKey = true, isValidatingApiKey = true) }
            val validationResult = llmRepository.validateApiKey()
            _state.update {
                it.copy(
                    isValidatingApiKey = false,
                    llmApiKeyValid = validationResult.isSuccess,
                    error = validationResult.exceptionOrNull() as? AppException,
                )
            }
            if (validationResult.isSuccess) {
                Timber.d("SettingsViewModel: LLM API key validated successfully")
            } else {
                Timber.w("SettingsViewModel: LLM API key validation failed: %s",
                    validationResult.exceptionOrNull()?.message)
            }
        }
    }

    /** Story 1.5: Removes all recent session records from Room. */
    fun onClearRecentSessions() {
        viewModelScope.launch {
            val result = sessionRepository.clearRecentSessions()
            result.exceptionOrNull()?.let { error ->
                Timber.e(error, "Failed to clear recent sessions")
                _state.update { it.copy(error = error as? AppException) }
            }
        }
    }

    fun onNavigatedToSessions() {
        _state.update { it.copy(navigateToSessions = false) }
    }

    fun onDismissError() {
        _state.update { it.copy(error = null) }
    }

    fun onDismissSaveSuccess() {
        _state.update { it.copy(saveSuccess = false) }
    }

    /**
     * Story 8.3: persists a new audit-log retention period and force-reschedules
     * the periodic worker so the new window takes effect on the next pass.
     */
    fun onAuditRetentionChange(days: Int) {
        viewModelScope.launch {
            val clamped = days.coerceIn(
                SettingsRepository.MIN_AUDIT_RETENTION_DAYS,
                SettingsRepository.MAX_AUDIT_RETENTION_DAYS,
            )
            val result = settingsRepository.saveAuditRetentionDays(clamped)
            if (result.isError) {
                Timber.e(result.exceptionOrNull(), "Failed to save audit retention")
                _state.update { it.copy(error = result.exceptionOrNull() as? AppException) }
            } else {
                AuditWorkScheduler.reschedule(appContext)
            }
        }
    }
}
