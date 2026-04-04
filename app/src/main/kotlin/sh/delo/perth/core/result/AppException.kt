package sh.delo.perth.core.result

/** Typed exceptions for all application error domains. */
sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** A network-level failure (unreachable host, TLS error, DNS failure). */
    class Network(message: String, cause: Throwable? = null) : AppException(message, cause)

    /** A network request that exceeded its allowed time budget. */
    class Timeout(message: String, cause: Throwable? = null) : AppException(message, cause)

    /** The server returned an error response. */
    class Server(val code: Int, message: String) : AppException(message)

    /**
     * The server rejected credentials or an API key was missing / invalid.
     * Distinct from [Server] (HTTP 401/403) so the UI can offer a targeted
     * "Check API Key" recovery action rather than a generic retry.
     */
    class Authentication(message: String, cause: Throwable? = null) : AppException(message, cause)

    /** A voice capture or speech-recognition failure. */
    class Voice(message: String, cause: Throwable? = null) : AppException(message, cause)

    /** Command interpretation or execution failure. */
    class Command(message: String, cause: Throwable? = null) : AppException(message, cause)

    /** A local storage (Room / DataStore / EncryptedSharedPreferences) failure. */
    class Storage(message: String, cause: Throwable? = null) : AppException(message, cause)
}
