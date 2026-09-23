---
name: 'step-01-init'
description: 'Resolve project context, validate preconditions, and acquire ticket'

nextStepFile: './step-02-triage.md'
workflowConfig: '../workflow.yaml'
planeSkill: '~/.claude/skills/managing-tickets-and-tasks-in-plane/'
eventSchemas: '../data/event-schemas.md'
---

# Step 1: Initialize and Acquire Ticket

## STEP GOAL:

To resolve the project's Plane workspace context, validate all preconditions for autonomous execution, and acquire a ticket to process.

## MANDATORY EXECUTION RULES (READ FIRST):

### Universal Rules:

- **NEVER** proceed if preconditions fail. Exit with a clear error message.
- Read the complete step file before taking any action.
- When loading next step, ensure entire file is read.
- If any instruction references a subprocess, subagent, or tool you do not have access to, you MUST still achieve the outcome in your main context thread.

### Role Reinforcement:

- You are a workflow orchestrator. You read state, evaluate conditions, and route execution.
- You do not write code. You do not interact with humans.
- You bring systematic precondition validation and context resolution.

### Step-Specific Rules:

- Focus ONLY on context resolution and ticket acquisition.
- FORBIDDEN to evaluate AC or spawn agents in this step.
- Exit immediately with a clear error if any precondition fails.

## EXECUTION PROTOCOLS:

- Resolve project context from the `ticket_provider` block in `.project.json` + workspace config.
- Load Plane skill for API patterns.
- Load workflow config for state mapping and staleness durations.
- Acquire ticket (by ID if provided, or via scoring algorithm).

## CONTEXT BOUNDARIES:

- This is step 1. No prior context exists.
- Focus: project identification, workspace resolution, ticket selection.
- Dependencies: `.project.json` must exist and contain a `ticket_provider` block with a non-empty `board_id`, Plane API must be accessible.

## MANDATORY SEQUENCE

**CRITICAL:** Follow this sequence exactly. Do not skip, reorder, or improvise.

### 1. Resolve Project Context

Search for `.project.json` in the current project root.

**If found:**
- Read and parse `.project.json` and extract the `ticket_provider` block, reading `ticket_provider.workspace` and `ticket_provider.board_id`.
- Load `~/.claude/plane-workspaces.json` to resolve API endpoint and credentials for this workspace.

**If NOT found (or no `ticket_provider` block / empty `board_id`):**
- EXIT with error: "PRECONDITION FAILED: No .project.json with a ticket_provider block found in project root. This workflow requires a Plane-integrated project. Ensure .project.json contains a ticket_provider block with workspace and a non-empty board_id."

### 2. Validate Workspace Registration

Using `ticket_provider.workspace` from `.project.json`, verify it exists in `~/.claude/plane-workspaces.json`.

**If registered:**
- Extract API base URL, API key reference, and workspace ID.
- Confirm API connectivity with a lightweight health check (list projects).

**If NOT registered:**
- EXIT with error: "PRECONDITION FAILED: Workspace '{workspace_slug}' not found in ~/.claude/plane-workspaces.json. Register the workspace first."

### 3. Validate External Dependencies

Verify availability of:

1. **Ticket writer:** `px` (Pilot >= 0.2.0) is on PATH, `px whoami --json` resolves this repo's board, and `px --help` lists `move`. `px` is the one Plane writer; every state move in this workflow is a `px move` (see {eventSchemas}).

This workflow publishes no Bloodbank events, so it needs no `bb-emit`: ticket
facts come only from the Plane webhook normalizer (see {eventSchemas}).

**If any dependency is missing:**
- EXIT with error listing which dependencies are unavailable and how to resolve them.

### 4. Load Workflow Configuration

Read {workflowConfig} to load:
- AC sufficiency rubric criteria
- QA retry limits
- Staleness durations per state
- Plane state name mapping

Store these values for use in subsequent steps.

### 5. Acquire Ticket

**If a ticket ID was provided as input:**
- Fetch the ticket from Plane API using the project context.
- Verify the ticket exists and is in a processable state (backlog or ready).

**If NO ticket ID was provided:**
- Load {planeSkill} to access the ticket scoring algorithm.
- Query the Plane board for tickets in backlog or ready state.
- Score and select the highest-priority ticket.
- If no eligible tickets exist, EXIT with message: "No eligible tickets found in backlog or ready state."

### 6. Capture Ticket Context

Extract from the selected ticket:
- Ticket ID, title, description
- Current state/status
- Acceptance criteria field (raw, for evaluation in next step)
- Any linked functional requirements or labels
- Assignee and priority metadata

### 7. Transition to Triage

Audit comment; the move below posts it:
```
[TICKET-LIFECYCLE] State Transition
---
from: {current_state}
to: triage
timestamp: {ISO 8601}
agent: orchestrator
reason: Ticket acquired for lifecycle processing
---
```

Move the ticket to triage and post that comment with it: `px move {ticket_id} "{states.triage}" -m "<audit comment>" --json`.

Emit nothing. The Plane webhook normalizer (n8n `Plane → Bloodbank`) publishes
`bloodbank.repo.task.updated` for this move (see {eventSchemas}); a hand-written
copy from the workflow would be a duplicate fact.

**Proceeding to triage...**

Immediately load, read entire file, then execute {nextStepFile}.

---

## SYSTEM SUCCESS/FAILURE METRICS

### SUCCESS:

- Project context resolved from the `ticket_provider` block in `.project.json` + workspace config
- All preconditions validated (Plane API, `px`)
- Ticket acquired (by ID or scoring algorithm)
- Ticket context captured (ID, title, AC, status, metadata)
- Ticket transitioned to triage state with audit comment
- No `repo.task.*` event emitted by the workflow (the Plane webhook publishes each move)

### FAILURE:

- Proceeding without validating preconditions
- Silently failing on missing `.project.json` / `ticket_provider` block or workspace
- Not posting audit comment at state transition
- Emitting `bloodbank.repo.task.*` yourself (the Plane webhook is its only producer)
- Attempting to evaluate AC in this step (that's step 2)

**Master Rule:** Exit immediately on precondition failure. Never proceed with incomplete context.
