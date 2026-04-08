package sh.delo.perth.core.ui

/**
 * Represents an action a user can take to recover from an error.
 *
 * Each subclass maps to a distinct UI affordance shown alongside the error message.
 * The [label] is displayed as button text; the semantic type drives which handler
 * the ErrorBanner invokes via its [onAction] callback.
 */
sealed class RecoveryAction(val label: String) {

    /** Retry the failed operation immediately. */
    object Retry : RecoveryAction("Retry")

    /** Open Android Settings so the user can manage app permissions. */
    object OpenSettings : RecoveryAction("Open Settings")

    /** Dismiss the error and allow the user to type a command manually. */
    object TypeInstead : RecoveryAction("Type Instead")

    /** Dismiss the error and close the current voice session. */
    object Dismiss : RecoveryAction("Dismiss")

    /** Initiate a reconnect to the zellij server. */
    object Reconnect : RecoveryAction("Reconnect")

    /** Check or update API key in Settings. */
    object CheckApiKey : RecoveryAction("Check API Key")
}
