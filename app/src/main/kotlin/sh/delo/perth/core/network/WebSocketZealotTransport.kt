package sh.delo.perth.core.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import sh.delo.perth.core.domain.model.ConnectionState
import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.domain.model.PaneOutput
import sh.delo.perth.core.domain.model.ServerConfig
import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketZealotTransport @Inject constructor(
    private val okHttpClient: OkHttpClient,
) : ZealotTransport {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableStateFlow(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _sessionFlow = MutableSharedFlow<List<ZellijSession>>(replay = 1)
    private val _paneOutputFlow = MutableSharedFlow<PaneOutput>(extraBufferCapacity = 256)

    // Last known session list used to cache state during disconnection (Story 1.4)
    private var lastKnownSessions: List<ZellijSession> = emptyList()

    private var webSocket: WebSocket? = null
    private var lastConfig: ServerConfig? = null
    private val reconnectAttempts = AtomicInteger(0)
    private var reconnectJob: Job? = null

    override suspend fun connect(config: ServerConfig): AppResult<Unit> {
        lastConfig = config
        reconnectAttempts.set(0)
        return doConnect(config)
    }

    private fun doConnect(config: ServerConfig): AppResult<Unit> {
        return try {
            _connectionState.value = ConnectionState.Connecting
            Timber.d("Connecting to zealot: url=%s", config.wsUrl)
            val request = Request.Builder()
                .url("${config.wsUrl}/ws")
                .apply { config.authToken?.let { addHeader("Authorization", "Bearer $it") } }
                .build()
            webSocket = okHttpClient.newWebSocket(request, PerthWebSocketListener())
            AppResult.Success(Unit)
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error
            Timber.e(e, "Failed to connect to zealot server")
            AppResult.Error(AppException.Network("Connection failed: ${e.message}", e))
        }
    }

    override suspend fun disconnect() {
        Timber.d("Disconnecting from zealot server")
        reconnectJob?.cancel()
        reconnectJob = null
        reconnectAttempts.set(0)
        webSocket?.close(NORMAL_CLOSURE_CODE, "User disconnected")
        webSocket = null
        _connectionState.value = ConnectionState.Disconnected
    }

    override fun sessionFlow(): Flow<List<ZellijSession>> = _sessionFlow

    override fun paneOutputFlow(paneId: PaneId): Flow<PaneOutput> =
        _paneOutputFlow.filter { it.paneId == paneId }

    override suspend fun sendInput(paneId: PaneId, input: String): AppResult<Unit> {
        val ws = webSocket ?: return AppResult.Error(
            AppException.Network("Not connected to zealot server")
        )
        return try {
            val message = """{"type":"pane.input","pane_id":"${paneId.value}","input":"$input"}"""
            ws.send(message)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send input: paneId=%s", paneId)
            AppResult.Error(AppException.Network("Send failed: ${e.message}", e))
        }
    }

    override suspend fun sendCommand(paneId: PaneId, command: String): AppResult<String> {
        val ws = webSocket ?: return AppResult.Error(
            AppException.Network("Not connected to zealot server")
        )
        return try {
            val message = """{"type":"pane.command","pane_id":"${paneId.value}","command":"$command"}"""
            ws.send(message)
            AppResult.Success("command_sent")
        } catch (e: Exception) {
            Timber.e(e, "Failed to send command: paneId=%s command=%s", paneId, command)
            AppResult.Error(AppException.Network("Send failed: ${e.message}", e))
        }
    }

    /**
     * Schedules an automatic reconnect attempt with exponential backoff.
     * Attempts: 1s delay, 2s delay, 4s delay — max [MAX_RECONNECT_ATTEMPTS] total.
     * After exhausting attempts the connection state is set to [ConnectionState.Error]
     * and no further automatic retries occur; the user must reconnect manually.
     */
    private fun scheduleReconnect() {
        val config = lastConfig ?: return
        val attempt = reconnectAttempts.incrementAndGet()
        if (attempt > MAX_RECONNECT_ATTEMPTS) {
            Timber.w("Reconnect attempts exhausted (%d)", MAX_RECONNECT_ATTEMPTS)
            _connectionState.value = ConnectionState.Error
            return
        }

        val delayMs = RECONNECT_BASE_DELAY_MS * (1 shl (attempt - 1)) // 1s, 2s, 4s
        Timber.d("Scheduling reconnect attempt %d in %dms", attempt, delayMs)

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(delayMs)
            Timber.d("Reconnecting (attempt %d)...", attempt)
            doConnect(config)
        }
    }

    private inner class PerthWebSocketListener : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Timber.d("WebSocket opened")
            reconnectAttempts.set(0)
            reconnectJob?.cancel()
            reconnectJob = null
            _connectionState.value = ConnectionState.Connected
            // Re-emit cached sessions so observers immediately have data after reconnect
            if (lastKnownSessions.isNotEmpty()) {
                scope.launch { _sessionFlow.emit(lastKnownSessions) }
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // TODO: Parse zealot protocol messages and emit to appropriate flows
            Timber.d("WebSocket message received: length=%d", text.length)
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.e(t, "WebSocket failure: code=%s", response?.code)
            _connectionState.value = ConnectionState.Disconnected
            scheduleReconnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Timber.d("WebSocket closed: code=%d reason=%s", code, reason)
            if (code != NORMAL_CLOSURE_CODE) {
                _connectionState.value = ConnectionState.Disconnected
                scheduleReconnect()
            } else {
                _connectionState.value = ConnectionState.Disconnected
            }
        }
    }

    companion object {
        private const val NORMAL_CLOSURE_CODE = 1000
        private const val MAX_RECONNECT_ATTEMPTS = 3
        private const val RECONNECT_BASE_DELAY_MS = 1000L
    }
}

/** Extension to create a configured OkHttpClient for zealot WebSocket connections. */
fun zealotOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(0, TimeUnit.MILLISECONDS) // No read timeout for WebSocket
    .writeTimeout(30, TimeUnit.SECONDS)
    .pingInterval(30, TimeUnit.SECONDS)
    .build()
