package sh.delo.perth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import sh.delo.perth.core.domain.model.ConnectionState
import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.domain.model.PaneOutput
import sh.delo.perth.core.domain.model.ServerConfig
import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.network.ZealotTransport
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult

/**
 * Controllable fake [ZealotTransport] for unit tests.
 *
 * Exposes mutable state and recorded calls so tests can assert on transport
 * interactions without spinning up a real WebSocket connection.
 *
 * Usage:
 * ```kotlin
 * val transport = FakeZealotTransport()
 * transport.emitSessions(TestData.ALL_SESSIONS)
 * transport.emitPaneOutput(TestData.PANE_OUTPUT_LINES.first())
 * ```
 */
class FakeZealotTransport : ZealotTransport {

    // ---------------------------------------------------------------------------
    // Mutable state the test controls
    // ---------------------------------------------------------------------------

    private val _connectionState = MutableStateFlow(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _sessionFlow = MutableSharedFlow<List<ZellijSession>>(replay = 1)
    private val _paneOutputFlow = MutableSharedFlow<PaneOutput>(extraBufferCapacity = 64)

    /** Override to make [connect] return an error. */
    var connectResult: AppResult<Unit> = AppResult.Success(Unit)

    /** Override to make [sendInput] return an error. */
    var sendInputResult: AppResult<Unit> = AppResult.Success(Unit)

    /** Override to make [sendCommand] return a specific result. */
    var sendCommandResult: AppResult<String> = AppResult.Success("ok")

    // ---------------------------------------------------------------------------
    // Recorded call history
    // ---------------------------------------------------------------------------

    val connectCalls: MutableList<ServerConfig> = mutableListOf()
    var disconnectCallCount: Int = 0
    val sendInputCalls: MutableList<Pair<PaneId, String>> = mutableListOf()
    val sendCommandCalls: MutableList<Pair<PaneId, String>> = mutableListOf()

    // ---------------------------------------------------------------------------
    // ZealotTransport implementation
    // ---------------------------------------------------------------------------

    override suspend fun connect(config: ServerConfig): AppResult<Unit> {
        connectCalls += config
        if (connectResult.isSuccess) {
            _connectionState.value = ConnectionState.Connected
        } else {
            _connectionState.value = ConnectionState.Error
        }
        return connectResult
    }

    override suspend fun disconnect() {
        disconnectCallCount++
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun sessionFlow(): Flow<List<ZellijSession>> = _sessionFlow

    override fun paneOutputFlow(paneId: PaneId): Flow<PaneOutput> =
        _paneOutputFlow.filter { it.paneId == paneId }

    override suspend fun sendInput(paneId: PaneId, input: String): AppResult<Unit> {
        sendInputCalls += paneId to input
        return sendInputResult
    }

    override suspend fun sendCommand(paneId: PaneId, command: String): AppResult<String> {
        sendCommandCalls += paneId to command
        return sendCommandResult
    }

    // ---------------------------------------------------------------------------
    // Test helpers
    // ---------------------------------------------------------------------------

    /** Emits a new session list to all active collectors of [sessionFlow]. */
    suspend fun emitSessions(sessions: List<ZellijSession>) {
        _sessionFlow.emit(sessions)
    }

    /** Emits a single [PaneOutput] to all active collectors of [paneOutputFlow]. */
    suspend fun emitPaneOutput(output: PaneOutput) {
        _paneOutputFlow.emit(output)
    }

    /** Sets the connection state directly (bypassing [connect] logic). */
    fun setConnectionState(state: ConnectionState) {
        _connectionState.value = state
    }

    /** Configures [connect] to return a [AppResult.Error] with a [AppException.Network]. */
    fun failNextConnect(message: String = "Connection refused") {
        connectResult = AppResult.Error(AppException.Network(message))
    }

    /** Resets all recorded calls and overrides. */
    fun reset() {
        connectResult = AppResult.Success(Unit)
        sendInputResult = AppResult.Success(Unit)
        sendCommandResult = AppResult.Success("ok")
        connectCalls.clear()
        disconnectCallCount = 0
        sendInputCalls.clear()
        sendCommandCalls.clear()
        _connectionState.value = ConnectionState.Disconnected
    }
}
