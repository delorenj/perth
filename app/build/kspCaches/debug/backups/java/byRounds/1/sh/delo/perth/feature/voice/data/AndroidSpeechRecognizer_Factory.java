package sh.delo.perth.feature.voice.data;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class AndroidSpeechRecognizer_Factory implements Factory<AndroidSpeechRecognizer> {
  private final Provider<Context> contextProvider;

  public AndroidSpeechRecognizer_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public AndroidSpeechRecognizer get() {
    return newInstance(contextProvider.get());
  }

  public static AndroidSpeechRecognizer_Factory create(
      javax.inject.Provider<Context> contextProvider) {
    return new AndroidSpeechRecognizer_Factory(Providers.asDaggerProvider(contextProvider));
  }

  public static AndroidSpeechRecognizer_Factory create(Provider<Context> contextProvider) {
    return new AndroidSpeechRecognizer_Factory(contextProvider);
  }

  public static AndroidSpeechRecognizer newInstance(Context context) {
    return new AndroidSpeechRecognizer(context);
  }
}
