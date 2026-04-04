package sh.delo.perth.core.network;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class MockZealotTransport_Factory implements Factory<MockZealotTransport> {
  @Override
  public MockZealotTransport get() {
    return newInstance();
  }

  public static MockZealotTransport_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MockZealotTransport newInstance() {
    return new MockZealotTransport();
  }

  private static final class InstanceHolder {
    static final MockZealotTransport_Factory INSTANCE = new MockZealotTransport_Factory();
  }
}
