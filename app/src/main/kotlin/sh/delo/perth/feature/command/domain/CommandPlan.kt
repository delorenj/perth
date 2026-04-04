package sh.delo.perth.feature.command.domain

/**
 * A single step in a command plan returned by the LLM.
 *
 * @param description Human-readable description of what the step does.
 * @param command The shell command string to execute.
 * @param safetyClassification Risk level as classified by [CommandSafetyGate].
 * @param isApproved Whether the user has approved this step for execution. Starts as false.
 */
data class CommandStep(
    val description: String,
    val command: String,
    val safetyClassification: SafetyClassification,
    val isApproved: Boolean = false,
)

/**
 * A structured execution plan produced by the LLM from a voice transcript.
 *
 * @param originalTranscript The raw voice transcript that produced this plan.
 * @param steps The ordered list of steps to execute.
 */
data class CommandPlan(
    val originalTranscript: String,
    val steps: List<CommandStep>,
) {
    /** Returns a copy of this plan with all steps marked as approved. */
    fun approveAll(): CommandPlan = copy(steps = steps.map { it.copy(isApproved = true) })

    /** Returns a copy of this plan with all steps marked as rejected. */
    fun rejectAll(): CommandPlan = copy(steps = steps.map { it.copy(isApproved = false) })

    /** Returns a copy of this plan with the step at [index] toggled. */
    fun toggleStep(index: Int): CommandPlan {
        val updated = steps.toMutableList()
        if (index in updated.indices) {
            updated[index] = updated[index].let { it.copy(isApproved = !it.isApproved) }
        }
        return copy(steps = updated)
    }

    /** The subset of steps that have been explicitly approved. */
    val approvedSteps: List<CommandStep> get() = steps.filter { it.isApproved }

    /** True when at least one step is approved and ready to execute. */
    val hasApprovedSteps: Boolean get() = approvedSteps.isNotEmpty()

    /** True when any step is classified as [SafetyClassification.Destructive]. */
    val hasDestructiveSteps: Boolean
        get() = steps.any { it.safetyClassification == SafetyClassification.Destructive }
}
