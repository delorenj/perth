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
public final class GetSessionsUseCase_Factory implements Factory<GetSessionsUseCase> {
  private final Provider<SessionRepository> sessionRepositoryProvider;

  public GetSessionsUseCase_Factory(Provider<SessionRepository> sessionRepositoryProvider) {
    this.sessionRepositoryProvider = sessionRepositoryProvider;
  }

  @Override
  public GetSessionsUseCase get() {
    return newInstance(sessionRepositoryProvider.get());
  }

  public static GetSessionsUseCase_Factory create(
      javax.inject.Provider<SessionRepository> sessionRepositoryProvider) {
    return new GetSessionsUseCase_Factory(Providers.asDaggerProvider(sessionRepositoryProvider));
  }

  public static GetSessionsUseCase_Factory create(
      Provider<SessionRepository> sessionRepositoryProvider) {
    return new GetSessionsUseCase_Factory(sessionRepositoryProvider);
  }

  public static GetSessionsUseCase newInstance(SessionRepository sessionRepository) {
    return new GetSessionsUseCase(sessionRepository);
  }
}
