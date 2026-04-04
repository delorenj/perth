package sh.delo.perth.feature.terminal.domain;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class NavigatePaneUseCase_Factory implements Factory<NavigatePaneUseCase> {
  @Override
  public NavigatePaneUseCase get() {
    return newInstance();
  }

  public static NavigatePaneUseCase_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static NavigatePaneUseCase newInstance() {
    return new NavigatePaneUseCase();
  }

  private static final class InstanceHolder {
    static final NavigatePaneUseCase_Factory INSTANCE = new NavigatePaneUseCase_Factory();
  }
}
