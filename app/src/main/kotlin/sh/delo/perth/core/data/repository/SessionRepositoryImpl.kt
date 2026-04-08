package sh.delo.perth.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import sh.delo.perth.core.data.db.dao.SessionDao
import sh.delo.perth.core.data.db.entity.SessionEntity
import sh.delo.perth.core.domain.model.ServerConfig
import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.domain.model.ZellijTab
import sh.delo.perth.core.domain.repository.SessionRepository
import sh.delo.perth.core.network.ZellijTransport
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import sh.delo.perth.core.result.runCatchingAppResult
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val transport: ZellijTransport,
    private val sessionDao: SessionDao,
) : SessionRepository {

    override fun sessionListFlow(): Flow<List<ZellijSession>> = transport.sessionFlow()

    override fun recentSessionsFlow(): Flow<List<ZellijSession>> =
        sessionDao.observeAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getSession(sessionId: String): AppResult<ZellijSession?> =
        runCatchingAppResult(errorMapper = { AppException.Network(it.message ?: "Read failed", it) }) {
            transport.sessionFlow().first().firstOrNull { it.id == sessionId }
        }

    override suspend fun connect(config: ServerConfig): AppResult<Unit> =
        transport.connect(config)

    override suspend fun disconnect(): AppResult<Unit> =
        runCatchingAppResult {
            transport.disconnect()
        }

    override suspend fun refreshSessions(): AppResult<List<ZellijSession>> =
        runCatchingAppResult(errorMapper = { AppException.Network(it.message ?: "Refresh failed", it) }) {
            transport.sessionFlow().first()
        }

    override suspend fun markSessionVisited(session: ZellijSession, serverUrl: String): AppResult<Unit> =
        runCatchingAppResult(errorMapper = { AppException.Storage(it.message ?: "Upsert failed", it) }) {
            val now = System.currentTimeMillis()
            sessionDao.upsertSession(
                SessionEntity(
                    sessionId = session.id,
                    name = session.name,
                    serverUrl = serverUrl,
                    tabsJson = session.tabCount.toString(), // minimal serialization; full JSON in future story
                    createdAt = session.createdAt.toEpochMilli(),
                    lastSeenAt = now,
                )
            )
        }

    override suspend fun clearRecentSessions(): AppResult<Unit> =
        runCatchingAppResult(errorMapper = { AppException.Storage(it.message ?: "Clear failed", it) }) {
            sessionDao.deleteStaleSessionsBefore(Long.MAX_VALUE)
        }

    // ---------------------------------------------------------------------------
    // Mapping helpers
    // ---------------------------------------------------------------------------

    private fun SessionEntity.toDomain(): ZellijSession = ZellijSession(
        id = sessionId,
        name = name,
        tabs = emptyList<ZellijTab>(), // Tab details not stored locally; refreshed from server
        createdAt = Instant.ofEpochMilli(createdAt),
    )
}
