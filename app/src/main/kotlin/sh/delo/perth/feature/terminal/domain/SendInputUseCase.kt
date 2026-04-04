package sh.delo.perth.feature.terminal.domain

import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.network.ZealotTransport
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import javax.inject.Inject

/**
 * Validates and sends raw keyboard input to the specified pane via [ZealotTransport].
 *
 * Returns [AppResult.Error] immediately if no pane is active or the input is blank.
 */
class SendInputUseCase @Inject constructor(
    private val transport: ZealotTransport,
) {

    suspend operator fun invoke(paneId: PaneId?, input: String): AppResult<Unit> {
        if (paneId == null) {
            return AppResult.Error(
                AppException.Command("No active pane to send input to"),
            )
        }
        if (input.isBlank()) {
            return AppResult.Error(
                AppException.Command("Input must not be blank"),
            )
        }
        return transport.sendInput(paneId, input)
    }
}
