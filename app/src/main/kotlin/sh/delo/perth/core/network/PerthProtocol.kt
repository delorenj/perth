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

    @Serializable
    @SerialName("error")
    data class Error(val message: String) : PerthMessage()
}

@Serializable
data class ZellijSessionInfo(
    val name: String,
    val is_current: Boolean,
)
