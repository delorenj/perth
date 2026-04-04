package sh.delo.perth.core.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "command_audit_logs",
    indices = [
        Index(value = ["session_id"]),
        Index(value = ["timestamp"]),
    ],
)
data class CommandAuditEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long, // epoch millis

    @ColumnInfo(name = "session_id")
    val sessionId: String,

    @ColumnInfo(name = "pane_id")
    val paneId: String,

    @ColumnInfo(name = "transcript")
    val transcript: String,

    @ColumnInfo(name = "interpreted_command")
    val interpretedCommand: String,

    @ColumnInfo(name = "user_approved")
    val userApproved: Boolean,

    @ColumnInfo(name = "execution_result")
    val executionResult: String?,
)
