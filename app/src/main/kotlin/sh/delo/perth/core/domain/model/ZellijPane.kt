package sh.delo.perth.core.domain.model

/** Represents a single pane within a Zellij tab. */
data class ZellijPane(
    val id: PaneId,
    val title: String,
    val isActive: Boolean,
)
