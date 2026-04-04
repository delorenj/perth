package sh.delo.perth.core.domain.repository

import kotlinx.coroutines.flow.StateFlow
import sh.delo.perth.core.domain.model.Transcript
import sh.delo.perth.core.result.AppResult

/** Repository for voice capture and speech recognition. */
interface VoiceRepository {

    /** Whether the microphone permission has been granted. */
    val hasAudioPermission: StateFlow<Boolean>

    /** Whether a speech recognition provider is available. */
    val isRecognitionAvailable: StateFlow<Boolean>

    /**
     * Records audio from the microphone and returns a transcript.
     * Caller is responsible for requesting RECORD_AUDIO permission before calling this.
     */
    suspend fun transcribe(): AppResult<Transcript>

    /** Updates the cached audio permission state. */
    fun onPermissionResult(granted: Boolean)
}
