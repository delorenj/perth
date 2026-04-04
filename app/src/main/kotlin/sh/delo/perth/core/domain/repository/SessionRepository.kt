package sh.delo.perth.core.domain.repository

import kotlinx.coroutines.flow.Flow
import sh.delo.perth.core.domain.model.ServerConfig
import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.result.AppResult

/** Repository for managing Zellij session data and server connectivity. */
interface SessionRepository {

    /** Emits the current list of sessions whenever it changes. */
    fun sessionListFlow(): Flow<List<ZellijSession>>

    /** Emits the list of recently-visited sessions stored in Room, newest first. */
    fun recentSessionsFlow(): Flow<List<ZellijSession>>

    /** Returns the cached session with the given [sessionId], or null. */
    suspend fun getSession(sessionId: String): AppResult<ZellijSession?>

    /** Connects to the server using the provided [config]. */
    suspend fun connect(config: ServerConfig): AppResult<Unit>

    /** Disconnects from the current server. */
    suspend fun disconnect(): AppResult<Unit>

    /** Refreshes the session list from the server. */
    suspend fun refreshSessions(): AppResult<List<ZellijSession>>

    /**
     * Records [session] as recently visited so it appears in the recent sessions list.
     * Upserts via Room using the session id as the primary key.
     */
    suspend fun markSessionVisited(session: ZellijSession, serverUrl: String): AppResult<Unit>

    /** Removes all recent session records from Room. */
    suspend fun clearRecentSessions(): AppResult<Unit>
}
