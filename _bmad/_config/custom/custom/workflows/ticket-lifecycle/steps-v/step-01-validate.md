---
name: 'step-v-01-validate'
description: 'Validate all workflow prerequisites: Plane config, px, the no-emit rule, and workflow configuration'

workflowConfig: '../workflow.yaml'
acRubric: '../data/ac-sufficiency-rubric.md'
eventSchemas: '../data/event-schemas.md'
---

# Step V1: Validate Workflow Prerequisites

## STEP GOAL:

To verify that all external dependencies, configuration files, and tooling required by the ticket-lifecycle workflow are present and correctly configured.

## MANDATORY EXECUTION RULES (READ FIRST):

### Universal Rules:

- Read the complete step file before taking any action.
- Report ALL findings before concluding. Do not stop at the first failure.

### Role Reinforcement:

- You are a diagnostic checker. Read-only. No modifications.
- Present clear pass/fail results with actionable remediation for failures.

### Step-Specific Rules:

- Focus ONLY on prerequisite validation.
- FORBIDDEN to modify any files, configuration, or state.
- FORBIDDEN to process tickets or trigger workflows.
- Check ALL items even if early checks fail.

## MANDATORY SEQUENCE

**CRITICAL:** Follow this sequence exactly. Do not skip, reorder, or improvise.

### 1. Validate Plane Configuration

**Check `.project.json` in project root:**
- [ ] File exists
- [ ] Contains a `ticket_provider` block
- [ ] `ticket_provider.workspace` is present
- [ ] `ticket_provider.board_id` is present and non-empty

**Check `~/.claude/plane-workspaces.json`:**
- [ ] File exists
- [ ] Contains entry matching `ticket_provider.workspace` from `.project.json`
- [ ] Entry has `api_key` and `base_url`

**Check Plane skill:**
- [ ] Directory exists at `~/.claude/skills/managing-tickets-and-tasks-in-plane/`

**Validate Plane API connectivity:**
- [ ] Attempt a read-only API call (e.g., list states) to confirm credentials work

### 2. Validate the Ticket Writer

**Check px:**
- [ ] `px` is on PATH
- [ ] `px whoami --json` resolves this repo's board binding
- [ ] `px --help` lists `move` (Pilot >= 0.2.0)
- [ ] Every lane named under `states` in {workflowConfig} exists on the board:
      `px move <any ticket> "<lane>" --dry-run --json` resolves it

### 3. Validate the No-Emit Rule

This workflow publishes no Bloodbank events. Ticket facts
(`bloodbank.repo.task.*`, `bloodbank.repo.board.*`) come only from the Plane
webhook normalizer (n8n `Plane → Bloodbank`), which turns every state move into
`bloodbank.repo.task.updated`.

**Verify no emit step survives in the workflow:**
- [ ] No step file tells the orchestrator to broadcast, publish, or `bb emit` a
      `repo.task.*` or `repo.board.*` type
- [ ] Every state move in steps-c/ is a `px move` followed by "Emit nothing"

### 4. Validate Workflow Configuration

**Check {workflowConfig}:**
- [ ] File exists and is valid YAML
- [ ] `ac_rubric` section present with all 4 criteria
- [ ] `qa.max_retries` defined (numeric, > 0)
- [ ] `staleness` section with durations for: triage, refining, in_progress, review, qa
- [ ] `states` mapping present

**Check {acRubric}:**
- [ ] File exists
- [ ] Contains all 4 binary criteria (non_empty, testable, enumerated, fr_coverage)

**Check {eventSchemas}:**
- [ ] File exists
- [ ] States that the workflow emits no ticket events and names the Plane
      webhook as the only producer of `bloodbank.repo.task.*`
- [ ] Documents staleness as a move to `blocked` plus an audit comment, not an event

### 5. Present Validation Report

Display a structured report:

```
TICKET-LIFECYCLE PREREQUISITE VALIDATION
========================================

Plane Configuration:
  .project.json (ticket_provider) [PASS/FAIL]
  plane-workspaces.json ...... [PASS/FAIL]
  Plane skill ................ [PASS/FAIL]
  Plane API connectivity ..... [PASS/FAIL]

Ticket Writer:
  px on PATH ................. [PASS/FAIL]
  px whoami .................. [PASS/FAIL]
  px move available .......... [PASS/FAIL]
  states lanes on board ...... [PASS/FAIL]

No-Emit Rule:
  no emit step in steps-c/ ... [PASS/FAIL]

Workflow Configuration:
  workflow.yaml .............. [PASS/FAIL]
  AC sufficiency rubric ...... [PASS/FAIL]
  Bloodbank rule doc ......... [PASS/FAIL]

Overall: [ALL CHECKS PASSED / X of Y FAILED]
```

**IF ALL PASS:**
"**All prerequisites validated. Workflow is ready for execution.**"

**IF ANY FAIL:**
For each failure, provide:
- What failed
- Why it matters
- How to fix it

"**Prerequisites incomplete. Resolve the above issues before running the workflow.**"

---

## SYSTEM SUCCESS/FAILURE METRICS

### SUCCESS:

- All prerequisite categories checked (Plane, px, no-emit rule, Config)
- Every check item evaluated even if others fail
- Clear pass/fail report with remediation steps for failures
- No files or state modified during validation

### FAILURE:

- Stopping at the first failure without checking remaining items
- Modifying any configuration or state
- Attempting to process tickets or trigger workflow execution
- Missing remediation guidance for failed checks

**Master Rule:** Read-only diagnostic. Check everything. Report clearly. Fix nothing.
