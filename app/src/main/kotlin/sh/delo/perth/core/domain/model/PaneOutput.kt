package sh.delo.perth.core.domain.model

import java.time.Instant

/** A chunk of terminal output from a specific pane. */
data class PaneOutput(
    val paneId: PaneId,
    val text: String,
    val timestamp: Instant = Instant.now(),
)
