package sh.delo.perth.feature.command.ui;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import sh.delo.perth.core.domain.repository.SettingsRepository;
import sh.delo.perth.feature.command.domain.ExecuteCommandUseCase;
import sh.delo.perth.feature.command.domain.InterpretCommandUseCase;

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
public final class CommandViewModel_Factory implements Factory<CommandViewModel> {
  private final Provider<InterpretCommandUseCase> interpretCommandUseCaseProvider;

  private final Provider<ExecuteCommandUseCase> executeCommandUseCaseProvider;

  private final Provider<SettingsRepository> settingsRepositoryProvider;

  public CommandViewModel_Factory(Provider<InterpretCommandUseCase> interpretCommandUseCaseProvider,
      Provider<ExecuteCommandUseCase> executeCommandUseCaseProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    this.interpretCommandUseCaseProvider = interpretCommandUseCaseProvider;
    this.executeCommandUseCaseProvider = executeCommandUseCaseProvider;
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public CommandViewModel get() {
    return newInstance(interpretCommandUseCaseProvider.get(), executeCommandUseCaseProvider.get(), settingsRepositoryProvider.get());
  }

  public static CommandViewModel_Factory create(
      javax.inject.Provider<InterpretCommandUseCase> interpretCommandUseCaseProvider,
      javax.inject.Provider<ExecuteCommandUseCase> executeCommandUseCaseProvider,
      javax.inject.Provider<SettingsRepository> settingsRepositoryProvider) {
    return new CommandViewModel_Factory(Providers.asDaggerProvider(interpretCommandUseCaseProvider), Providers.asDaggerProvider(executeCommandUseCaseProvider), Providers.asDaggerProvider(settingsRepositoryProvider));
  }

  public static CommandViewModel_Factory create(
      Provider<InterpretCommandUseCase> interpretCommandUseCaseProvider,
      Provider<ExecuteCommandUseCase> executeCommandUseCaseProvider,
      Provider<SettingsRepository> settingsRepositoryProvider) {
    return new CommandViewModel_Factory(interpretCommandUseCaseProvider, executeCommandUseCaseProvider, settingsRepositoryProvider);
  }

  public static CommandViewModel newInstance(InterpretCommandUseCase interpretCommandUseCase,
      ExecuteCommandUseCase executeCommandUseCase, SettingsRepository settingsRepository) {
    return new CommandViewModel(interpretCommandUseCase, executeCommandUseCase, settingsRepository);
  }
}
