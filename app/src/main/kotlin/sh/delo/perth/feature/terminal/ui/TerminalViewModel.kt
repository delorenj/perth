package sh.delo.perth.feature.terminal.ui

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
import sh.delo.perth.core.domain.model.ConnectionState
import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.domain.model.ZellijTab
import sh.delo.perth.core.domain.repository.SessionRepository
import sh.delo.perth.core.domain.repository.SettingsRepository
import sh.delo.perth.core.network.ZellijTransport
import sh.delo.perth.feature.terminal.domain.NavigatePaneUseCase
import sh.delo.perth.feature.terminal.domain.SendInputUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val transport: ZellijTransport,
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
    private val navigatePaneUseCase: NavigatePaneUseCase,
    private val sendInputUseCase: SendInputUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(TerminalUiState())
    val state: StateFlow<TerminalUiState> = _state.asStateFlow()

    init {
        observeConnectionState()
    }

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            transport.sessionFlow()
                .onEach { sessions ->
                    val session = sessions.firstOrNull { it.id == sessionId }
                    _state.update { it.copy(session = session, isLoading = false) }

                    // Auto-select an active pane for the current tab when the session loads
                    session?.tabs?.getOrNull(_state.value.activeTabIndex)?.let { tab ->
                        autoSelectPane(tab)
                    }

                    // Subscribe to output for every pane in the session
                    session?.tabs?.forEach { tab ->
                        tab.panes.forEach { pane ->
                            observePaneOutput(pane.id)
                        }
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun onTabSelected(index: Int) {
        val tab = _state.value.activeTabs.getOrNull(index) ?: return
        _state.update { it.copy(activeTabIndex = index, activePaneId = null) }
        autoSelectPane(tab)
    }

    fun onPaneSelected(paneId: PaneId) {
        val tab = _state.value.activeTab ?: return
        val result = navigatePaneUseCase(tab, paneId)
        result.getOrNull()?.let { resolvedId ->
            _state.update { it.copy(activePaneId = resolvedId) }
        }
        result.exceptionOrNull()?.let { error ->
            Timber.w(error, "onPaneSelected failed paneId=%s", paneId)
            _state.update { it.copy(error = error) }
        }
    }

    fun onSendInput(input: String) {
        val paneId = _state.value.activePaneId
        viewModelScope.launch {
            val result = sendInputUseCase(paneId, input)
            result.exceptionOrNull()?.let { error ->
                Timber.e(error, "Failed to send input to pane=%s", paneId)
                _state.update { it.copy(error = error) }
            }
        }
    }

    /**
     * Story 1.4: Triggered by the manual Reconnect button after auto-reconnect is exhausted.
     * Reads the last saved server config and initiates a fresh WebSocket connect.
     */
    fun onManualReconnect() {
        viewModelScope.launch {
            val config = settingsRepository.getServerConfig()
            if (config == null) {
                Timber.w("Manual reconnect requested but no server config saved")
                return@launch
            }
            _state.update { it.copy(isReconnecting = true, reconnectFailed = false, error = null) }
            val result = sessionRepository.connect(config)
            if (result.isError) {
                Timber.e(result.exceptionOrNull(), "Manual reconnect failed")
                _state.update {
                    it.copy(
                        isReconnecting = false,
                        reconnectFailed = true,
                        error = result.exceptionOrNull(),
                    )
                }
            }
            // On success the connectionState flow drives the UI back to Connected.
        }
    }

    fun onDismissError() {
        _state.update { it.copy(error = null) }
    }

    private fun autoSelectPane(tab: ZellijTab) {
        val result = navigatePaneUseCase(tab, requestedPaneId = null)
        result.getOrNull()?.let { paneId ->
            _state.update { it.copy(activePaneId = paneId) }
        }
    }

    private fun observeConnectionState() {
        transport.connectionState
            .onEach { connectionState ->
                _state.update { current ->
                    current.copy(
                        connectionState = connectionState,
                        // WebSocketZellijTransport is reconnecting when it transitions to
                        // Connecting from a previously-connected state.
                        isReconnecting = connectionState == ConnectionState.Connecting &&
                            current.connectionState != ConnectionState.Disconnected,
                        // Error after exhausted retries — show manual reconnect button.
                        reconnectFailed = if (connectionState == ConnectionState.Connected) {
                            false
                        } else {
                            connectionState == ConnectionState.Error || current.reconnectFailed
                        },
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observePaneOutput(paneId: PaneId) {
        transport.paneOutputFlow(paneId)
            .onEach { output ->
                _state.update { current ->
                    val existing = current.paneOutput[paneId.value] ?: emptyList()
                    val updated = (existing + output).takeLast(MAX_PANE_LINES)
                    current.copy(paneOutput = current.paneOutput + (paneId.value to updated))
                }
            }
            .launchIn(viewModelScope)
    }

    companion object {
        private const val MAX_PANE_LINES = 500
    }
}
