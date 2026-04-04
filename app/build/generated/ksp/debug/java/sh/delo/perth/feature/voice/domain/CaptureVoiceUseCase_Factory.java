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
public final class CaptureVoiceUseCase_Factory implements Factory<CaptureVoiceUseCase> {
  private final Provider<VoiceRepository> voiceRepositoryProvider;

  public CaptureVoiceUseCase_Factory(Provider<VoiceRepository> voiceRepositoryProvider) {
    this.voiceRepositoryProvider = voiceRepositoryProvider;
  }

  @Override
  public CaptureVoiceUseCase get() {
    return newInstance(voiceRepositoryProvider.get());
  }

  public static CaptureVoiceUseCase_Factory create(
      javax.inject.Provider<VoiceRepository> voiceRepositoryProvider) {
    return new CaptureVoiceUseCase_Factory(Providers.asDaggerProvider(voiceRepositoryProvider));
  }

  public static CaptureVoiceUseCase_Factory create(
      Provider<VoiceRepository> voiceRepositoryProvider) {
    return new CaptureVoiceUseCase_Factory(voiceRepositoryProvider);
  }

  public static CaptureVoiceUseCase newInstance(VoiceRepository voiceRepository) {
    return new CaptureVoiceUseCase(voiceRepository);
  }
}
