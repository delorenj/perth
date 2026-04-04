package sh.delo.perth.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import sh.delo.perth.core.domain.repository.VoiceRepository
import sh.delo.perth.core.network.SpeechRecognizer
import sh.delo.perth.feature.voice.data.AndroidSpeechRecognizer
import sh.delo.perth.feature.voice.data.VoiceRepositoryImpl
import sh.delo.perth.feature.voice.data.WhisperSpeechRecognizer
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class VoiceModule {

    @Binds
    @Singleton
    @Named("primary")
    abstract fun bindPrimaryRecognizer(impl: AndroidSpeechRecognizer): SpeechRecognizer

    @Binds
    @Singleton
    @Named("fallback")
    abstract fun bindFallbackRecognizer(impl: WhisperSpeechRecognizer): SpeechRecognizer

    @Binds
    @Singleton
    abstract fun bindVoiceRepository(impl: VoiceRepositoryImpl): VoiceRepository
}
