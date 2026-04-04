package sh.delo.perth.feature.command.domain

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Classifies shell commands by risk level using pattern matching.
 *
 * This is a pure domain class with no Android dependencies.
 * Every command must pass through this gate before it is shown to the user
 * or executed. The gate is NON-NEGOTIABLE per the architecture contract.
 */
@Singleton
class CommandSafetyGate @Inject constructor() {

    /**
     * Classifies a single [command] string into a [SafetyClassification].
     *
     * Matching is case-insensitive and checks the full command string so that
     * piped or chained commands are still caught (e.g. `echo foo | rm -rf /`).
     */
    fun classify(command: String): SafetyClassification {
        val normalized = command.trim().lowercase()
        return when {
            DESTRUCTIVE_PATTERNS.any { it.containsMatchIn(normalized) } -> SafetyClassification.Destructive
            CAUTION_PATTERNS.any { it.containsMatchIn(normalized) } -> SafetyClassification.Caution
            else -> SafetyClassification.Safe
        }
    }

    /**
     * Applies [classify] to every step in [plan] and returns a new plan with
     * updated safety classifications.
     */
    fun applyTo(plan: CommandPlan): CommandPlan {
        val classified = plan.steps.map { step ->
            step.copy(safetyClassification = classify(step.command))
        }
        return plan.copy(steps = classified)
    }

    companion object {
        /**
         * Patterns that indicate a command is outright destructive.
         * Any match results in [SafetyClassification.Destructive].
         */
        private val DESTRUCTIVE_PATTERNS: List<Regex> = listOf(
            // rm with force/recursive flags
            Regex("""rm\s+.*-[a-z]*[rf]"""),
            Regex("""rm\s+.*--force"""),
            Regex("""rm\s+.*--recursive"""),
            // Disk/partition operations
            Regex("""mkfs(\.\w+)?"""),
            Regex("""format\s"""),
            Regex("""fdisk\s"""),
            Regex("""parted\s"""),
            Regex("""dd\s+.*of="""),
            // Database destructive operations
            Regex("""drop\s+(table|database|schema|index)\s"""),
            Regex("""truncate\s+(table\s+)?\w"""),
            Regex("""delete\s+from\s"""),
            // Process termination
            Regex("""kill\s+(-9|-SIGKILL|-KILL)"""),
            Regex("""killall\s"""),
            Regex("""pkill\s"""),
            // Privilege escalation destroying data
            Regex("""sudo\s+rm\s"""),
            Regex("""sudo\s+mkfs"""),
            Regex("""sudo\s+dd\s"""),
            // Overwrite with redirect to root paths or system dirs
            Regex(""">\s*/(dev|sys|proc|etc)/"""),
            // Shutdown/reboot
            Regex("""(shutdown|reboot|halt|poweroff)(\s|$)"""),
            // Wipe history
            Regex("""history\s+-c"""),
            // Git destructive
            Regex("""git\s+(push\s+.*--force|reset\s+--hard|clean\s+-[a-z]*f)"""),
        )

        /**
         * Patterns that indicate a command deserves a caution warning but is
         * not outright destructive. Any match results in [SafetyClassification.Caution].
         */
        private val CAUTION_PATTERNS: List<Regex> = listOf(
            // rm without force flags (still a deletion)
            Regex("""^rm\s"""),
            // Moving files
            Regex("""^mv\s"""),
            // chmod/chown on recursive paths
            Regex("""(chmod|chown)\s+.*-[rR]"""),
            // Overwrite redirection
            Regex(""">\s*\S+"""),
            // Package installation
            Regex("""(apt|apt-get|yum|dnf|brew|pip|npm|yarn|cargo)\s+install"""),
            Regex("""(apt|apt-get|yum|dnf)\s+(remove|purge|autoremove)"""),
            // sudo in general
            Regex("""^sudo\s"""),
            // Systemctl state changes
            Regex("""systemctl\s+(start|stop|restart|enable|disable|mask)"""),
            // Environment variable overwrite
            Regex("""export\s+\w+="""),
            // Curl/wget pipe to shell (code execution)
            Regex("""(curl|wget)\s+.*\|\s*(bash|sh|zsh|fish)"""),
            // git operations that alter remote state
            Regex("""git\s+(push|rebase|merge|cherry-pick)"""),
            // Docker destructive-ish
            Regex("""docker\s+(rm|rmi|system\s+prune|volume\s+prune)"""),
            // kubectl delete
            Regex("""kubectl\s+delete"""),
            // Database operations that modify state
            Regex("""(insert|update|alter)\s+(into\s+|table\s+)?\w"""),
        )
    }
}
