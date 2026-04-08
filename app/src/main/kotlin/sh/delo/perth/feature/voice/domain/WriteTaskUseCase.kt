package sh.delo.perth.feature.voice.domain

import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.network.ZellijTransport
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import sh.delo.perth.core.result.runCatchingAppResult
import timber.log.Timber
import javax.inject.Inject

/**
 * Writes transcription text to `task.md` in the active pane by sending a safe
 * heredoc shell command via [ZellijTransport.sendCommand].
 *
 * The heredoc form `cat > task.md << 'EOF' ... EOF` is used so that:
 * - Single quotes inside the text do not need escaping (the delimiter `'EOF'`
 *   disables all shell expansions inside the heredoc body).
 * - The text is written verbatim without any shell interpretation.
 *
 * The only character that cannot appear inside a quoted heredoc body is a line
 * that equals the delimiter exactly (`EOF` on its own line). [sanitize] replaces
 * any such line to prevent premature termination.
 *
 * @param append When true the command uses `>>` (append) instead of `>` (overwrite).
 */
class WriteTaskUseCase @Inject constructor(
    private val transport: ZellijTransport,
) {
    /**
     * Sends the write command to [paneId].
     *
     * @return [AppResult.Success] with the server's acknowledgement string, or
     *   [AppResult.Error] wrapping an [AppException.Command] on failure.
     */
    suspend operator fun invoke(
        paneId: PaneId,
        text: String,
        append: Boolean = false,
    ): AppResult<String> {
        Timber.d("WriteTaskUseCase: writing task.md to paneId=%s append=%b", paneId, append)

        val sanitisedText = sanitize(text)
        val redirectOp = if (append) ">>" else ">"
        val command = buildString {
            append("cat $redirectOp task.md << 'EOF'\n")
            append(sanitisedText)
            if (!sanitisedText.endsWith("\n")) append("\n")
            append("EOF")
        }

        return runCatchingAppResult(
            errorMapper = { throwable ->
                AppException.Command(
                    "Failed to write task.md: ${throwable.message}",
                    throwable,
                )
            },
        ) {
            val result = transport.sendCommand(paneId, command)
            when (result) {
                is AppResult.Success -> result.data
                is AppResult.Error -> throw result.exception
            }
        }
    }

    /**
     * Replaces any line that is exactly `EOF` with `EOF ` (trailing space) so the
     * heredoc delimiter is never accidentally triggered inside the body.
     */
    private fun sanitize(text: String): String =
        text.lines().joinToString("\n") { line ->
            if (line == HEREDOC_DELIMITER) "$HEREDOC_DELIMITER " else line
        }

    companion object {
        private const val HEREDOC_DELIMITER = "EOF"
    }
}
