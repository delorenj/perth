package sh.delo.perth.core.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import sh.delo.perth.core.domain.model.ConnectionState
import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.domain.model.PaneOutput
import sh.delo.perth.core.domain.model.ServerConfig
import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.result.AppResult

/** Abstracts the transport layer between Perth and the zealot server. */
interface ZealotTransport {

    /** The current connection state as a hot stream. */
    val connectionState: StateFlow<ConnectionState>

    /** Connects to the server specified by [config]. */
    suspend fun connect(config: ServerConfig): AppResult<Unit>

    /** Closes the current connection gracefully. */
    suspend fun disconnect()

    /** Emits the full session list whenever it changes. */
    fun sessionFlow(): Flow<List<ZellijSession>>

    /** Emits terminal output lines for the pane identified by [paneId]. */
    fun paneOutputFlow(paneId: PaneId): Flow<PaneOutput>

    /**
     * Sends raw keyboard [input] to the pane identified by [paneId].
     * Returns [AppResult.Success] when the server acknowledges delivery.
     */
    suspend fun sendInput(paneId: PaneId, input: String): AppResult<Unit>

    /**
     * Sends a structured [command] to the pane identified by [paneId].
     * Returns [AppResult.Success] with the command result string.
     */
    suspend fun sendCommand(paneId: PaneId, command: String): AppResult<String>
}
