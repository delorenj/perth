package sh.delo.perth.core.domain.model

import java.time.Instant

/** Represents a top-level Zellij session. */
data class ZellijSession(
    val id: String,
    val name: String,
    val tabs: List<ZellijTab>,
    val createdAt: Instant,
) {
    val activeTab: ZellijTab? get() = tabs.firstOrNull { it.isActive } ?: tabs.firstOrNull()
    val tabCount: Int get() = tabs.size
}
