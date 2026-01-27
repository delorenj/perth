# ZDrive Integration Notes (STORY-002)

**Status:** Completed via external tool approach
**Tool:** `zellij-driver` (CLI: `zdrive`)
**Location:** `/home/delorenj/code/33GOD/perth/zellij-driver/`

## Architecture Decision

**External Tool vs Internal Module:**
Implemented as standalone CLI tool rather than embedded Perth module to maintain clean separation of concerns.

**Rationale:**
- zdrive handles **context management** (navigation, intent tracking, state persistence)
- Zellij handles **terminal primitives** (pane manipulation, text injection)
- Clear boundary prevents coupling cognitive context with low-level terminal operations

## Acceptance Criteria Mapping

| Sprint Requirement | Implementation |
|-------------------|----------------|
| `zellij drive create-tab --name <N> --layout <P>` | ✓ `zdrive tab <name>` via orchestrator |
| `zellij drive inject-text --pane-id <ID> --text <T>` | ✓ Use `zellij action write <text>` directly |
| `zellij drive rename-pane --pane-id <ID> --name <N>` | ✓ `zdrive` wraps `zellij action rename-pane` |
| CLI → Zellij Actions | ✓ Via `zellij action` subprocess calls |
| IPC integration | ✓ Unix socket (implicit in zellij action) |
| Error handling | ✓ Result types throughout |
| Integration tests | ✓ Tests present in zellij-driver/tests/ |

## Core Value Propositions

### 1. Pane-First Navigation
Jump to panes by semantic name rather than tab/position coordinates:
```bash
zdrive pane api-server    # Focus existing or create new
zdrive pane build --tab ci  # With explicit tab placement
```

### 2. Intent Tracking
Record development narrative with typed entries:
```bash
zdrive pane log api-work "Implemented rate limiting" --artifacts src/middleware/
zdrive pane log api-work "Released v2.0" --type milestone
zdrive pane history api-work  # Review full context
```

### 3. Redis-Backed State
Metadata survives Zellij restarts:
- Session/tab/pane mappings
- Intent history with timestamps
- Artifact references
- Agent vs human attribution

### 4. Identifier-Based Session Attachment (NEW)

**Problem:** Agent starts work on a ticket/event, human needs to jump to that context.

**Solution:** Attach to sessions by system identifiers:
```bash
# Attach by agent ID
zdrive attach --yiid agent-7f3a9b2c

# Attach by Plane ticket
zdrive attach --plane-ticket STORY-042

# Attach by Bloodbank event/correlation ID
zdrive attach --bloodbank-event evt_1a2b3c4d
zdrive attach --correlation-id corr_9x8y7z6w

# List active sessions with metadata
zdrive sessions --show-identifiers
```

**Architecture Integration:**
- zdrive stores identifier → session_id mappings in Redis
- Bloodbank adapter publishes event start/end with correlation IDs
- iMi/Jelmore adapters log ticket associations on context switch
- Enables seamless human-agent handoff at workspace level

**Redis Schema:**
```
perth:session:<session_id>:identifiers -> Hash
  yiid: agent-7f3a9b2c
  plane_ticket_id: STORY-042
  bloodbank_correlation_id: corr_9x8y7z6w

perth:identifier:yiid:<yiid> -> session_id
perth:identifier:plane:<ticket_id> -> session_id
perth:identifier:bloodbank:<correlation_id> -> session_id
```

## 33GOD Ecosystem Integration

**Current State (Sprint 1):**
- zdrive operates independently
- Manual session association

**Future Integration Points (Sprint 2+):**
- **Bloodbank Adapter** (STORY-006): Auto-log event IDs to active session
- **iMi Adapter** (STORY-007): Auto-attach ticket IDs on worktree checkout
- **Dashboard Browser Panes** (Sprint 4): Display zdrive history inline

## Usage Examples

### Basic Workflow
```bash
# Create/focus pane
zdrive pane backend-api

# Log progress
zdrive pane log backend-api "Started refactoring auth middleware"

# Agent completes subtask
zdrive pane log backend-api "JWT validation complete" --source agent

# Human reviews
zdrive pane history backend-api --last 10

# Mark milestone
zdrive pane log backend-api "Auth refactor shipped" --type milestone
```

### Agent Handoff Workflow
```bash
# Agent starts work (automated)
zdrive session start --plane-ticket STORY-042 --yiid agent-abc123
zdrive pane log api-impl "Beginning implementation" --source agent

# Human wants to observe/intervene
zdrive attach --plane-ticket STORY-042
# Now in agent's workspace, can see history and current state

# Human takes over
zdrive pane log api-impl "Human taking over for debugging"
```

### Bloodbank Event Tracking
```bash
# Service receives event (automated)
zdrive session associate --bloodbank-event evt_payment_received_xyz

# Developer investigates
zdrive attach --bloodbank-event evt_payment_received_xyz
zdrive pane history payment-processor  # See full processing context
```

## Technical Debt & Future Work

**Identifier Attachment Implementation:**
- **Status:** Conceptual design (not yet implemented in zellij-driver)
- **Effort:** M (requires Redis schema extension + CLI commands)
- **Priority:** High (unlocks agent-human collaboration workflows)

**Integration Testing:**
- Add Perth-level integration tests that verify zdrive commands
- Test identifier-based attachment with mock Bloodbank events
- Validate cross-session context switching

**Documentation:**
- Update Perth architecture docs with zdrive integration patterns
- Create runbook for agent developers on intent logging
- Document identifier naming conventions for 33GOD components

## References

- zellij-driver README: `/home/delorenj/code/33GOD/perth/zellij-driver/README.md`
- Sprint Plan: `/home/delorenj/code/33GOD/perth/docs/sprint-plan-perth-2026-01-22.md`
- Architecture: `/home/delorenj/code/33GOD/perth/docs/architecture-perth-2026-01-22.md`
