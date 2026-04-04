package sh.delo.perth.feature.command.data;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
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
public final class LlmRepositoryImpl_Factory implements Factory<LlmRepositoryImpl> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<SecureStorage> secureStorageProvider;

  public LlmRepositoryImpl_Factory(Provider<OkHttpClient> okHttpClientProvider,
      Provider<SecureStorage> secureStorageProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
    this.secureStorageProvider = secureStorageProvider;
  }

  @Override
  public LlmRepositoryImpl get() {
    return newInstance(okHttpClientProvider.get(), secureStorageProvider.get());
  }

  public static LlmRepositoryImpl_Factory create(
      javax.inject.Provider<OkHttpClient> okHttpClientProvider,
      javax.inject.Provider<SecureStorage> secureStorageProvider) {
    return new LlmRepositoryImpl_Factory(Providers.asDaggerProvider(okHttpClientProvider), Providers.asDaggerProvider(secureStorageProvider));
  }

  public static LlmRepositoryImpl_Factory create(Provider<OkHttpClient> okHttpClientProvider,
      Provider<SecureStorage> secureStorageProvider) {
    return new LlmRepositoryImpl_Factory(okHttpClientProvider, secureStorageProvider);
  }

  public static LlmRepositoryImpl newInstance(OkHttpClient okHttpClient,
      SecureStorage secureStorage) {
    return new LlmRepositoryImpl(okHttpClient, secureStorage);
  }
}
