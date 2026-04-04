package sh.delo.perth.feature.voice.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer as AndroidSdk
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import sh.delo.perth.core.domain.model.Transcript
import sh.delo.perth.core.network.SpeechRecognizer
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume

/**
 * Wraps the Android platform [android.speech.SpeechRecognizer] API behind the Perth
 * [SpeechRecognizer] interface.
 *
 * The callback-based Android API is bridged to a coroutine via [suspendCancellableCoroutine].
 * Audio is never persisted — it flows directly from the microphone through the recognizer.
 *
 * Must be created and used on the main thread (Android SDK requirement).
 */
class AndroidSpeechRecognizer @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechRecognizer {

    private val _isAvailable = MutableStateFlow(AndroidSdk.isRecognitionAvailable(context))
    override val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    override suspend fun recognize(): AppResult<Transcript> =
        suspendCancellableCoroutine { continuation ->
            val recognizer = AndroidSdk.createSpeechRecognizer(context)

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            val listener = object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    Timber.d("AndroidSpeechRecognizer: ready for speech")
                }

                override fun onBeginningOfSpeech() {
                    Timber.d("AndroidSpeechRecognizer: speech started")
                }

                override fun onRmsChanged(rmsdB: Float) = Unit

                override fun onBufferReceived(buffer: ByteArray?) = Unit

                override fun onEndOfSpeech() {
                    Timber.d("AndroidSpeechRecognizer: speech ended")
                }

                override fun onError(error: Int) {
                    val message = sdkErrorMessage(error)
                    Timber.e("AndroidSpeechRecognizer: error %d — %s", error, message)
                    recognizer.destroy()
                    if (continuation.isActive) {
                        continuation.resume(
                            AppResult.Error(AppException.Voice(message))
                        )
                    }
                }

                override fun onResults(results: Bundle?) {
                    val matches = results
                        ?.getStringArrayList(AndroidSdk.RESULTS_RECOGNITION)
                        ?.firstOrNull()

                    val confidences = results
                        ?.getFloatArray(AndroidSdk.CONFIDENCE_SCORES)

                    recognizer.destroy()

                    if (matches == null) {
                        if (continuation.isActive) {
                            continuation.resume(
                                AppResult.Error(
                                    AppException.Voice("No recognition results returned")
                                )
                            )
                        }
                        return
                    }

                    val confidence = confidences?.firstOrNull() ?: 1.0f
                    val transcript = Transcript(
                        text = matches,
                        confidence = confidence,
                        provider = Transcript.SpeechProvider.MlKit,
                    )
                    if (continuation.isActive) {
                        continuation.resume(AppResult.Success(transcript))
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) = Unit

                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            }

            recognizer.setRecognitionListener(listener)
            recognizer.startListening(intent)

            continuation.invokeOnCancellation {
                Timber.d("AndroidSpeechRecognizer: cancelled")
                recognizer.stopListening()
                recognizer.destroy()
            }
        }

    private fun sdkErrorMessage(error: Int): String = when (error) {
        AndroidSdk.ERROR_AUDIO -> "Audio recording error"
        AndroidSdk.ERROR_CLIENT -> "Client-side recognition error"
        AndroidSdk.ERROR_INSUFFICIENT_PERMISSIONS -> "Missing RECORD_AUDIO permission"
        AndroidSdk.ERROR_NETWORK -> "Network error during recognition"
        AndroidSdk.ERROR_NETWORK_TIMEOUT -> "Network timeout during recognition"
        AndroidSdk.ERROR_NO_MATCH -> "No speech recognized — please try again"
        AndroidSdk.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy"
        AndroidSdk.ERROR_SERVER -> "Server-side recognition error"
        AndroidSdk.ERROR_SPEECH_TIMEOUT -> "No speech detected"
        else -> "Unknown recognition error ($error)"
    }
}
