# Bloodbank Contract — ticket-lifecycle

This workflow publishes **no** ticket events. It moves tickets; the facts
follow on their own.

## The rule

Agents never emit `bloodbank.repo.task.*` or `bloodbank.repo.board.*`. Those
facts have exactly one producer: the Plane webhook, normalized by the n8n
`Plane → Bloodbank` workflow
(`~/code/33GOD/bloodbank/docs/plane-event-normalization.md`).

| What this workflow does | What the webhook normalizer publishes |
| --- | --- |
| moves the ticket to a new state | `bloodbank.repo.task.updated` (`provider_event_type: plane.ticket.transitioned`) |
| edits the ticket (title, description, AC, labels) | `bloodbank.repo.task.updated` (`provider_event_type: plane.ticket.updated`) |
| posts an audit comment | `bloodbank.repo.task.appended` (`provider_event_type: plane.ticket.commented`) |

So every transition this workflow drives — triage, refining, ready,
in_progress, review, qa, done, blocked — already becomes one
`bloodbank.repo.task.updated`, carrying the lossless Plane ticket. A second,
hand-written copy from the workflow would be a duplicate fact with a
different provenance, and consumers would count the transition twice.

## How to move a ticket

One command does the move and posts the audit comment:

```bash
px move {ticket_id} "{states.<phase>}" -m "<audit comment>" --json
```

- `{states.<phase>}` is the lane name mapped for that phase under `states:` in
  {workflowConfig} (e.g. `{states.ready}` is `Todo` on the canonical board).
  `px move` resolves it strictly: exact name, then the name ignoring case and
  spacing. It never guesses; an unknown name fails and lists the board's
  lanes. Fix the mapping, don't retry with a different name.
- `-m` posts the `[TICKET-LIFECYCLE]` audit comment on the ticket. The comment
  is the place for detail (rubric evidence, failure history, stuck-state
  durations); it reaches the bus as `bloodbank.repo.task.appended`. px posts it
  even when the ticket is already in that lane (two phases can share one lane),
  and then skips the PATCH (`"changed": false`).
- Retrying after an uncertain failure? Read `changed` in the first result, or
  the ticket, before re-sending `-m`, or the comment lands twice.
- `px move` is for a legacy (non-Krebs) board. On a Krebs-managed board
  (`.project.json` `execution.mode` is `managed` or `shadow`) it refuses:
  there the lane moves only through the lifecycle commands (`px task plan|
  claim|handoff|complete|attention|release`), per the momo skill's
  managed-execution reference.
- Then move on. Do not publish anything, do not wait for an echo, and do not
  call `bb emit` for a `repo.task.*` or `repo.board.*` type.

## Staleness

A state that exceeds its max duration in {workflowConfig} is not an event.
Move the ticket to `blocked` (`px move {ticket_id} "{states.blocked}" -m ...`)
and put the detail in the audit comment:

```
[TICKET-LIFECYCLE] State Transition
---
from: {stuck state}
to: blocked
timestamp: {ISO 8601}
agent: orchestrator
reason: ticket-lifecycle-staleness
details:
  stuck_state: {state the ticket is stuck in}
  duration_minutes: {how long it has been in this state}
  max_duration_minutes: {configured max from workflow.yaml}
---
```

The move to `blocked` becomes `bloodbank.repo.task.updated`; the comment
becomes `bloodbank.repo.task.appended`. Consumers read the reason from there.

## What Momo may still publish

Judgment, not ticket state. A consequential call (pulling from To Do, cutting
scope, accepting a review) is recorded with the Momo skill's
`scripts/record-decision.py`, which publishes
`bloodbank.repo.decision.recorded`. That is a different fact from the ticket
move and is never a substitute for it.
