---
name: 'step-07-complete'
description: 'Post final audit comment and exit workflow; the Plane webhook already published the terminal move'

auditCommentTemplate: '../data/audit-comment-template.md'
eventSchemas: '../data/event-schemas.md'
---

# Step 7: Completion

## STEP GOAL:

To post the closing audit comment to Plane and exit the workflow. The terminal move (done or blocked) was already published as `bloodbank.repo.task.updated` by the Plane webhook when the previous step wrote it; this step emits nothing.

## MANDATORY EXECUTION RULES (READ FIRST):

### Universal Rules:

- Read the complete step file before taking any action.
- If any instruction references a subprocess, subagent, or tool you do not have access to, you MUST still achieve the outcome in your main context thread.

### Role Reinforcement:

- You are a workflow orchestrator performing final bookkeeping.
- This is the last step. Clean exit with full audit trail.

### Step-Specific Rules:

- Focus ONLY on completion bookkeeping.
- FORBIDDEN to modify ticket state beyond what was set in the previous step.
- Handle both "done" and "blocked" terminal states.

## EXECUTION PROTOCOLS:

- Post final audit comment summarizing the full lifecycle.
- Emit no Bloodbank event (the Plane webhook is the only producer of ticket facts).
- Exit cleanly.

## CONTEXT BOUNDARIES:

- Previous step set the terminal state (done or blocked).
- Focus: audit trail completion.
- This is the final step. No next step.

## MANDATORY SEQUENCE

**CRITICAL:** Follow this sequence exactly. Do not skip, reorder, or improvise.

### 1. Determine Terminal State

Check the ticket's current state (set by the previous step):
- **done**: All AC items verified, ticket complete.
- **blocked**: Max retries exceeded, AC ambiguity, or refinement failure.

### 2. Post Final Audit Summary

Post a summary audit comment using {auditCommentTemplate}. The ticket is already
in its terminal lane, so the same command posts only the comment (px sees the
lane is unchanged and skips the PATCH):
`px move {ticket_id} "{states.done}" -m "<summary>" --json` (or `"{states.blocked}"`).

**For "done" state:**
```
[TICKET-LIFECYCLE] Workflow Complete
---
terminal_state: done
timestamp: {ISO 8601}
agent: orchestrator
summary: Ticket processed through full lifecycle. All AC items verified.
lifecycle:
  started: {workflow_start_timestamp}
  completed: {now}
  states_visited: [{list of all states the ticket passed through}]
  retries_used: {qa_retry_count}
  agents_spawned: [{list: plane-captain (if used), coding-agent, qa-agent}]
---
```

**For "blocked" state:**
```
[TICKET-LIFECYCLE] Workflow Complete
---
terminal_state: blocked
timestamp: {ISO 8601}
agent: orchestrator
summary: Ticket blocked. Requires external intervention.
reason: {blocked_reason from previous step}
lifecycle:
  started: {workflow_start_timestamp}
  blocked_at: {now}
  states_visited: [{list of all states}]
  blocking_details: {details from previous step}
---
```

### 3. Emit Nothing

There is no terminal event to send. The previous step moved the ticket to
`done` or `blocked` with `px`, and the Plane webhook normalizer (n8n
`Plane → Bloodbank`) already published that move as
`bloodbank.repo.task.updated`. The summary comment you just posted reaches the
bus as `bloodbank.repo.task.appended`. Do not call `bb emit` for any
`repo.task.*` or `repo.board.*` type (see {eventSchemas}).

### 4. Exit Workflow

**Workflow complete.** No further steps.

Report final status:
- Ticket ID and title
- Terminal state (done or blocked)
- Total states visited
- Agents spawned
- Retries used (if any)

---

## SYSTEM SUCCESS/FAILURE METRICS

### SUCCESS:

- Final audit summary posted with full lifecycle details
- No `repo.task.*` event emitted by the workflow (the Plane webhook published the terminal move)
- Clean exit with status report
- Both "done" and "blocked" paths handled

### FAILURE:

- Exiting without posting final audit summary
- Emitting a terminal `bloodbank.repo.task.*` event yourself (a duplicate of the webhook's fact)
- Attempting further state transitions after completion
- Missing lifecycle details in summary

**Master Rule:** Clean exit. Full audit trail. No loose ends.
