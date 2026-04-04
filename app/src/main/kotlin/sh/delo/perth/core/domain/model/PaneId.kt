package sh.delo.perth.core.domain.model

/** Strongly-typed identifier for a Zellij pane. */
@JvmInline
value class PaneId(val value: String) {
    override fun toString(): String = value
}
