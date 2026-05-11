package sh.delo.perth.core.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class PerthMessage {
    @Serializable
    @SerialName("session_list")
    object SessionList : PerthMessage()

    @Serializable
    @SerialName("session_list_update")
    data class SessionListUpdate(val sessions: List<ZellijSessionInfo>) : PerthMessage()

    @Serializable
    @SerialName("session_attach")
    data class SessionAttach(val session_name: String) : PerthMessage()

    @Serializable
    @SerialName("pane_input")
    data class PaneInput(val pane_id: String, val input: String) : PerthMessage()

    @Serializable
    @SerialName("pane_command")
    data class PaneCommand(val pane_id: String, val command: String) : PerthMessage()

    @Serializable
    @SerialName("pane_output")
    data class PaneOutputMessage(val pane_id: String, val data: String) : PerthMessage()

    /**
     * Sent by the bridge after a `session_attach` (or whenever the plugin
     * reports a new state snapshot). Carries the live tab/pane tree for a
     * session, populating what the Kotlin client previously left empty.
     */
    @Serializable
    @SerialName("session_attached")
    data class SessionAttached(
        val session_name: String,
        val tabs: List<TabInfoWire>,
    ) : PerthMessage()

    @Serializable
    @SerialName("error")
    data class Error(val message: String) : PerthMessage()
}

@Serializable
data class ZellijSessionInfo(
    val name: String,
    val is_current: Boolean,
)

/**
 * Wire shape of a single tab in a [PerthMessage.SessionAttached] message.
 * Mirrors the `TabInfoWire` struct on the bridge side. Field names use snake
 * case to match the bridge's JSON output verbatim — kotlinx.serialization
 * keeps the JSON key the same as the Kotlin field unless overridden.
 */
@Serializable
data class TabInfoWire(
    val position: Int,
    val name: String,
    val is_active: Boolean,
    val active_pane_id: Int? = null,
    val panes: List<PaneInfoWire> = emptyList(),
)

@Serializable
data class PaneInfoWire(
    val id: Int,
    val title: String,
    val is_active: Boolean,
)
