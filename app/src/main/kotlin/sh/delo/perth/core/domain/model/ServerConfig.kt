package sh.delo.perth.core.domain.model

/** Connection configuration for a zealot server. */
data class ServerConfig(
    val url: String,
    val authToken: String? = null,
) {
    val wsUrl: String
        get() = url.replace("http://", "ws://").replace("https://", "wss://")

    val isValid: Boolean
        get() = url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))
}
