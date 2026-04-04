package sh.delo.perth.feature.terminal.domain

import sh.delo.perth.core.domain.model.PaneId
import sh.delo.perth.core.domain.model.ZellijTab
import sh.delo.perth.core.result.AppException
import sh.delo.perth.core.result.AppResult
import javax.inject.Inject

/**
 * Resolves which pane should be active after a navigation event.
 *
 * Rules (in priority order):
 * 1. If [requestedPaneId] is non-null and exists in [tab], use it.
 * 2. Otherwise use the pane already marked active by the server.
 * 3. Otherwise fall back to the first pane in the tab.
 * 4. If the tab has no panes, return Error.
 */
class NavigatePaneUseCase @Inject constructor() {

    operator fun invoke(tab: ZellijTab, requestedPaneId: PaneId? = null): AppResult<PaneId> {
        val panes = tab.panes
        if (panes.isEmpty()) {
            return AppResult.Error(
                AppException.Command("Tab '${tab.name}' has no panes"),
            )
        }

        val resolved = when {
            requestedPaneId != null && panes.any { it.id == requestedPaneId } -> requestedPaneId
            else -> tab.activePane?.id ?: panes.first().id
        }
        return AppResult.Success(resolved)
    }
}
