package sh.delo.perth.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import sh.delo.perth.core.data.db.entity.SessionEntity

@Dao
interface SessionDao {

    @Query("SELECT * FROM sessions ORDER BY last_seen_at DESC")
    fun observeAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE session_id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSession(session: SessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSessions(sessions: List<SessionEntity>)

    @Query("UPDATE sessions SET last_seen_at = :lastSeenAt WHERE session_id = :sessionId")
    suspend fun updateLastSeen(sessionId: String, lastSeenAt: Long)

    @Query("DELETE FROM sessions WHERE session_id = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("DELETE FROM sessions WHERE last_seen_at < :olderThan")
    suspend fun deleteStaleSessionsBefore(olderThan: Long)

    @Query("SELECT COUNT(*) FROM sessions")
    suspend fun count(): Int
}
