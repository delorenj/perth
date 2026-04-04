package sh.delo.perth.feature.session.ui;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import sh.delo.perth.core.domain.repository.SessionRepository;
import sh.delo.perth.core.domain.repository.SettingsRepository;
import sh.delo.perth.core.network.ZealotTransport;
import sh.delo.perth.feature.session.domain.ConnectToSessionUseCase;
import sh.delo.perth.feature.session.domain.GetSessionsUseCase;

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
public final class SessionListViewModel_Factory implements Factory<SessionListViewModel> {
  private final Provider<ZealotTransport> transportProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<GetSessionsUseCase> getSessionsUseCaseProvider;

  private final Provider<ConnectToSessionUseCase> connectToSessionUseCaseProvider;

  public SessionListViewModel_Factory(Provider<ZealotTransport> transportProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<GetSessionsUseCase> getSessionsUseCaseProvider,
      Provider<ConnectToSessionUseCase> connectToSessionUseCaseProvider) {
    this.transportProvider = transportProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.getSessionsUseCaseProvider = getSessionsUseCaseProvider;
    this.connectToSessionUseCaseProvider = connectToSessionUseCaseProvider;
  }

  @Override
  public SessionListViewModel get() {
    return newInstance(transportProvider.get(), sessionRepositoryProvider.get(), settingsRepositoryProvider.get(), getSessionsUseCaseProvider.get(), connectToSessionUseCaseProvider.get());
  }

  public static SessionListViewModel_Factory create(
      javax.inject.Provider<ZealotTransport> transportProvider,
      javax.inject.Provider<SessionRepository> sessionRepositoryProvider,
      javax.inject.Provider<SettingsRepository> settingsRepositoryProvider,
      javax.inject.Provider<GetSessionsUseCase> getSessionsUseCaseProvider,
      javax.inject.Provider<ConnectToSessionUseCase> connectToSessionUseCaseProvider) {
    return new SessionListViewModel_Factory(Providers.asDaggerProvider(transportProvider), Providers.asDaggerProvider(sessionRepositoryProvider), Providers.asDaggerProvider(settingsRepositoryProvider), Providers.asDaggerProvider(getSessionsUseCaseProvider), Providers.asDaggerProvider(connectToSessionUseCaseProvider));
  }

  public static SessionListViewModel_Factory create(Provider<ZealotTransport> transportProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<GetSessionsUseCase> getSessionsUseCaseProvider,
      Provider<ConnectToSessionUseCase> connectToSessionUseCaseProvider) {
    return new SessionListViewModel_Factory(transportProvider, sessionRepositoryProvider, settingsRepositoryProvider, getSessionsUseCaseProvider, connectToSessionUseCaseProvider);
  }

  public static SessionListViewModel newInstance(ZealotTransport transport,
      SessionRepository sessionRepository, SettingsRepository settingsRepository,
      GetSessionsUseCase getSessionsUseCase, ConnectToSessionUseCase connectToSessionUseCase) {
    return new SessionListViewModel(transport, sessionRepository, settingsRepository, getSessionsUseCase, connectToSessionUseCase);
  }
}
