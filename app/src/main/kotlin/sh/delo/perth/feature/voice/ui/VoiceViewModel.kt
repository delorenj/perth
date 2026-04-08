package sh.delo.perth.feature.voice.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.domain.repository.SettingsRepository
import sh.delo.perth.core.domain.repository.VoiceRepository
import sh.delo.perth.core.network.ZellijTransport
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import sh.delo.perth.core.ui.RecoveryAction
import sh.delo.perth.feature.voice.domain.CaptureVoiceUseCase
import sh.delo.perth.feature.voice.domain.VoiceMode
import sh.delo.perth.feature.voice.domain.WriteTaskUseCase
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val captureVoiceUseCase: CaptureVoiceUseCase,
    private val voiceRepository: VoiceRepository,
    private val settingsRepository: SettingsRepository,
    private val transport: ZellijTransport,
    private val writeTaskUseCase: WriteTaskUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(VoiceUiState())
    val state: StateFlow<VoiceUiState> = _state.asStateFlow()

    init {
        observeVoiceMode()
    }

    // -------------------------------------------------------------------------
    // Permission
    // -------------------------------------------------------------------------

    /** Called by the UI after the system permission dialog resolves. */
    fun onPermissionResult(granted: Boolean) {
        voiceRepository.onPermissionResult(granted)
        if (!granted) {
            _state.update {
                it.copy(
                    error = AppException.Voice(
                        "Microphone permission is required for voice capture"
                    )
                )
            }
        }
    }

    // -------------------------------------------------------------------------
    // Recording controls
    // -------------------------------------------------------------------------

    /**
     * Starts voice capture. The caller (UI layer) must have already obtained
     * RECORD_AUDIO permission before invoking this.
     */
    fun startRecording() {
        if (_state.value.captureState != CaptureState.Idle) return

        _state.update { it.copy(captureState = CaptureState.Recording, error = null) }

        viewModelScope.launch {
            // Move to Processing immediately so the UI stops showing the pulsing
            // indicator as soon as we call the recognizer.
            _state.update { it.copy(captureState = CaptureState.Processing) }

            when (val result = captureVoiceUseCase()) {
                is AppResult.Success -> {
                    val text = result.data.text
                    _state.update {
                        it.copy(
                            captureState = CaptureState.ReadyToSend,
                            transcription = text,
                            editedTranscription = text,
                        )
                    }
                }
                is AppResult.Error -> {
                    Timber.e(result.exception, "Voice capture failed")
                    _state.update {
                        it.copy(
                            captureState = CaptureState.Idle,
                            error = result.exception,
                        )
                    }
                }
            }
        }
    }

    /** Cancels any in-flight recording and resets to Idle. */
    fun stopRecording() {
        // Cancelling the coroutine triggers suspendCancellableCoroutine cleanup inside
        // AndroidSpeechRecognizer. Here we just snap state back to Idle if still Recording.
        if (_state.value.captureState == CaptureState.Recording) {
            _state.update { it.copy(captureState = CaptureState.Idle) }
        }
    }

    // -------------------------------------------------------------------------
    // Transcription preview
    // -------------------------------------------------------------------------

    /** Updates the editable transcription text as the user types in the preview field. */
    fun onTranscriptionEdited(text: String) {
        _state.update { it.copy(editedTranscription = text) }
    }

    /**
     * Sends the (possibly edited) transcription to the active pane and resets state.
     *
     * [activePaneId] must be the currently focused Zellij pane. If null, an error is shown.
     */
    fun onSend(activePaneId: PaneId?, onCommandTranscript: (String) -> Unit = {}) {
        val text = _state.value.editedTranscription.trim()
        if (text.isBlank()) {
            onCancel()
            return
        }

        if (_state.value.selectedMode == VoiceMode.Command) {
            onCommandTranscript(text)
            resetToIdle()
            return
        }

        if (activePaneId == null) {
            _state.update {
                it.copy(error = AppException.Voice("No active pane selected"))
            }
            return
        }

        viewModelScope.launch {
            when (val result = transport.sendInput(activePaneId, text)) {
                is AppResult.Success -> resetToIdle()
                is AppResult.Error -> {
                    Timber.e(result.exception, "Failed to send transcription to pane %s", activePaneId)
                    _state.update { it.copy(error = result.exception) }
                }
            }
        }
    }

    /** Retries the last send operation using the same edited transcription. */
    fun onRetrySend(activePaneId: PaneId?, onCommandTranscript: (String) -> Unit = {}) {
        onSend(activePaneId, onCommandTranscript)
    }

    /** Discards the transcription and returns to Idle. */
    fun onCancel() {
        resetToIdle()
    }

    /** Clears the current error without changing capture state. */
    fun onDismissError() {
        _state.update { it.copy(error = null) }
    }

    // -------------------------------------------------------------------------
    // Mode selection
    // -------------------------------------------------------------------------

    /** Switches the active voice mode. If recording is in progress, it is stopped first. */
    fun onModeSelected(mode: VoiceMode) {
        if (_state.value.captureState == CaptureState.Recording) {
            stopRecording()
        }
        _state.update { it.copy(selectedMode = mode) }
        viewModelScope.launch {
            settingsRepository.saveVoiceMode(mode.toSettingsVoiceMode())
        }
    }

    // -------------------------------------------------------------------------
    // Task mode (Story 4.1)
    // -------------------------------------------------------------------------

    /**
     * Called when the user taps the task-mode confirm button.
     * Shows the confirmation dialog by setting [VoiceUiState.taskWriteConfirmationPending].
     * The UI is responsible for detecting this flag and displaying the dialog, then
     * calling [onConfirmWriteTask] or [onCancelWriteTask].
     */
    fun onRequestWriteTask() {
        if (_state.value.editedTranscription.isBlank()) return
        _state.update { it.copy(taskWriteConfirmationPending = true) }
    }

    /**
     * Executes the task.md write after the user confirmed the dialog.
     *
     * @param activePaneId The pane that should receive the shell write command.
     * @param append True when the user chose "Append" rather than the default overwrite.
     */
    fun onConfirmWriteTask(activePaneId: PaneId?, append: Boolean = false) {
        val text = _state.value.editedTranscription
        if (text.isBlank() || activePaneId == null) {
            _state.update {
                it.copy(
                    taskWriteConfirmationPending = false,
                    error = AppException.Command("No active pane or empty transcription"),
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update {
                it.copy(
                    taskWriteConfirmationPending = false,
                    captureState = CaptureState.Processing,
                    error = null,
                )
            }

            when (val result = writeTaskUseCase(activePaneId, text, append)) {
                is AppResult.Success -> {
                    Timber.d(
                        "VoiceViewModel: task.md written pane=%s append=%b",
                        activePaneId,
                        append,
                    )
                    _state.update {
                        it.copy(
                            captureState = CaptureState.Idle,
                            transcription = null,
                            editedTranscription = "",
                            taskWriteSuccess = true,
                        )
                    }
                }
                is AppResult.Error -> {
                    Timber.w(
                        result.exception,
                        "VoiceViewModel: writeTaskUseCase failed pane=%s",
                        activePaneId,
                    )
                    // Return to ReadyToSend so the user can retry or edit.
                    _state.update {
                        it.copy(
                            captureState = CaptureState.ReadyToSend,
                            error = result.exception,
                        )
                    }
                }
            }
        }
    }

    /** Called when the user dismisses the task write confirmation dialog without confirming. */
    fun onCancelWriteTask() {
        _state.update { it.copy(taskWriteConfirmationPending = false) }
    }

    /** Called by the UI after showing the task-write success snackbar/banner. */
    fun onDismissTaskWriteSuccess() {
        _state.update { it.copy(taskWriteSuccess = false) }
    }

    // -------------------------------------------------------------------------
    // Error recovery (Story 6.1, 6.2)
    // -------------------------------------------------------------------------

    /**
     * Routes a [RecoveryAction] from the [sh.delo.perth.core.ui.ErrorBanner].
     *
     * Actions that need Android context (e.g. [RecoveryAction.OpenSettings]) are
     * signalled via one-shot flags in [VoiceUiState] so the UI can handle them
     * without the ViewModel holding a Context reference.
     */
    fun onRecoveryAction(action: RecoveryAction) {
        when (action) {
            RecoveryAction.Retry -> {
                _state.update { it.copy(error = null) }
                startRecording()
            }
            RecoveryAction.OpenSettings -> {
                _state.update {
                    it.copy(
                        error = null,
                        captureState = CaptureState.Idle,
                        pendingSettingsIntent = true,
                    )
                }
            }
            RecoveryAction.TypeInstead -> {
                _state.update {
                    it.copy(
                        error = null,
                        captureState = CaptureState.ReadyToSend,
                        typeInsteadRequested = true,
                    )
                }
            }
            RecoveryAction.Dismiss -> {
                _state.update {
                    it.copy(
                        error = null,
                        captureState = CaptureState.Idle,
                    )
                }
            }
            RecoveryAction.Reconnect -> {
                _state.update {
                    it.copy(error = null, reconnectRequested = true)
                }
            }
            RecoveryAction.CheckApiKey -> {
                _state.update {
                    it.copy(
                        error = null,
                        captureState = CaptureState.Idle,
                        pendingSettingsIntent = true,
                    )
                }
            }
        }
    }

    /** Called by the UI after it has consumed the settings Intent. */
    fun onSettingsIntentConsumed() {
        _state.update { it.copy(pendingSettingsIntent = false) }
    }

    /** Called by the UI after it has initiated a reconnect. */
    fun onReconnectConsumed() {
        _state.update { it.copy(reconnectRequested = false) }
    }

    /** Called by the UI after it has focused the manual text input. */
    fun onTypeInsteadConsumed() {
        _state.update { it.copy(typeInsteadRequested = false) }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private fun observeVoiceMode() {
        settingsRepository.voiceModeFlow()
            .onEach { settingsMode ->
                val featureMode = VoiceMode.fromSettingsVoiceMode(settingsMode)
                _state.update { it.copy(selectedMode = featureMode) }
            }
            .launchIn(viewModelScope)
    }

    private fun resetToIdle() {
        _state.update {
            it.copy(
                captureState = CaptureState.Idle,
                transcription = null,
                editedTranscription = "",
                error = null,
            )
        }
    }
}
