package sh.delo.perth.core.data.datastore;

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
public final class UserPreferences_Factory implements Factory<UserPreferences> {
  private final Provider<Context> contextProvider;

  public UserPreferences_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public UserPreferences get() {
    return newInstance(contextProvider.get());
  }

  public static UserPreferences_Factory create(javax.inject.Provider<Context> contextProvider) {
    return new UserPreferences_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static UserPreferences_Factory create(Provider<Context> contextProvider) {
    return new UserPreferences_Factory(contextProvider);
  }

  public static UserPreferences newInstance(Context context) {
    return new UserPreferences(context);
  }
}
