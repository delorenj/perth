package sh.delo.perth.core.network

import android.util.Base64
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import sh.delo.perth.core.domain.model.ConnectionState
import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.domain.model.PaneOutput
import sh.delo.perth.core.domain.model.ServerConfig
import sh.delo.perth.core.domain.model.ZellijPane
import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.domain.model.ZellijTab
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import timber.log.Timber
import java.time.Instant
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketZellijTransport @Inject constructor(
    private val okHttpClient: OkHttpClient,
) : ZellijTransport {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    private val _connectionState = MutableStateFlow(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _sessionFlow = MutableSharedFlow<List<ZellijSession>>(replay = 1)
    private val _paneOutputFlow = MutableSharedFlow<PaneOutput>(extraBufferCapacity = 256)

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
            Timber.d("Connecting to zellij: url=%s", config.wsUrl)
            val request = Request.Builder()
                .url("${config.wsUrl}/ws")
                .apply { config.authToken?.let { addHeader("Authorization", "Bearer $it") } }
                .build()
            webSocket = okHttpClient.newWebSocket(request, PerthWebSocketListener())
            AppResult.Success(Unit)
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Error
            Timber.e(e, "Failed to connect to zellij server")
            AppResult.Error(AppException.Network("Connection failed: ${e.message}", e))
        }
    }

    override suspend fun disconnect() {
        Timber.d("Disconnecting from zellij server")
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
            AppException.Network("Not connected to zellij server")
        )
        return try {
            val message = PerthMessage.PaneInput(pane_id = paneId.value, input = input)
            ws.send(json.encodeToString(message))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send input: paneId=%s", paneId)
            AppResult.Error(AppException.Network("Send failed: ${e.message}", e))
        }
    }

    override suspend fun sendCommand(paneId: PaneId, command: String): AppResult<String> {
        val ws = webSocket ?: return AppResult.Error(
            AppException.Network("Not connected to zellij server")
        )
        return try {
            val message = PerthMessage.PaneCommand(pane_id = paneId.value, command = command)
            ws.send(json.encodeToString(message))
            AppResult.Success("command_sent")
        } catch (e: Exception) {
            Timber.e(e, "Failed to send command: paneId=%s command=%s", paneId, command)
            AppResult.Error(AppException.Network("Send failed: ${e.message}", e))
        }
    }

    private fun scheduleReconnect() {
        val config = lastConfig ?: return
        val attempt = reconnectAttempts.incrementAndGet()
        if (attempt > MAX_RECONNECT_ATTEMPTS) {
            Timber.w("Reconnect attempts exhausted (%d)", MAX_RECONNECT_ATTEMPTS)
            _connectionState.value = ConnectionState.Error
            return
        }

        val delayMs = RECONNECT_BASE_DELAY_MS * (1 shl (attempt - 1))
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            delay(delayMs)
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

            // Request session list on connect
            webSocket.send(json.encodeToString<PerthMessage>(PerthMessage.SessionList))
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                val message = json.decodeFromString<PerthMessage>(text)
                handlePerthMessage(message)
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse message: %s", text)
            }
        }

        private fun handlePerthMessage(message: PerthMessage) {
            when (message) {
                is PerthMessage.SessionListUpdate -> {
                    val sessions = message.sessions.map { info ->
                        ZellijSession(
                            id = info.name,
                            name = info.name,
                            tabs = emptyList(), // Tabs arrive via SessionAttached.
                            createdAt = Instant.now()
                        )
                    }
                    lastKnownSessions = sessions
                    scope.launch { _sessionFlow.emit(sessions) }
                }
                is PerthMessage.SessionAttached -> {
                    // Merge the tab/pane tree from the bridge plugin into the
                    // cached session entry. If the session wasn't in the list
                    // yet (e.g. attach happened before list refresh), insert
                    // a new entry — this happens with the M2 single-session
                    // workflow where the phone goes straight to the Workspace.
                    val tabs = message.tabs.map { wire ->
                        ZellijTab(
                            id = wire.position.toString(),
                            name = wire.name,
                            isActive = wire.is_active,
                            // Plugin doesn't populate per-pane details yet (M2
                            // ships viewport-only); synthesize a single active
                            // pane entry so the UI knows where to direct input.
                            panes = wire.active_pane_id?.let { paneId ->
                                listOf(
                                    ZellijPane(
                                        id = PaneId(paneId.toString()),
                                        title = wire.name,
                                        isActive = true,
                                    )
                                )
                            } ?: emptyList(),
                        )
                    }
                    val existing = lastKnownSessions.find { it.name == message.session_name }
                    val updated = (existing ?: ZellijSession(
                        id = message.session_name,
                        name = message.session_name,
                        tabs = emptyList(),
                        createdAt = Instant.now(),
                    )).copy(tabs = tabs)
                    lastKnownSessions = lastKnownSessions
                        .filterNot { it.name == message.session_name } + updated
                    scope.launch { _sessionFlow.emit(lastKnownSessions) }
                }
                is PerthMessage.PaneOutputMessage -> {
                    // The bridge plugin posts viewport text directly (not
                    // base64-encoded) since zellij 0.44 PaneContents already
                    // returns Strings. Detect and route accordingly: if the
                    // payload decodes cleanly as base64 we treat it as such,
                    // otherwise pass through. This preserves compatibility
                    // with a future binary-stream path without breaking M2.
                    val text = try {
                        val decoded = Base64.decode(message.data, Base64.NO_WRAP or Base64.NO_PADDING)
                        String(decoded, Charsets.UTF_8)
                    } catch (_: IllegalArgumentException) {
                        message.data
                    }
                    val output = PaneOutput(
                        paneId = PaneId(message.pane_id),
                        text = text,
                    )
                    scope.launch { _paneOutputFlow.emit(output) }
                }
                is PerthMessage.Error -> {
                    Timber.e("Server error: %s", message.message)
                }
                else -> { /* Ignore other types */ }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.e(t, "WebSocket failure")
            _connectionState.value = ConnectionState.Disconnected
            scheduleReconnect()
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            _connectionState.value = ConnectionState.Disconnected
            if (code != NORMAL_CLOSURE_CODE) scheduleReconnect()
        }
    }

    companion object {
        private const val NORMAL_CLOSURE_CODE = 1000
        private const val MAX_RECONNECT_ATTEMPTS = 3
        private const val RECONNECT_BASE_DELAY_MS = 1000L
    }
}

fun zellijOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(0, TimeUnit.MILLISECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .pingInterval(30, TimeUnit.SECONDS)
    .build()
