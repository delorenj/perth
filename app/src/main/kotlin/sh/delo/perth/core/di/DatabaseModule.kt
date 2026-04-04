package sh.delo.perth.core.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import sh.delo.perth.core.data.db.PerthDatabase
import sh.delo.perth.core.data.db.dao.CommandAuditDao
import sh.delo.perth.core.data.db.dao.SessionDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun providePerthDatabase(
        @ApplicationContext context: Context,
    ): PerthDatabase = Room.databaseBuilder(
        context,
        PerthDatabase::class.java,
        PerthDatabase.DATABASE_NAME,
    )
        .fallbackToDestructiveMigrationOnDowngrade()
        .build()

    @Provides
    @Singleton
    fun provideSessionDao(db: PerthDatabase): SessionDao = db.sessionDao()

    @Provides
    @Singleton
    fun provideCommandAuditDao(db: PerthDatabase): CommandAuditDao = db.commandAuditDao()
}
