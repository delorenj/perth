package sh.delo.perth.feature.terminal.domain;

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
public final class SendInputUseCase_Factory implements Factory<SendInputUseCase> {
  private final Provider<ZealotTransport> transportProvider;

  public SendInputUseCase_Factory(Provider<ZealotTransport> transportProvider) {
    this.transportProvider = transportProvider;
  }

  @Override
  public SendInputUseCase get() {
    return newInstance(transportProvider.get());
  }

  public static SendInputUseCase_Factory create(
      javax.inject.Provider<ZealotTransport> transportProvider) {
    return new SendInputUseCase_Factory(Providers.asDaggerProvider(transportProvider));
  }

  public static SendInputUseCase_Factory create(Provider<ZealotTransport> transportProvider) {
    return new SendInputUseCase_Factory(transportProvider);
  }

  public static SendInputUseCase newInstance(ZealotTransport transport) {
    return new SendInputUseCase(transport);
  }
}
