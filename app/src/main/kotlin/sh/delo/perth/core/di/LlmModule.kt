package sh.delo.perth.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sh.delo.perth.core.domain.repository.LlmRepository
import sh.delo.perth.feature.command.data.LlmRepositoryImpl
import javax.inject.Singleton

/** Wires the LLM repository implementation (Story 5.1). */
@Module
@InstallIn(SingletonComponent::class)
abstract class LlmModule {

    @Binds
    @Singleton
    abstract fun bindLlmRepository(impl: LlmRepositoryImpl): LlmRepository
}
