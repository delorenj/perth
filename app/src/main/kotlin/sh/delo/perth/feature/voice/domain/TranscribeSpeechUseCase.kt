package sh.delo.perth.feature.voice.domain

import sh.delo.perth.core.domain.model.Transcript
import sh.delo.perth.core.domain.repository.VoiceRepository
import sh.delo.perth.core.result.AppResult
import javax.inject.Inject

/**
 * Sends captured audio through the configured speech recognizer and returns a verbatim [Transcript].
 *
 * This use case wraps [VoiceRepository.transcribe] so the ViewModel stays decoupled from
 * the recognition provider. The result is always verbatim — no LLM interpretation is applied here.
 */
class TranscribeSpeechUseCase @Inject constructor(
    private val voiceRepository: VoiceRepository,
) {
    suspend operator fun invoke(): AppResult<Transcript> = voiceRepository.transcribe()
}
