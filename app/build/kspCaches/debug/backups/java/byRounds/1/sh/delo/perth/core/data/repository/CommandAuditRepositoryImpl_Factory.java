package sh.delo.perth.core.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import sh.delo.perth.core.data.db.dao.CommandAuditDao;

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
public final class CommandAuditRepositoryImpl_Factory implements Factory<CommandAuditRepositoryImpl> {
  private final Provider<CommandAuditDao> commandAuditDaoProvider;

  public CommandAuditRepositoryImpl_Factory(Provider<CommandAuditDao> commandAuditDaoProvider) {
    this.commandAuditDaoProvider = commandAuditDaoProvider;
  }

  @Override
  public CommandAuditRepositoryImpl get() {
    return newInstance(commandAuditDaoProvider.get());
  }

  public static CommandAuditRepositoryImpl_Factory create(
      javax.inject.Provider<CommandAuditDao> commandAuditDaoProvider) {
    return new CommandAuditRepositoryImpl_Factory(Providers.asDaggerProvider(commandAuditDaoProvider));
  }

  public static CommandAuditRepositoryImpl_Factory create(
      Provider<CommandAuditDao> commandAuditDaoProvider) {
    return new CommandAuditRepositoryImpl_Factory(commandAuditDaoProvider);
  }

  public static CommandAuditRepositoryImpl newInstance(CommandAuditDao commandAuditDao) {
    return new CommandAuditRepositoryImpl(commandAuditDao);
  }
}
