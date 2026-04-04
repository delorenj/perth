package sh.delo.perth.feature.voice.ui;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import sh.delo.perth.core.domain.repository.SettingsRepository;
import sh.delo.perth.core.domain.repository.VoiceRepository;
import sh.delo.perth.core.network.ZealotTransport;
import sh.delo.perth.feature.voice.domain.CaptureVoiceUseCase;
import sh.delo.perth.feature.voice.domain.WriteTaskUseCase;

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
public final class VoiceViewModel_Factory implements Factory<VoiceViewModel> {
  private final Provider<CaptureVoiceUseCase> captureVoiceUseCaseProvider;

  private final Provider<VoiceRepository> voiceRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<ZealotTransport> transportProvider;

  private final Provider<WriteTaskUseCase> writeTaskUseCaseProvider;

  public VoiceViewModel_Factory(Provider<CaptureVoiceUseCase> captureVoiceUseCaseProvider,
      Provider<VoiceRepository> voiceRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<ZealotTransport> transportProvider,
      Provider<WriteTaskUseCase> writeTaskUseCaseProvider) {
    this.captureVoiceUseCaseProvider = captureVoiceUseCaseProvider;
    this.voiceRepositoryProvider = voiceRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.transportProvider = transportProvider;
    this.writeTaskUseCaseProvider = writeTaskUseCaseProvider;
  }

  @Override
  public VoiceViewModel get() {
    return newInstance(captureVoiceUseCaseProvider.get(), voiceRepositoryProvider.get(), settingsRepositoryProvider.get(), transportProvider.get(), writeTaskUseCaseProvider.get());
  }

  public static VoiceViewModel_Factory create(
      javax.inject.Provider<CaptureVoiceUseCase> captureVoiceUseCaseProvider,
      javax.inject.Provider<VoiceRepository> voiceRepositoryProvider,
      javax.inject.Provider<SettingsRepository> settingsRepositoryProvider,
      javax.inject.Provider<ZealotTransport> transportProvider,
      javax.inject.Provider<WriteTaskUseCase> writeTaskUseCaseProvider) {
    return new VoiceViewModel_Factory(Providers.asDaggerProvider(captureVoiceUseCaseProvider), Providers.asDaggerProvider(voiceRepositoryProvider), Providers.asDaggerProvider(settingsRepositoryProvider), Providers.asDaggerProvider(transportProvider), Providers.asDaggerProvider(writeTaskUseCaseProvider));
  }

  public static VoiceViewModel_Factory create(
      Provider<CaptureVoiceUseCase> captureVoiceUseCaseProvider,
      Provider<VoiceRepository> voiceRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<ZealotTransport> transportProvider,
      Provider<WriteTaskUseCase> writeTaskUseCaseProvider) {
    return new VoiceViewModel_Factory(captureVoiceUseCaseProvider, voiceRepositoryProvider, settingsRepositoryProvider, transportProvider, writeTaskUseCaseProvider);
  }

  public static VoiceViewModel newInstance(CaptureVoiceUseCase captureVoiceUseCase,
      VoiceRepository voiceRepository, SettingsRepository settingsRepository,
      ZealotTransport transport, WriteTaskUseCase writeTaskUseCase) {
    return new VoiceViewModel(captureVoiceUseCase, voiceRepository, settingsRepository, transport, writeTaskUseCase);
  }
}
