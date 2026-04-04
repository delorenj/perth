package sh.delo.perth.feature.voice.domain;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import sh.delo.perth.core.domain.repository.VoiceRepository;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class TranscribeSpeechUseCase_Factory implements Factory<TranscribeSpeechUseCase> {
  private final Provider<VoiceRepository> voiceRepositoryProvider;

  public TranscribeSpeechUseCase_Factory(Provider<VoiceRepository> voiceRepositoryProvider) {
    this.voiceRepositoryProvider = voiceRepositoryProvider;
  }

  @Override
  public TranscribeSpeechUseCase get() {
    return newInstance(voiceRepositoryProvider.get());
  }

  public static TranscribeSpeechUseCase_Factory create(
      javax.inject.Provider<VoiceRepository> voiceRepositoryProvider) {
    return new TranscribeSpeechUseCase_Factory(Providers.asDaggerProvider(voiceRepositoryProvider));
  }

  public static TranscribeSpeechUseCase_Factory create(
      Provider<VoiceRepository> voiceRepositoryProvider) {
    return new TranscribeSpeechUseCase_Factory(voiceRepositoryProvider);
  }

  public static TranscribeSpeechUseCase newInstance(VoiceRepository voiceRepository) {
    return new TranscribeSpeechUseCase(voiceRepository);
  }
}
