package sh.delo.perth.core.ui

import sh.delo.perth.core.result.AppException

/**
 * Maps [AppException] subclasses to user-friendly messages and a prioritised list of
 * [RecoveryAction]s. No raw stack traces or internal details ever reach this layer.
 *
 * Design rules:
 * - Messages are written in plain language, never developer jargon.
 * - Every mapping provides at least one recovery action.
 * - [RecoveryAction.Retry] is always present so the user is never stuck.
 */
object ErrorMapper {

    data class ErrorPresentation(
        val message: String,
        val recoveryActions: List<RecoveryAction>,
    )

    fun map(exception: AppException): ErrorPresentation = when (exception) {
        is AppException.Network -> mapNetwork(exception)
        is AppException.Server -> mapServer(exception)
        is AppException.Voice -> mapVoice(exception)
        is AppException.Command -> mapCommand(exception)
        is AppException.Storage -> mapStorage(exception)
        is AppException.Authentication -> mapAuthentication(exception)
        is AppException.Timeout -> mapTimeout(exception)
    }

    // -------------------------------------------------------------------------
    // Per-type mappings
    // -------------------------------------------------------------------------

    private fun mapNetwork(e: AppException.Network): ErrorPresentation {
        val message = buildString {
            append("Network error: unable to reach the server.")
            if (e.message?.isNotBlank() == true) append(" (${e.message})")
        }
        return ErrorPresentation(
            message = message,
            recoveryActions = listOf(RecoveryAction.Retry, RecoveryAction.Reconnect),
        )
    }

    private fun mapServer(e: AppException.Server): ErrorPresentation {
        val message = when (e.code) {
            in 500..599 -> "Server error (${e.code}). The zellij server returned an unexpected response."
            in 400..499 -> "Request error (${e.code}). The server rejected the last command."
            else -> "Server error (${e.code}). Try reconnecting."
        }
        return ErrorPresentation(
            message = message,
            recoveryActions = listOf(RecoveryAction.Retry, RecoveryAction.Reconnect),
        )
    }

    private fun mapVoice(e: AppException.Voice): ErrorPresentation {
        val isMicUnavailable = e.message?.contains("permission", ignoreCase = true) == true
            || e.message?.contains("microphone", ignoreCase = true) == true
            || e.message?.contains("unavailable", ignoreCase = true) == true

        return if (isMicUnavailable) {
            ErrorPresentation(
                message = "Microphone not available. Check permissions.",
                // Retry exists as a last resort per the "always available" guarantee
                // in Story 6.1, but it is intentionally last because retrying without
                // granting the permission reproduces the failure.
                recoveryActions = listOf(
                    RecoveryAction.OpenSettings,
                    RecoveryAction.TypeInstead,
                    RecoveryAction.Retry,
                    RecoveryAction.Dismiss,
                ),
            )
        } else {
            ErrorPresentation(
                message = "Could not transcribe audio. Tap to retry or type instead.",
                recoveryActions = listOf(
                    RecoveryAction.Retry,
                    RecoveryAction.TypeInstead,
                    RecoveryAction.Dismiss,
                ),
            )
        }
    }

    private fun mapCommand(e: AppException.Command): ErrorPresentation {
        val isLlm = e.message?.contains("LLM", ignoreCase = true) == true
            || e.message?.contains("interpretation", ignoreCase = true) == true
            || e.message?.contains("api key", ignoreCase = true) == true

        return if (isLlm) {
            ErrorPresentation(
                message = "Command interpretation failed. Check API key or retry.",
                recoveryActions = listOf(
                    RecoveryAction.CheckApiKey,
                    RecoveryAction.Retry,
                    RecoveryAction.TypeInstead,
                ),
            )
        } else {
            val detail = e.message?.takeIf { it.isNotBlank() } ?: "unknown error"
            ErrorPresentation(
                message = "Command failed: $detail. No further commands were executed.",
                recoveryActions = listOf(
                    RecoveryAction.Retry,
                    RecoveryAction.TypeInstead,
                    RecoveryAction.Dismiss,
                ),
            )
        }
    }

    private fun mapStorage(e: AppException.Storage): ErrorPresentation = ErrorPresentation(
        message = "A local storage error occurred. Some data may not have been saved.",
        recoveryActions = listOf(RecoveryAction.Retry, RecoveryAction.Dismiss),
    )

    private fun mapAuthentication(e: AppException.Authentication): ErrorPresentation = ErrorPresentation(
        message = "Authentication failed. Check your server credentials in Settings.",
        // Retry comes after CheckApiKey because retrying a known-bad credential just
        // reproduces the failure. CheckApiKey leads to Settings where the user can
        // fix the underlying problem; Retry is kept available so the user is never
        // stuck per the "Retry button always available" guarantee in Story 6.1.
        recoveryActions = listOf(
            RecoveryAction.CheckApiKey,
            RecoveryAction.Retry,
            RecoveryAction.Dismiss,
        ),
    )

    private fun mapTimeout(e: AppException.Timeout): ErrorPresentation = ErrorPresentation(
        message = "The request timed out. Check your connection and retry.",
        recoveryActions = listOf(RecoveryAction.Retry, RecoveryAction.Reconnect),
    )
}
