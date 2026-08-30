---
name: 'step-v-01-validate'
description: 'Validate all workflow prerequisites: Plane config, Bloodbank CLI, event naming contract, and workflow configuration'

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

**Check `.plane.json` in project root:**
- [ ] File exists
- [ ] Contains `workspace_slug`
- [ ] Contains `project_id`

**Check `~/.claude/plane-workspaces.json`:**
- [ ] File exists
- [ ] Contains entry matching the workspace_slug from `.plane.json`
- [ ] Entry has `api_key` and `base_url`

**Check Plane skill:**
- [ ] Directory exists at `~/.claude/skills/managing-tickets-and-tasks-in-plane/`

**Validate Plane API connectivity:**
- [ ] Attempt a read-only API call (e.g., list states) to confirm credentials work

### 2. Validate Bloodbank CLI

**Check Bloodbank installation:**
- [ ] `bb` is on PATH (`bb contract` prints the naming vocabulary)
- [ ] `bb-emit` is on PATH (this is the emitter; there is no `publish.sh`)

**Check Bloodbank connectivity:**
- [ ] `bb doctor` reports a usable local scaffold

### 3. Validate the Event Naming Contract

The contract is discoverable. Do not read allowlists out of source files, and do
not accept a type that has not been checked.

**Verify the type this workflow publishes is legal:**
- [ ] `bb emit --check --type bloodbank.repo.task.updated` exits 0 and prints PASS
- [ ] The registered schema exists at
      `~/code/33GOD/bloodbank/schemas/bloodbank/repo/task.updated.json`

**Verify no illegal literal survives in the workflow:**
- [ ] No step file contains a `"version"` envelope field
- [ ] No step file names a type outside `bloodbank.<domain>.<entity>.<action>`

### 4. Validate Workflow Configuration

**Check {workflowConfig}:**
- [ ] File exists and is valid YAML
- [ ] `ac_rubric` section present with all 4 criteria
- [ ] `qa.max_retries` defined (numeric, > 0)
- [ ] `staleness` section with durations for: triage, refining, in_progress, review, qa
- [ ] `plane_states` mapping present

**Check {acRubric}:**
- [ ] File exists
- [ ] Contains all 4 binary criteria (non_empty, testable, enumerated, fr_coverage)

**Check {eventSchemas}:**
- [ ] File exists
- [ ] Documents the `bloodbank.repo.task.updated` state-transition payload
- [ ] Documents the `bloodbank.repo.task.updated` staleness-report payload
- [ ] Every type literal in it passes `bb emit --check --type <literal>`

### 5. Present Validation Report

Display a structured report:

```
TICKET-LIFECYCLE PREREQUISITE VALIDATION
========================================

Plane Configuration:
  .plane.json ................ [PASS/FAIL]
  plane-workspaces.json ...... [PASS/FAIL]
  Plane skill ................ [PASS/FAIL]
  Plane API connectivity ..... [PASS/FAIL]

Bloodbank:
  CLI installation ........... [PASS/FAIL]
  bb doctor .................. [PASS/FAIL]

Event Naming Contract:
  bb emit --check ............ [PASS/FAIL]
  Registered schema present .. [PASS/FAIL]

Workflow Configuration:
  workflow.yaml .............. [PASS/FAIL]
  AC sufficiency rubric ...... [PASS/FAIL]
  Event schema docs .......... [PASS/FAIL]

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

- All prerequisite categories checked (Plane, Bloodbank, event contract, Config)
- Every check item evaluated even if others fail
- Clear pass/fail report with remediation steps for failures
- No files or state modified during validation

### FAILURE:

- Stopping at the first failure without checking remaining items
- Modifying any configuration or state
- Attempting to process tickets or trigger workflow execution
- Missing remediation guidance for failed checks

**Master Rule:** Read-only diagnostic. Check everything. Report clearly. Fix nothing.
