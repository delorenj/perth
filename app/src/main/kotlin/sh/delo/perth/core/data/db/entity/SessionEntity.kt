package sh.delo.perth.core.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    indices = [Index(value = ["server_url"])]
)
data class SessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "session_id")
    val sessionId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "server_url")
    val serverUrl: String,

    @ColumnInfo(name = "tabs_json")
    val tabsJson: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long, // epoch millis

    @ColumnInfo(name = "last_seen_at")
    val lastSeenAt: Long, // epoch millis
)
