package sh.delo.perth.core.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import sh.delo.perth.core.data.db.dao.SessionDao;
import sh.delo.perth.core.network.ZealotTransport;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SessionRepositoryImpl_Factory implements Factory<SessionRepositoryImpl> {
  private final Provider<ZealotTransport> transportProvider;

  private final Provider<SessionDao> sessionDaoProvider;

  public SessionRepositoryImpl_Factory(Provider<ZealotTransport> transportProvider,
      Provider<SessionDao> sessionDaoProvider) {
    this.transportProvider = transportProvider;
    this.sessionDaoProvider = sessionDaoProvider;
  }

  @Override
  public SessionRepositoryImpl get() {
    return newInstance(transportProvider.get(), sessionDaoProvider.get());
  }

  public static SessionRepositoryImpl_Factory create(
      javax.inject.Provider<ZealotTransport> transportProvider,
      javax.inject.Provider<SessionDao> sessionDaoProvider) {
    return new SessionRepositoryImpl_Factory(Providers.asDaggerProvider(transportProvider), Providers.asDaggerProvider(sessionDaoProvider));
  }

  public static SessionRepositoryImpl_Factory create(Provider<ZealotTransport> transportProvider,
      Provider<SessionDao> sessionDaoProvider) {
    return new SessionRepositoryImpl_Factory(transportProvider, sessionDaoProvider);
  }

  public static SessionRepositoryImpl newInstance(ZealotTransport transport,
      SessionDao sessionDao) {
    return new SessionRepositoryImpl(transport, sessionDao);
  }
}
