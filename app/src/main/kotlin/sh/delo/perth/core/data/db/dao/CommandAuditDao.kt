package sh.delo.perth.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import sh.delo.perth.core.data.db.entity.CommandAuditEntity

@Dao
interface CommandAuditDao {

    @Query("SELECT * FROM command_audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecentAuditLog(limit: Int): Flow<List<CommandAuditEntity>>

    @Query("SELECT * FROM command_audit_logs WHERE session_id = :sessionId ORDER BY timestamp DESC LIMIT :limit")
    fun observeAuditLogForSession(sessionId: String, limit: Int): Flow<List<CommandAuditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditEntry(entry: CommandAuditEntity): Long

    @Query("DELETE FROM command_audit_logs WHERE timestamp < :olderThanMillis")
    suspend fun purgeOlderThan(olderThanMillis: Long)

    @Query("SELECT COUNT(*) FROM command_audit_logs")
    suspend fun count(): Int
}
