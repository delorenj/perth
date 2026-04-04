package sh.delo.perth.feature.command.domain

/** Risk classification for a terminal command. */
enum class SafetyClassification {
    /** Command is safe to run without a warning. */
    Safe,

    /** Command may have side-effects; user should review before approving. */
    Caution,

    /** Command is potentially destructive; explicit confirmation required. */
    Destructive,
}
