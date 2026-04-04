package sh.delo.perth.feature.command.domain;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import sh.delo.perth.core.domain.repository.CommandAuditRepository;
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
public final class ExecuteCommandUseCase_Factory implements Factory<ExecuteCommandUseCase> {
  private final Provider<ZealotTransport> transportProvider;

  private final Provider<CommandAuditRepository> auditRepositoryProvider;

  public ExecuteCommandUseCase_Factory(Provider<ZealotTransport> transportProvider,
      Provider<CommandAuditRepository> auditRepositoryProvider) {
    this.transportProvider = transportProvider;
    this.auditRepositoryProvider = auditRepositoryProvider;
  }

  @Override
  public ExecuteCommandUseCase get() {
    return newInstance(transportProvider.get(), auditRepositoryProvider.get());
  }

  public static ExecuteCommandUseCase_Factory create(
      javax.inject.Provider<ZealotTransport> transportProvider,
      javax.inject.Provider<CommandAuditRepository> auditRepositoryProvider) {
    return new ExecuteCommandUseCase_Factory(Providers.asDaggerProvider(transportProvider), Providers.asDaggerProvider(auditRepositoryProvider));
  }

  public static ExecuteCommandUseCase_Factory create(Provider<ZealotTransport> transportProvider,
      Provider<CommandAuditRepository> auditRepositoryProvider) {
    return new ExecuteCommandUseCase_Factory(transportProvider, auditRepositoryProvider);
  }

  public static ExecuteCommandUseCase newInstance(ZealotTransport transport,
      CommandAuditRepository auditRepository) {
    return new ExecuteCommandUseCase(transport, auditRepository);
  }
}
