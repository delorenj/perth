package sh.delo.perth.feature.voice.domain

import sh.delo.perth.core.domain.repository.SettingsRepository

/** The three voice interaction modes available in Perth. */
enum class VoiceMode {
    /** Verbatim speech-to-text transcription. */
    Transcription,

    /** Task-oriented voice input (future LLM interpretation). */
    Task,

    /** Command mode with safety confirmation gate. */
    Command,
    ;

    fun toSettingsVoiceMode(): SettingsRepository.VoiceMode = when (this) {
        Transcription -> SettingsRepository.VoiceMode.Transcription
        Task -> SettingsRepository.VoiceMode.Task
        Command -> SettingsRepository.VoiceMode.Command
    }

    companion object {
        fun fromSettingsVoiceMode(mode: SettingsRepository.VoiceMode): VoiceMode = when (mode) {
            SettingsRepository.VoiceMode.Transcription -> Transcription
            SettingsRepository.VoiceMode.Task -> Task
            SettingsRepository.VoiceMode.Command -> Command
        }
    }
}
