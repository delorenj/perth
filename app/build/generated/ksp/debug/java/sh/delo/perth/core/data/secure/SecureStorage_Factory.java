package sh.delo.perth.core.data.secure;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SecureStorage_Factory implements Factory<SecureStorage> {
  private final Provider<Context> contextProvider;

  public SecureStorage_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public SecureStorage get() {
    return newInstance(contextProvider.get());
  }

  public static SecureStorage_Factory create(javax.inject.Provider<Context> contextProvider) {
    return new SecureStorage_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static SecureStorage_Factory create(Provider<Context> contextProvider) {
    return new SecureStorage_Factory(contextProvider);
  }

  public static SecureStorage newInstance(Context context) {
    return new SecureStorage(context);
  }
}
