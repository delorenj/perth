package sh.delo.perth.core.domain.model

/** Represents the current connection state to the zellij server. */
enum class ConnectionState {
    /** Successfully connected and receiving data. */
    Connected,

    /** Connection attempt is in progress. */
    Connecting,

    /** No active connection. */
    Disconnected,

    /** Connection failed or was lost with an error. */
    Error,
}
