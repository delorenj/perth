package sh.delo.perth.feature.settings.ui;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import sh.delo.perth.core.domain.repository.LlmRepository;
import sh.delo.perth.core.domain.repository.SessionRepository;
import sh.delo.perth.core.domain.repository.SettingsRepository;
import sh.delo.perth.core.network.ZealotTransport;

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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  private final Provider<ZealotTransport> transportProvider;

  private final Provider<LlmRepository> llmRepositoryProvider;

  public SettingsViewModel_Factory(Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<ZealotTransport> transportProvider, Provider<LlmRepository> llmRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.transportProvider = transportProvider;
    this.llmRepositoryProvider = llmRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(settingsRepositoryProvider.get(), sessionRepositoryProvider.get(), transportProvider.get(), llmRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(
      javax.inject.Provider<SettingsRepository> settingsRepositoryProvider,
      javax.inject.Provider<SessionRepository> sessionRepositoryProvider,
      javax.inject.Provider<ZealotTransport> transportProvider,
      javax.inject.Provider<LlmRepository> llmRepositoryProvider) {
    return new SettingsViewModel_Factory(Providers.asDaggerProvider(settingsRepositoryProvider), Providers.asDaggerProvider(sessionRepositoryProvider), Providers.asDaggerProvider(transportProvider), Providers.asDaggerProvider(llmRepositoryProvider));
  }

  public static SettingsViewModel_Factory create(
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<ZealotTransport> transportProvider, Provider<LlmRepository> llmRepositoryProvider) {
    return new SettingsViewModel_Factory(settingsRepositoryProvider, sessionRepositoryProvider, transportProvider, llmRepositoryProvider);
  }

  public static SettingsViewModel newInstance(SettingsRepository settingsRepository,
      SessionRepository sessionRepository, ZealotTransport transport, LlmRepository llmRepository) {
    return new SettingsViewModel(settingsRepository, sessionRepository, transport, llmRepository);
  }
}
