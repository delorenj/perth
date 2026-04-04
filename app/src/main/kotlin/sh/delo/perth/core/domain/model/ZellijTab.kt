package sh.delo.perth.core.domain.model

/** Represents a tab within a Zellij session, containing one or more panes. */
data class ZellijTab(
    val id: String,
    val name: String,
    val panes: List<ZellijPane>,
    val isActive: Boolean,
) {
    val activePane: ZellijPane? get() = panes.firstOrNull { it.isActive } ?: panes.firstOrNull()
}
