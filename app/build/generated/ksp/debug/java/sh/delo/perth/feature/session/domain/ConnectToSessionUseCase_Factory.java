package sh.delo.perth.feature.session.domain;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import sh.delo.perth.core.domain.repository.SessionRepository;

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
public final class ConnectToSessionUseCase_Factory implements Factory<ConnectToSessionUseCase> {
  private final Provider<SessionRepository> sessionRepositoryProvider;

  public ConnectToSessionUseCase_Factory(Provider<SessionRepository> sessionRepositoryProvider) {
    this.sessionRepositoryProvider = sessionRepositoryProvider;
  }

  @Override
  public ConnectToSessionUseCase get() {
    return newInstance(sessionRepositoryProvider.get());
  }

  public static ConnectToSessionUseCase_Factory create(
      javax.inject.Provider<SessionRepository> sessionRepositoryProvider) {
    return new ConnectToSessionUseCase_Factory(Providers.asDaggerProvider(sessionRepositoryProvider));
  }

  public static ConnectToSessionUseCase_Factory create(
      Provider<SessionRepository> sessionRepositoryProvider) {
    return new ConnectToSessionUseCase_Factory(sessionRepositoryProvider);
  }

  public static ConnectToSessionUseCase newInstance(SessionRepository sessionRepository) {
    return new ConnectToSessionUseCase(sessionRepository);
  }
}
