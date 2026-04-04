package sh.delo.perth.feature.session.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.domain.repository.SessionRepository
import sh.delo.perth.core.domain.repository.SettingsRepository
import sh.delo.perth.core.network.ZealotTransport
import sh.delo.perth.feature.session.domain.ConnectToSessionUseCase
import sh.delo.perth.feature.session.domain.GetSessionsUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SessionListViewModel @Inject constructor(
    private val transport: ZealotTransport,
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
    private val getSessionsUseCase: GetSessionsUseCase,
    private val connectToSessionUseCase: ConnectToSessionUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(SessionListUiState())
    val state: StateFlow<SessionListUiState> = _state.asStateFlow()

    init {
        observeConnectionState()
        observeSessions()
        autoConnectFromSettings()
    }

    private fun observeConnectionState() {
        transport.connectionState
            .onEach { connectionState ->
                _state.update { it.copy(connectionState = connectionState) }
            }
            .launchIn(viewModelScope)
    }

    private fun observeSessions() {
        getSessionsUseCase()
            .onEach { sessions ->
                _state.update { it.copy(sessions = sessions, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Reads the saved server config from settings and connects automatically.
     * If no config is saved the user is prompted to visit Settings.
     */
    private fun autoConnectFromSettings() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val config = settingsRepository.getServerConfig()
            if (config == null) {
                Timber.d("No server config found — showing empty state")
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            val result = sessionRepository.connect(config)
            result.exceptionOrNull()?.let { error ->
                Timber.e(error, "Auto-connect failed")
                _state.update { it.copy(isLoading = false, error = error) }
            }
        }
    }

    /** Pull-to-refresh: re-fetches the session list from the server. */
    fun onRefresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true, error = null) }
            val result = sessionRepository.refreshSessions()
            _state.update { it.copy(isRefreshing = false) }
            result.exceptionOrNull()?.let { error ->
                Timber.e(error, "Refresh failed")
                _state.update { it.copy(error = error) }
            }
        }
    }

    /** Retry the server connection (e.g., after an error or manual disconnect). */
    fun onRetryConnect() {
        autoConnectFromSettings()
    }

    /**
     * Called when the user taps a session card.
     * Saves the session to Room as recently visited before navigating.
     */
    fun onSessionSelected(session: ZellijSession) {
        viewModelScope.launch {
            val serverUrl = settingsRepository.getServerConfig()?.url ?: ""
            val result = connectToSessionUseCase(session, serverUrl)
            result.exceptionOrNull()?.let { error ->
                Timber.e(error, "Failed to mark session as visited: sessionId=%s", session.id)
                // Non-fatal; navigation still proceeds via UI callback
            }
        }
    }

    fun onDismissError() {
        _state.update { it.copy(error = null) }
    }
}
