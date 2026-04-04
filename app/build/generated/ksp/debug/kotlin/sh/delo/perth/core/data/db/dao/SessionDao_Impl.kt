package sh.delo.perth.core.`data`.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow
import sh.delo.perth.core.`data`.db.entity.SessionEntity

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SessionDao_Impl(
  __db: RoomDatabase,
) : SessionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSessionEntity: EntityInsertAdapter<SessionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSessionEntity = object : EntityInsertAdapter<SessionEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `sessions` (`session_id`,`name`,`server_url`,`tabs_json`,`created_at`,`last_seen_at`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SessionEntity) {
        statement.bindText(1, entity.sessionId)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.serverUrl)
        statement.bindText(4, entity.tabsJson)
        statement.bindLong(5, entity.createdAt)
        statement.bindLong(6, entity.lastSeenAt)
      }
    }
  }

  public override suspend fun upsertSession(session: SessionEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfSessionEntity.insert(_connection, session)
  }

  public override suspend fun upsertSessions(sessions: List<SessionEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSessionEntity.insert(_connection, sessions)
  }

  public override fun observeAllSessions(): Flow<List<SessionEntity>> {
    val _sql: String = "SELECT * FROM sessions ORDER BY last_seen_at DESC"
    return createFlow(__db, false, arrayOf("sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "session_id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfServerUrl: Int = getColumnIndexOrThrow(_stmt, "server_url")
        val _columnIndexOfTabsJson: Int = getColumnIndexOrThrow(_stmt, "tabs_json")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "created_at")
        val _columnIndexOfLastSeenAt: Int = getColumnIndexOrThrow(_stmt, "last_seen_at")
        val _result: MutableList<SessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SessionEntity
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpServerUrl: String
          _tmpServerUrl = _stmt.getText(_columnIndexOfServerUrl)
          val _tmpTabsJson: String
          _tmpTabsJson = _stmt.getText(_columnIndexOfTabsJson)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastSeenAt: Long
          _tmpLastSeenAt = _stmt.getLong(_columnIndexOfLastSeenAt)
          _item =
              SessionEntity(_tmpSessionId,_tmpName,_tmpServerUrl,_tmpTabsJson,_tmpCreatedAt,_tmpLastSeenAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSessionById(sessionId: String): SessionEntity? {
    val _sql: String = "SELECT * FROM sessions WHERE session_id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "session_id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfServerUrl: Int = getColumnIndexOrThrow(_stmt, "server_url")
        val _columnIndexOfTabsJson: Int = getColumnIndexOrThrow(_stmt, "tabs_json")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "created_at")
        val _columnIndexOfLastSeenAt: Int = getColumnIndexOrThrow(_stmt, "last_seen_at")
        val _result: SessionEntity?
        if (_stmt.step()) {
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpServerUrl: String
          _tmpServerUrl = _stmt.getText(_columnIndexOfServerUrl)
          val _tmpTabsJson: String
          _tmpTabsJson = _stmt.getText(_columnIndexOfTabsJson)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastSeenAt: Long
          _tmpLastSeenAt = _stmt.getLong(_columnIndexOfLastSeenAt)
          _result =
              SessionEntity(_tmpSessionId,_tmpName,_tmpServerUrl,_tmpTabsJson,_tmpCreatedAt,_tmpLastSeenAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun count(): Int {
    val _sql: String = "SELECT COUNT(*) FROM sessions"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateLastSeen(sessionId: String, lastSeenAt: Long) {
    val _sql: String = "UPDATE sessions SET last_seen_at = ? WHERE session_id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, lastSeenAt)
        _argIndex = 2
        _stmt.bindText(_argIndex, sessionId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteSession(sessionId: String) {
    val _sql: String = "DELETE FROM sessions WHERE session_id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteStaleSessionsBefore(olderThan: Long) {
    val _sql: String = "DELETE FROM sessions WHERE last_seen_at < ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, olderThan)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
