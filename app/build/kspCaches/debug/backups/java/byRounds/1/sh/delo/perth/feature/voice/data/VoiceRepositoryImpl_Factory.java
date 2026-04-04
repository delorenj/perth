package sh.delo.perth.feature.voice.data;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.Providers;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import sh.delo.perth.core.network.SpeechRecognizer;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("javax.inject.Named")
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
public final class VoiceRepositoryImpl_Factory implements Factory<VoiceRepositoryImpl> {
  private final Provider<SpeechRecognizer> primaryRecognizerProvider;

  private final Provider<SpeechRecognizer> fallbackRecognizerProvider;

  public VoiceRepositoryImpl_Factory(Provider<SpeechRecognizer> primaryRecognizerProvider,
      Provider<SpeechRecognizer> fallbackRecognizerProvider) {
    this.primaryRecognizerProvider = primaryRecognizerProvider;
    this.fallbackRecognizerProvider = fallbackRecognizerProvider;
  }

  @Override
  public VoiceRepositoryImpl get() {
    return newInstance(primaryRecognizerProvider.get(), fallbackRecognizerProvider.get());
  }

  public static VoiceRepositoryImpl_Factory create(
      javax.inject.Provider<SpeechRecognizer> primaryRecognizerProvider,
      javax.inject.Provider<SpeechRecognizer> fallbackRecognizerProvider) {
    return new VoiceRepositoryImpl_Factory(Providers.asDaggerProvider(primaryRecognizerProvider), Providers.asDaggerProvider(fallbackRecognizerProvider));
  }

  public static VoiceRepositoryImpl_Factory create(
      Provider<SpeechRecognizer> primaryRecognizerProvider,
      Provider<SpeechRecognizer> fallbackRecognizerProvider) {
    return new VoiceRepositoryImpl_Factory(primaryRecognizerProvider, fallbackRecognizerProvider);
  }

  public static VoiceRepositoryImpl newInstance(SpeechRecognizer primaryRecognizer,
      SpeechRecognizer fallbackRecognizer) {
    return new VoiceRepositoryImpl(primaryRecognizer, fallbackRecognizer);
  }
}
