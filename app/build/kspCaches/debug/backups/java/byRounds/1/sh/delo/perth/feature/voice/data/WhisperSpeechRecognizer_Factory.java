package sh.delo.perth.feature.voice.data;

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
public final class WhisperSpeechRecognizer_Factory implements Factory<WhisperSpeechRecognizer> {
  @Override
  public WhisperSpeechRecognizer get() {
    return newInstance();
  }

  public static WhisperSpeechRecognizer_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static WhisperSpeechRecognizer newInstance() {
    return new WhisperSpeechRecognizer();
  }

  private static final class InstanceHolder {
    static final WhisperSpeechRecognizer_Factory INSTANCE = new WhisperSpeechRecognizer_Factory();
  }
}
