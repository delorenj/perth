package sh.delo.perth.core.`data`.db.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow
import sh.delo.perth.core.`data`.db.entity.CommandAuditEntity

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CommandAuditDao_Impl(
  __db: RoomDatabase,
) : CommandAuditDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCommandAuditEntity: EntityInsertAdapter<CommandAuditEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfCommandAuditEntity = object : EntityInsertAdapter<CommandAuditEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `command_audit_logs` (`id`,`timestamp`,`session_id`,`pane_id`,`transcript`,`interpreted_command`,`user_approved`,`execution_result`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CommandAuditEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.timestamp)
        statement.bindText(3, entity.sessionId)
        statement.bindText(4, entity.paneId)
        statement.bindText(5, entity.transcript)
        statement.bindText(6, entity.interpretedCommand)
        val _tmp: Int = if (entity.userApproved) 1 else 0
        statement.bindLong(7, _tmp.toLong())
        val _tmpExecutionResult: String? = entity.executionResult
        if (_tmpExecutionResult == null) {
          statement.bindNull(8)
        } else {
          statement.bindText(8, _tmpExecutionResult)
        }
      }
    }
  }

  public override suspend fun insertAuditEntry(entry: CommandAuditEntity): Long =
      performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfCommandAuditEntity.insertAndReturnId(_connection, entry)
    _result
  }

  public override fun observeRecentAuditLog(limit: Int): Flow<List<CommandAuditEntity>> {
    val _sql: String = "SELECT * FROM command_audit_logs ORDER BY timestamp DESC LIMIT ?"
    return createFlow(__db, false, arrayOf("command_audit_logs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "session_id")
        val _columnIndexOfPaneId: Int = getColumnIndexOrThrow(_stmt, "pane_id")
        val _columnIndexOfTranscript: Int = getColumnIndexOrThrow(_stmt, "transcript")
        val _columnIndexOfInterpretedCommand: Int = getColumnIndexOrThrow(_stmt,
            "interpreted_command")
        val _columnIndexOfUserApproved: Int = getColumnIndexOrThrow(_stmt, "user_approved")
        val _columnIndexOfExecutionResult: Int = getColumnIndexOrThrow(_stmt, "execution_result")
        val _result: MutableList<CommandAuditEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CommandAuditEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpPaneId: String
          _tmpPaneId = _stmt.getText(_columnIndexOfPaneId)
          val _tmpTranscript: String
          _tmpTranscript = _stmt.getText(_columnIndexOfTranscript)
          val _tmpInterpretedCommand: String
          _tmpInterpretedCommand = _stmt.getText(_columnIndexOfInterpretedCommand)
          val _tmpUserApproved: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfUserApproved).toInt()
          _tmpUserApproved = _tmp != 0
          val _tmpExecutionResult: String?
          if (_stmt.isNull(_columnIndexOfExecutionResult)) {
            _tmpExecutionResult = null
          } else {
            _tmpExecutionResult = _stmt.getText(_columnIndexOfExecutionResult)
          }
          _item =
              CommandAuditEntity(_tmpId,_tmpTimestamp,_tmpSessionId,_tmpPaneId,_tmpTranscript,_tmpInterpretedCommand,_tmpUserApproved,_tmpExecutionResult)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeAuditLogForSession(sessionId: String, limit: Int):
      Flow<List<CommandAuditEntity>> {
    val _sql: String =
        "SELECT * FROM command_audit_logs WHERE session_id = ? ORDER BY timestamp DESC LIMIT ?"
    return createFlow(__db, false, arrayOf("command_audit_logs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "session_id")
        val _columnIndexOfPaneId: Int = getColumnIndexOrThrow(_stmt, "pane_id")
        val _columnIndexOfTranscript: Int = getColumnIndexOrThrow(_stmt, "transcript")
        val _columnIndexOfInterpretedCommand: Int = getColumnIndexOrThrow(_stmt,
            "interpreted_command")
        val _columnIndexOfUserApproved: Int = getColumnIndexOrThrow(_stmt, "user_approved")
        val _columnIndexOfExecutionResult: Int = getColumnIndexOrThrow(_stmt, "execution_result")
        val _result: MutableList<CommandAuditEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CommandAuditEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpPaneId: String
          _tmpPaneId = _stmt.getText(_columnIndexOfPaneId)
          val _tmpTranscript: String
          _tmpTranscript = _stmt.getText(_columnIndexOfTranscript)
          val _tmpInterpretedCommand: String
          _tmpInterpretedCommand = _stmt.getText(_columnIndexOfInterpretedCommand)
          val _tmpUserApproved: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfUserApproved).toInt()
          _tmpUserApproved = _tmp != 0
          val _tmpExecutionResult: String?
          if (_stmt.isNull(_columnIndexOfExecutionResult)) {
            _tmpExecutionResult = null
          } else {
            _tmpExecutionResult = _stmt.getText(_columnIndexOfExecutionResult)
          }
          _item =
              CommandAuditEntity(_tmpId,_tmpTimestamp,_tmpSessionId,_tmpPaneId,_tmpTranscript,_tmpInterpretedCommand,_tmpUserApproved,_tmpExecutionResult)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun count(): Int {
    val _sql: String = "SELECT COUNT(*) FROM command_audit_logs"
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

  public override suspend fun purgeOlderThan(olderThanMillis: Long) {
    val _sql: String = "DELETE FROM command_audit_logs WHERE timestamp < ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, olderThanMillis)
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
