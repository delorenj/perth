package sh.delo.perth.feature.voice.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import sh.delo.perth.core.domain.model.Transcript
import sh.delo.perth.core.domain.repository.VoiceRepository
import sh.delo.perth.core.network.SpeechRecognizer
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Production implementation of [VoiceRepository].
 *
 * Uses [primaryRecognizer] (Android on-device) as the first choice and falls back to
 * [fallbackRecognizer] (Whisper) if the primary reports unavailable or returns an error.
 * Audio never leaves this class in persisted form.
 */
@Singleton
class VoiceRepositoryImpl @Inject constructor(
    @Named("primary") private val primaryRecognizer: SpeechRecognizer,
    @Named("fallback") private val fallbackRecognizer: SpeechRecognizer,
) : VoiceRepository {

    private val _hasAudioPermission = MutableStateFlow(false)
    override val hasAudioPermission: StateFlow<Boolean> = _hasAudioPermission.asStateFlow()

    override val isRecognitionAvailable: StateFlow<Boolean>
        get() = primaryRecognizer.isAvailable

    override suspend fun transcribe(): AppResult<Transcript> {
        if (!_hasAudioPermission.value) {
            return AppResult.Error(
                AppException.Voice("RECORD_AUDIO permission has not been granted")
            )
        }

        return if (primaryRecognizer.isAvailable.value) {
            val result = primaryRecognizer.recognize()
            if (result.isError) {
                Timber.w(
                    result.exceptionOrNull(),
                    "Primary recognizer failed; attempting fallback"
                )
                fallbackRecognizer.recognize()
            } else {
                result
            }
        } else {
            Timber.d("Primary recognizer unavailable; using fallback")
            fallbackRecognizer.recognize()
        }
    }

    override fun onPermissionResult(granted: Boolean) {
        _hasAudioPermission.value = granted
    }
}
