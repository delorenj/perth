package sh.delo.perth.feature.terminal.ui;

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
import sh.delo.perth.feature.terminal.domain.NavigatePaneUseCase;
import sh.delo.perth.feature.terminal.domain.SendInputUseCase;

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
public final class TerminalViewModel_Factory implements Factory<TerminalViewModel> {
  private final Provider<ZealotTransport> transportProvider;

  private final Provider<SessionRepository> sessionRepositoryProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  private final Provider<NavigatePaneUseCase> navigatePaneUseCaseProvider;

  private final Provider<SendInputUseCase> sendInputUseCaseProvider;

  public TerminalViewModel_Factory(Provider<ZealotTransport> transportProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<NavigatePaneUseCase> navigatePaneUseCaseProvider,
      Provider<SendInputUseCase> sendInputUseCaseProvider) {
    this.transportProvider = transportProvider;
    this.sessionRepositoryProvider = sessionRepositoryProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
    this.navigatePaneUseCaseProvider = navigatePaneUseCaseProvider;
    this.sendInputUseCaseProvider = sendInputUseCaseProvider;
  }

  @Override
  public TerminalViewModel get() {
    return newInstance(transportProvider.get(), sessionRepositoryProvider.get(), settingsRepositoryProvider.get(), navigatePaneUseCaseProvider.get(), sendInputUseCaseProvider.get());
  }

  public static TerminalViewModel_Factory create(
      javax.inject.Provider<ZealotTransport> transportProvider,
      javax.inject.Provider<SessionRepository> sessionRepositoryProvider,
      javax.inject.Provider<SettingsRepository> settingsRepositoryProvider,
      javax.inject.Provider<NavigatePaneUseCase> navigatePaneUseCaseProvider,
      javax.inject.Provider<SendInputUseCase> sendInputUseCaseProvider) {
    return new TerminalViewModel_Factory(Providers.asDaggerProvider(transportProvider), Providers.asDaggerProvider(sessionRepositoryProvider), Providers.asDaggerProvider(settingsRepositoryProvider), Providers.asDaggerProvider(navigatePaneUseCaseProvider), Providers.asDaggerProvider(sendInputUseCaseProvider));
  }

  public static TerminalViewModel_Factory create(Provider<ZealotTransport> transportProvider,
      Provider<SessionRepository> sessionRepositoryProvider,
      Provider<SettingsRepository> settingsRepositoryProvider,
      Provider<NavigatePaneUseCase> navigatePaneUseCaseProvider,
      Provider<SendInputUseCase> sendInputUseCaseProvider) {
    return new TerminalViewModel_Factory(transportProvider, sessionRepositoryProvider, settingsRepositoryProvider, navigatePaneUseCaseProvider, sendInputUseCaseProvider);
  }

  public static TerminalViewModel newInstance(ZealotTransport transport,
      SessionRepository sessionRepository, SettingsRepository settingsRepository,
      NavigatePaneUseCase navigatePaneUseCase, SendInputUseCase sendInputUseCase) {
    return new TerminalViewModel(transport, sessionRepository, settingsRepository, navigatePaneUseCase, sendInputUseCase);
  }
}
