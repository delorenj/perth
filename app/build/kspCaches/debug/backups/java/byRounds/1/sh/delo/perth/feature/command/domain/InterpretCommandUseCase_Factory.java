package sh.delo.perth.feature.command.domain;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import sh.delo.perth.core.domain.repository.LlmRepository;

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
public final class InterpretCommandUseCase_Factory implements Factory<InterpretCommandUseCase> {
  private final Provider<LlmRepository> llmRepositoryProvider;

  private final Provider<CommandSafetyGate> safetyGateProvider;

  public InterpretCommandUseCase_Factory(Provider<LlmRepository> llmRepositoryProvider,
      Provider<CommandSafetyGate> safetyGateProvider) {
    this.llmRepositoryProvider = llmRepositoryProvider;
    this.safetyGateProvider = safetyGateProvider;
  }

  @Override
  public InterpretCommandUseCase get() {
    return newInstance(llmRepositoryProvider.get(), safetyGateProvider.get());
  }

  public static InterpretCommandUseCase_Factory create(
      javax.inject.Provider<LlmRepository> llmRepositoryProvider,
      javax.inject.Provider<CommandSafetyGate> safetyGateProvider) {
    return new InterpretCommandUseCase_Factory(Providers.asDaggerProvider(llmRepositoryProvider), Providers.asDaggerProvider(safetyGateProvider));
  }

  public static InterpretCommandUseCase_Factory create(
      Provider<LlmRepository> llmRepositoryProvider,
      Provider<CommandSafetyGate> safetyGateProvider) {
    return new InterpretCommandUseCase_Factory(llmRepositoryProvider, safetyGateProvider);
  }

  public static InterpretCommandUseCase newInstance(LlmRepository llmRepository,
      CommandSafetyGate safetyGate) {
    return new InterpretCommandUseCase(llmRepository, safetyGate);
  }
}
