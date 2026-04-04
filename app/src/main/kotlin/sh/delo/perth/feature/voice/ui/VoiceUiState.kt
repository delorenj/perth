package sh.delo.perth.feature.voice.ui

import sh.delo.perth.core.result.AppException
import sh.delo.perth.feature.voice.domain.VoiceMode

/** Represents every possible state of the voice control panel. */
data class VoiceUiState(
    /** The currently selected voice interaction mode. */
    val selectedMode: VoiceMode = VoiceMode.Transcription,

    /** Current capture/recognition lifecycle stage. */
    val captureState: CaptureState = CaptureState.Idle,

    /**
     * The transcription text shown in the preview area.
     * Non-null only when [captureState] is [CaptureState.ReadyToSend].
     */
    val transcription: String? = null,

    /**
     * The user-edited version of [transcription] shown in the editable field.
     * Starts equal to [transcription] and tracks live edits before sending.
     */
    val editedTranscription: String = "",

    /** Non-null when an error needs to be surfaced to the user. */
    val error: AppException? = null,

    // -------------------------------------------------------------------------
    // Task mode state (Story 4.1)
    // -------------------------------------------------------------------------

    /**
     * True while the "Write this to task.md?" confirmation dialog should be visible.
     * Reset to false once the user confirms or cancels.
     */
    val taskWriteConfirmationPending: Boolean = false,

    /**
     * True when task.md was written successfully. Used to show a brief success
     * snackbar/banner. Reset to false via [VoiceViewModel.onDismissTaskWriteSuccess].
     */
    val taskWriteSuccess: Boolean = false,

    // -------------------------------------------------------------------------
    // One-shot navigation / side-effect flags (Story 6.1, 6.2)
    // -------------------------------------------------------------------------

    /**
     * True when the user tapped "Open Settings" from the error banner.
     * The UI observes this and opens Android app settings, then calls
     * [VoiceViewModel.onSettingsIntentConsumed] to clear the flag.
     */
    val pendingSettingsIntent: Boolean = false,

    /**
     * True when the user tapped "Reconnect" from the error banner.
     * The UI observes this and triggers a transport reconnect, then calls
     * [VoiceViewModel.onReconnectConsumed].
     */
    val reconnectRequested: Boolean = false,

    /**
     * True when the user chose "Type Instead" from the error banner.
     * The UI uses this to focus the manual input field.
     */
    val typeInsteadRequested: Boolean = false,
) {
    /** Whether the microphone button should respond to taps. */
    val canRecord: Boolean
        get() = captureState == CaptureState.Idle

    /** Whether the send/cancel row should be visible. */
    val showSendPanel: Boolean
        get() = captureState == CaptureState.ReadyToSend

    /** Whether the pulsing recording indicator should be shown. */
    val isRecording: Boolean
        get() = captureState == CaptureState.Recording

    /** Whether the processing spinner should be shown. */
    val isProcessing: Boolean
        get() = captureState == CaptureState.Processing

    /** Whether task mode confirmation row should be shown instead of generic send. */
    val showTaskConfirmRow: Boolean
        get() = selectedMode == VoiceMode.Task && captureState == CaptureState.ReadyToSend
}

/** Lifecycle stages of a voice capture round-trip. */
enum class CaptureState {
    /** No capture in progress. Microphone button is available. */
    Idle,

    /** Microphone is open; audio is streaming to the recognizer. */
    Recording,

    /** Recording has stopped; recognition request is in flight. */
    Processing,

    /** Recognition returned a result; awaiting user Send or Cancel action. */
    ReadyToSend,
}
