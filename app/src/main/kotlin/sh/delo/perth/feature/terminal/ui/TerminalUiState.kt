package sh.delo.perth.feature.terminal.ui

import sh.delo.perth.core.domain.model.ConnectionState
import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.domain.model.PaneOutput
import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.domain.model.ZellijTab
import sh.delo.perth.core.result.AppException

data class TerminalUiState(
    val isLoading: Boolean = true,
    val session: ZellijSession? = null,
    val activeTabIndex: Int = 0,
    val activePaneId: PaneId? = null,
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val paneOutput: Map<String, List<PaneOutput>> = emptyMap(),
    val error: AppException? = null,
    /**
     * Story 1.4: True when auto-reconnect attempts are in progress after a network drop.
     * Drives the disconnected banner in TerminalScreen.
     */
    val isReconnecting: Boolean = false,
    /**
     * Story 1.4: True when all auto-reconnect attempts have been exhausted.
     * Drives the manual Reconnect button.
     */
    val reconnectFailed: Boolean = false,
) {
    val activeTabs: List<ZellijTab> get() = session?.tabs ?: emptyList()
    val activeTab: ZellijTab? get() = activeTabs.getOrNull(activeTabIndex)
    val sessionName: String get() = session?.name ?: ""

    /**
     * True when the connection is not healthy — shows the disconnected banner.
     * The last-known pane output remains visible while this is true (Story 1.4 cache).
     */
    val showDisconnectedBanner: Boolean
        get() = connectionState == ConnectionState.Disconnected ||
            connectionState == ConnectionState.Error ||
            isReconnecting
}
