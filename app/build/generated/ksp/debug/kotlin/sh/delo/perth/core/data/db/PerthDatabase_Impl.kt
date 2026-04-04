package sh.delo.perth.core.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass
import sh.delo.perth.core.`data`.db.dao.CommandAuditDao
import sh.delo.perth.core.`data`.db.dao.CommandAuditDao_Impl
import sh.delo.perth.core.`data`.db.dao.SessionDao
import sh.delo.perth.core.`data`.db.dao.SessionDao_Impl

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class PerthDatabase_Impl : PerthDatabase() {
  private val _sessionDao: Lazy<SessionDao> = lazy {
    SessionDao_Impl(this)
  }

  private val _commandAuditDao: Lazy<CommandAuditDao> = lazy {
    CommandAuditDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1,
        "57b2d23cdf261d913a79b8eef6ec6694", "62f4a2537afcbd7054b2f49fdb691c15") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sessions` (`session_id` TEXT NOT NULL, `name` TEXT NOT NULL, `server_url` TEXT NOT NULL, `tabs_json` TEXT NOT NULL, `created_at` INTEGER NOT NULL, `last_seen_at` INTEGER NOT NULL, PRIMARY KEY(`session_id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sessions_server_url` ON `sessions` (`server_url`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `command_audit_logs` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `timestamp` INTEGER NOT NULL, `session_id` TEXT NOT NULL, `pane_id` TEXT NOT NULL, `transcript` TEXT NOT NULL, `interpreted_command` TEXT NOT NULL, `user_approved` INTEGER NOT NULL, `execution_result` TEXT)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_command_audit_logs_session_id` ON `command_audit_logs` (`session_id`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_command_audit_logs_timestamp` ON `command_audit_logs` (`timestamp`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '57b2d23cdf261d913a79b8eef6ec6694')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `sessions`")
        connection.execSQL("DROP TABLE IF EXISTS `command_audit_logs`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsSessions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSessions.put("session_id", TableInfo.Column("session_id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("name", TableInfo.Column("name", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("server_url", TableInfo.Column("server_url", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("tabs_json", TableInfo.Column("tabs_json", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("created_at", TableInfo.Column("created_at", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("last_seen_at", TableInfo.Column("last_seen_at", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSessions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSessions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSessions.add(TableInfo.Index("index_sessions_server_url", false,
            listOf("server_url"), listOf("ASC")))
        val _infoSessions: TableInfo = TableInfo("sessions", _columnsSessions, _foreignKeysSessions,
            _indicesSessions)
        val _existingSessions: TableInfo = read(connection, "sessions")
        if (!_infoSessions.equals(_existingSessions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |sessions(sh.delo.perth.core.data.db.entity.SessionEntity).
              | Expected:
              |""".trimMargin() + _infoSessions + """
              |
              | Found:
              |""".trimMargin() + _existingSessions)
        }
        val _columnsCommandAuditLogs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCommandAuditLogs.put("id", TableInfo.Column("id", "INTEGER", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCommandAuditLogs.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCommandAuditLogs.put("session_id", TableInfo.Column("session_id", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCommandAuditLogs.put("pane_id", TableInfo.Column("pane_id", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCommandAuditLogs.put("transcript", TableInfo.Column("transcript", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCommandAuditLogs.put("interpreted_command", TableInfo.Column("interpreted_command",
            "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCommandAuditLogs.put("user_approved", TableInfo.Column("user_approved", "INTEGER",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCommandAuditLogs.put("execution_result", TableInfo.Column("execution_result",
            "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCommandAuditLogs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCommandAuditLogs: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesCommandAuditLogs.add(TableInfo.Index("index_command_audit_logs_session_id", false,
            listOf("session_id"), listOf("ASC")))
        _indicesCommandAuditLogs.add(TableInfo.Index("index_command_audit_logs_timestamp", false,
            listOf("timestamp"), listOf("ASC")))
        val _infoCommandAuditLogs: TableInfo = TableInfo("command_audit_logs",
            _columnsCommandAuditLogs, _foreignKeysCommandAuditLogs, _indicesCommandAuditLogs)
        val _existingCommandAuditLogs: TableInfo = read(connection, "command_audit_logs")
        if (!_infoCommandAuditLogs.equals(_existingCommandAuditLogs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |command_audit_logs(sh.delo.perth.core.data.db.entity.CommandAuditEntity).
              | Expected:
              |""".trimMargin() + _infoCommandAuditLogs + """
              |
              | Found:
              |""".trimMargin() + _existingCommandAuditLogs)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "sessions",
        "command_audit_logs")
  }

  public override fun clearAllTables() {
    super.performClear(false, "sessions", "command_audit_logs")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(SessionDao::class, SessionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CommandAuditDao::class, CommandAuditDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun sessionDao(): SessionDao = _sessionDao.value

  public override fun commandAuditDao(): CommandAuditDao = _commandAuditDao.value
}
