package sh.delo.perth.core.domain.model

/** The result of speech-to-text recognition. */
data class Transcript(
    val text: String,
    val confidence: Float,
    val provider: SpeechProvider,
) {
    enum class SpeechProvider {
        MlKit,
        Whisper,
    }
}
