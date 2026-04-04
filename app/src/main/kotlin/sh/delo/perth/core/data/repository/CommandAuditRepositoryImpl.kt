package sh.delo.perth.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import sh.delo.perth.core.data.db.dao.CommandAuditDao
import sh.delo.perth.core.data.db.entity.CommandAuditEntity
import sh.delo.perth.core.domain.repository.CommandAuditRepository
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import sh.delo.perth.core.result.runCatchingAppResult
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommandAuditRepositoryImpl @Inject constructor(
    private val commandAuditDao: CommandAuditDao,
) : CommandAuditRepository {

    override fun auditLogFlow(limit: Int): Flow<List<CommandAuditRepository.AuditEntry>> =
        commandAuditDao.observeRecentAuditLog(limit).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun record(entry: CommandAuditRepository.AuditEntry): AppResult<Unit> =
        runCatchingAppResult(errorMapper = { AppException.Storage(it.message ?: "Insert failed", it) }) {
            commandAuditDao.insertAuditEntry(entry.toEntity())
            Unit
        }

    override suspend fun purgeOlderThan(before: Instant): AppResult<Unit> =
        runCatchingAppResult(errorMapper = { AppException.Storage(it.message ?: "Purge failed", it) }) {
            commandAuditDao.purgeOlderThan(before.toEpochMilli())
        }

    // ---------------------------------------------------------------------------
    // Mapping
    // ---------------------------------------------------------------------------

    private fun CommandAuditEntity.toDomain() = CommandAuditRepository.AuditEntry(
        id = id,
        timestamp = Instant.ofEpochMilli(timestamp),
        sessionId = sessionId,
        paneId = paneId,
        transcript = transcript,
        interpretedCommand = interpretedCommand,
        userApproved = userApproved,
        executionResult = executionResult,
    )

    private fun CommandAuditRepository.AuditEntry.toEntity() = CommandAuditEntity(
        id = id,
        timestamp = timestamp.toEpochMilli(),
        sessionId = sessionId,
        paneId = paneId,
        transcript = transcript,
        interpretedCommand = interpretedCommand,
        userApproved = userApproved,
        executionResult = executionResult,
    )
}
