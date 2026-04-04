package sh.delo.perth.core.domain.repository

import kotlinx.coroutines.flow.Flow
import sh.delo.perth.core.result.AppResult
import java.time.Instant

/** Repository for persisting the command execution audit log. */
interface CommandAuditRepository {

    /** Streams the most recent [limit] audit entries, newest first. */
    fun auditLogFlow(limit: Int = 50): Flow<List<AuditEntry>>

    /** Records a new audit entry. */
    suspend fun record(entry: AuditEntry): AppResult<Unit>

    /** Clears all audit log entries older than [before]. */
    suspend fun purgeOlderThan(before: Instant): AppResult<Unit>

    data class AuditEntry(
        val id: Long = 0,
        val timestamp: Instant,
        val sessionId: String,
        val paneId: String,
        val transcript: String,
        val interpretedCommand: String,
        val userApproved: Boolean,
        val executionResult: String?,
    )
}
