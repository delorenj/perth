package sh.delo.perth.core.network

import kotlinx.coroutines.flow.StateFlow
import sh.delo.perth.core.domain.model.Transcript
import sh.delo.perth.core.result.AppResult

/** Abstraction over on-device or cloud speech recognition providers. */
interface SpeechRecognizer {

    /** Whether this recognizer is ready to process audio. */
    val isAvailable: StateFlow<Boolean>

    /**
     * Records audio from the microphone until silence is detected,
     * then returns the transcribed [Transcript].
     */
    suspend fun recognize(): AppResult<Transcript>
}
