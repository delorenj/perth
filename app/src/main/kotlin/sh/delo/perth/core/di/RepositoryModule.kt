package sh.delo.perth.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sh.delo.perth.core.data.repository.CommandAuditRepositoryImpl
import sh.delo.perth.core.data.repository.SessionRepositoryImpl
import sh.delo.perth.core.data.repository.SettingsRepositoryImpl
import sh.delo.perth.core.domain.repository.CommandAuditRepository
import sh.delo.perth.core.domain.repository.SessionRepository
import sh.delo.perth.core.domain.repository.SettingsRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindCommandAuditRepository(impl: CommandAuditRepositoryImpl): CommandAuditRepository
}
