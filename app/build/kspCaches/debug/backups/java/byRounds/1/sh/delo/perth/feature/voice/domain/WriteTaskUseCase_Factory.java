package sh.delo.perth.feature.voice.domain;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
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
public final class WriteTaskUseCase_Factory implements Factory<WriteTaskUseCase> {
  private final Provider<ZealotTransport> transportProvider;

  public WriteTaskUseCase_Factory(Provider<ZealotTransport> transportProvider) {
    this.transportProvider = transportProvider;
  }

  @Override
  public WriteTaskUseCase get() {
    return newInstance(transportProvider.get());
  }

  public static WriteTaskUseCase_Factory create(
      javax.inject.Provider<ZealotTransport> transportProvider) {
    return new WriteTaskUseCase_Factory(Providers.asDaggerProvider(transportProvider));
  }

  public static WriteTaskUseCase_Factory create(Provider<ZealotTransport> transportProvider) {
    return new WriteTaskUseCase_Factory(transportProvider);
  }

  public static WriteTaskUseCase newInstance(ZealotTransport transport) {
    return new WriteTaskUseCase(transport);
  }
}
