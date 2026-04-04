package sh.delo.perth.feature.session.ui

import sh.delo.perth.core.domain.model.ConnectionState
import sh.delo.perth.core.domain.model.ZellijSession
import sh.delo.perth.core.result.AppException

data class SessionListUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val sessions: List<ZellijSession> = emptyList(),
    val connectionState: ConnectionState = ConnectionState.Disconnected,
    val error: AppException? = null,
) {
    val isEmpty: Boolean get() = !isLoading && !isRefreshing && sessions.isEmpty() && error == null
}
