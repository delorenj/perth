package sh.delo.perth.core.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import sh.delo.perth.core.data.db.dao.CommandAuditDao
import sh.delo.perth.core.data.db.dao.SessionDao
import sh.delo.perth.core.data.db.entity.CommandAuditEntity
import sh.delo.perth.core.data.db.entity.SessionEntity

@Database(
    entities = [
        SessionEntity::class,
        CommandAuditEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class PerthDatabase : RoomDatabase() {

    abstract fun sessionDao(): SessionDao

    abstract fun commandAuditDao(): CommandAuditDao

    companion object {
        const val DATABASE_NAME = "perth.db"
    }
}
