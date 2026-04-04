package sh.delo.perth.core.di;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import sh.delo.perth.core.data.db.PerthDatabase;
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
public final class DatabaseModule_ProvideCommandAuditDaoFactory implements Factory<CommandAuditDao> {
  private final Provider<PerthDatabase> dbProvider;

  public DatabaseModule_ProvideCommandAuditDaoFactory(Provider<PerthDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public CommandAuditDao get() {
    return provideCommandAuditDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideCommandAuditDaoFactory create(
      javax.inject.Provider<PerthDatabase> dbProvider) {
    return new DatabaseModule_ProvideCommandAuditDaoFactory(Providers.asDaggerProvider(dbProvider));
  }

  public static DatabaseModule_ProvideCommandAuditDaoFactory create(
      Provider<PerthDatabase> dbProvider) {
    return new DatabaseModule_ProvideCommandAuditDaoFactory(dbProvider);
  }

  public static CommandAuditDao provideCommandAuditDao(PerthDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideCommandAuditDao(db));
  }
}
