# Bloodbank Event Contract — ticket-lifecycle

This workflow publishes exactly **one** event family: `bloodbank.repo.task.updated`.

## The grammar

`bb contract` is the authority. Nothing here overrides it.

```
type     bloodbank.<domain>.<entity>.<action>          4 tokens
subject  bloodbank.<kind>.<domain>.<entity>.<action>   5 tokens, kind = evt|cmd|rpy
```

- Versioning lives ONLY in `schemaref` / `dataschema`. There is no `version`
  field in the envelope, and no `v1` token in a type.
- Identity — repo, agent, ticket, project — lives in `data.*` and `actor.*`.
  Never as a token.
- Shape-valid is not contract-valid: a well-formed 4-token type whose action is
  not in the allowlist is refused.

**Never invent a name.** Before writing any producer, run:

```bash
bb contract                                       # legal domains / entities / actions
bb emit --check --type bloodbank.repo.task.updated  # rc=1 if the name is illegal
```

## bloodbank.repo.task.updated

Registered schema: `~/code/33GOD/bloodbank/schemas/bloodbank/repo/task.updated.json`
("A provider-neutral repo task changed. Provider provenance and the lossless
ticket entity live in data.")

Every transition this workflow drives — triage, refining, ready, in_progress,
review, qa, done, blocked — is one `bloodbank.repo.task.updated`.

**Staleness is not a separate family.** A state that exceeds its max duration in
{workflowConfig} is the same event, distinguished by `data.trigger_source` and
the stuck-state detail in `data`. Nothing in the system ever published or
consumed a distinct staleness type, so none is invented here; if a consumer
one day genuinely needs one, register the schema and add it to the contract
first, then use it.

`data` — required by the schema:

| field | value from |
| --- | --- |
| `repo` | repository directory name |
| `slug` | `project_slug` in `.project.json` |
| `workspace` | `ticket_provider.workspace` in `.project.json` |
| `board_id` | `ticket_provider.board_id` in `.project.json` |
| `project_id` | provider project UUID (same as `board_id` for Plane) |
| `ticket_id` | provider ticket UUID |
| `provider` | `ticket_provider.type` (`plane`, `trello`, …) |
| `provider_event_type` | provenance name — see below |
| `changed_fields` | array of field names that moved; `[]` for a staleness report |
| `timestamp` | ISO 8601 |
| `ticket` | lossless ticket JSON as the provider returned it |

Optional and used by this workflow: `ticket_key`, `title`, `previous_phase`,
`phase`, `trigger_source`. `data` allows additional properties, which is where
the staleness detail rides.

### State transition

```json
{
  "repo": "{repo directory name}",
  "slug": "{project_slug from .project.json}",
  "workspace": "{ticket_provider.workspace}",
  "board_id": "{ticket_provider.board_id}",
  "project_id": "{provider project UUID}",
  "ticket_id": "{provider ticket UUID}",
  "ticket_key": "{human ticket key, e.g. PJAN-88}",
  "title": "{ticket title}",
  "provider": "plane",
  "provider_event_type": "ticket-lifecycle.transitioned",
  "previous_phase": "{state before transition}",
  "phase": "{state after transition}",
  "changed_fields": ["state"],
  "trigger_source": "ticket-lifecycle-workflow",
  "timestamp": "{ISO 8601}",
  "ticket": { "…": "lossless provider ticket JSON" }
}
```

### Staleness report

Same type. `phase` is the state the ticket is stuck in, nothing changed, and the
duration detail is carried alongside.

```json
{
  "repo": "{repo directory name}",
  "slug": "{project_slug from .project.json}",
  "workspace": "{ticket_provider.workspace}",
  "board_id": "{ticket_provider.board_id}",
  "project_id": "{provider project UUID}",
  "ticket_id": "{provider ticket UUID}",
  "provider": "plane",
  "provider_event_type": "ticket-lifecycle.stalled",
  "previous_phase": "{stuck state}",
  "phase": "{stuck state}",
  "changed_fields": [],
  "trigger_source": "ticket-lifecycle-staleness",
  "stuck_state": "{state the ticket is stuck in}",
  "duration_minutes": "{how long it has been in this state}",
  "max_duration_minutes": "{configured max from workflow.yaml}",
  "timestamp": "{ISO 8601}",
  "ticket": { "…": "lossless provider ticket JSON" }
}
```

## Publishing

`bb-emit` is the emitter. **There is no `publish.sh`** — it never existed, and
any instruction that names one is stale.

```bash
# validate the name first — rc=1 and nothing is published if it is illegal
bb emit --check --type bloodbank.repo.task.updated

# publish (data on stdin, or --data '<json>')
printf '%s' "$payload" | bb emit \
  --type bloodbank.repo.task.updated \
  --service ticket-lifecycle \
  --strict
```

`bb emit` builds the envelope: it derives `subject`
(`bloodbank.evt.repo.task.updated`), `schemaref`
(`bloodbank.repo.task.updated.v1`), `dataschema`, `kind`, `domain` and `actor`.
Do not hand-write those fields, and do not add a `version` field — the emitter
refuses envelopes that violate the contract.
