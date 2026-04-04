package sh.delo.perth.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;

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
public final class WebSocketZealotTransport_Factory implements Factory<WebSocketZealotTransport> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  public WebSocketZealotTransport_Factory(Provider<OkHttpClient> okHttpClientProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public WebSocketZealotTransport get() {
    return newInstance(okHttpClientProvider.get());
  }

  public static WebSocketZealotTransport_Factory create(
      javax.inject.Provider<OkHttpClient> okHttpClientProvider) {
    return new WebSocketZealotTransport_Factory(Providers.asDaggerProvider(okHttpClientProvider));
  }

  public static WebSocketZealotTransport_Factory create(
      Provider<OkHttpClient> okHttpClientProvider) {
    return new WebSocketZealotTransport_Factory(okHttpClientProvider);
  }

  public static WebSocketZealotTransport newInstance(OkHttpClient okHttpClient) {
    return new WebSocketZealotTransport(okHttpClient);
  }
}
