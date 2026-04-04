package sh.delo.perth.core.data.repository;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import sh.delo.perth.core.data.datastore.UserPreferences;
import sh.delo.perth.core.data.secure.SecureStorage;

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
public final class SettingsRepositoryImpl_Factory implements Factory<SettingsRepositoryImpl> {
  private final Provider<UserPreferences> userPreferencesProvider;

  private final Provider<SecureStorage> secureStorageProvider;

  public SettingsRepositoryImpl_Factory(Provider<UserPreferences> userPreferencesProvider,
      Provider<SecureStorage> secureStorageProvider) {
    this.userPreferencesProvider = userPreferencesProvider;
    this.secureStorageProvider = secureStorageProvider;
  }

  @Override
  public SettingsRepositoryImpl get() {
    return newInstance(userPreferencesProvider.get(), secureStorageProvider.get());
  }

  public static SettingsRepositoryImpl_Factory create(
      javax.inject.Provider<UserPreferences> userPreferencesProvider,
      javax.inject.Provider<SecureStorage> secureStorageProvider) {
    return new SettingsRepositoryImpl_Factory(Providers.asDaggerProvider(userPreferencesProvider), Providers.asDaggerProvider(secureStorageProvider));
  }

  public static SettingsRepositoryImpl_Factory create(
      Provider<UserPreferences> userPreferencesProvider,
      Provider<SecureStorage> secureStorageProvider) {
    return new SettingsRepositoryImpl_Factory(userPreferencesProvider, secureStorageProvider);
  }

  public static SettingsRepositoryImpl newInstance(UserPreferences userPreferences,
      SecureStorage secureStorage) {
    return new SettingsRepositoryImpl(userPreferences, secureStorage);
  }
}
