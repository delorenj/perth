package sh.delo.perth.feature.voice.domain

import sh.delo.perth.core.domain.model.Transcript
import sh.delo.perth.core.domain.repository.VoiceRepository
import sh.delo.perth.core.result.AppResult
import javax.inject.Inject

/**
 * Triggers speech recognition via the [VoiceRepository].
 *
 * The caller must ensure RECORD_AUDIO permission is granted before invoking [invoke].
 * Audio is captured and transcribed in one step; no audio is persisted to disk.
 */
class CaptureVoiceUseCase @Inject constructor(
    private val voiceRepository: VoiceRepository,
) {
    suspend operator fun invoke(): AppResult<Transcript> = voiceRepository.transcribe()
}
