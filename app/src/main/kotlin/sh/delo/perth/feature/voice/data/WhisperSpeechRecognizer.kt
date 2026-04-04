package sh.delo.perth.feature.voice.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import sh.delo.perth.core.domain.model.Transcript
import sh.delo.perth.core.network.SpeechRecognizer
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import javax.inject.Inject

/**
 * Placeholder implementation of [SpeechRecognizer] backed by the OpenAI Whisper API.
 *
 * The HTTP integration is deferred — this class satisfies the interface contract so the
 * DI graph can bind it as a fallback. Replace the TODO body with the actual Whisper call
 * once an API key flow and audio-file export are available.
 */
class WhisperSpeechRecognizer @Inject constructor() : SpeechRecognizer {

    // Whisper requires a valid API key; mark unavailable until configured.
    private val _isAvailable = MutableStateFlow(false)
    override val isAvailable: StateFlow<Boolean> = _isAvailable.asStateFlow()

    override suspend fun recognize(): AppResult<Transcript> {
        // TODO: Implement Whisper API call.
        //  Steps:
        //  1. Export the recorded audio as a temporary WAV/OGG file.
        //  2. POST to https://api.openai.com/v1/audio/transcriptions with multipart form.
        //  3. Parse the JSON response and return Transcript(text, confidence, SpeechProvider.Whisper).
        //  4. Delete the temporary file regardless of outcome.
        return AppResult.Error(
            AppException.Voice("Whisper speech recognition is not yet implemented")
        )
    }
}
