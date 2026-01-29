# Sprint 2 Plan: Integration Layer

**Project:** Perth (33GOD IDE)
**Sprint:** 2
**Date:** 2026-01-29
**Scrum Master:** Claude (Scrum Master Agent)
**Project Level:** 2
**Plane Project:** [33god/perth](https://plane.delo.sh/33god/projects/cbfbb641-33e2-43c6-a7d1-ce63136ab689/)

---

## Sprint Overview

**Sprint Goal:** Build clean abstraction for external CLI tool integration with error isolation

**Duration:** 2 weeks (flexible, effort-based)
**Committed Points:** 16 points
**Stories:** 3

**Epic:** Epic 5 - Integration Layer

---

## Sprint Backlog

### STORY-005: Integration Adapter Framework (8 points) - Must Have

**User Story:**
As a System
I want a clean abstraction for external CLI tool integration
So that Bloodbank, iMi, and Jelmore can be integrated without scattering subprocess logic

**Acceptance Criteria:**
- [ ] `IntegrationAdapter` trait with `call()` and `subscribe()` methods
- [ ] `SubprocessManager` spawns, monitors, and restarts long-running subprocesses
- [ ] Health checks detect subprocess crashes
- [ ] Automatic restart with exponential backoff (3 retries: 1s, 2s, 4s)
- [ ] Graceful shutdown on pane close
- [ ] Error isolation: adapter failure doesn't crash Zellij
- [ ] Mock adapter for unit testing
- [ ] Integration test with fake CLI script

**Technical Notes:**
- Module: `zellij-server/src/integrations/`
- Uses `tokio::process::Command` for async subprocess management
- `tokio::select!` for concurrent stdout/stderr reading
- Bounded channels (capacity 100) to prevent memory growth

**Dependencies:** None (foundation story)
**Blocks:** STORY-006, STORY-007
**Document:** [docs/stories/STORY-005.md](stories/STORY-005.md)

---

### STORY-006: Bloodbank Adapter (5 points) - Must Have

**User Story:**
As a Dashboard Component
I want to subscribe to Bloodbank events in real-time
So that users see event stream updates without polling

**Acceptance Criteria:**
- [ ] `BloodbankAdapter` spawns `bloodbank subscribe --format json` subprocess
- [ ] Parses newline-delimited JSON events via `serde_json`
- [ ] Returns `Receiver<Event>` for streaming events to Dashboard component
- [ ] Subprocess health checks every 5 seconds
- [ ] Auto-restart on crash (3 retries with exponential backoff)
- [ ] Error message displayed in pane if subprocess fails permanently
- [ ] Integration test with mock `bloodbank` script emitting fake events

**Technical Notes:**
- Module: `zellij-server/src/integrations/bloodbank.rs`
- Event buffer: last 100 events (bounded channel with capacity 100)
- JSON parsing errors logged but don't crash adapter
- Milestone: M1

**Dependencies:** STORY-005 (Integration Adapter Framework)
**Blocks:** STORY-010 (Bloodbank Event Feed Pane)

---

### STORY-007: iMi & Jelmore Adapters (3 points) - Must Have

**User Story:**
As a Dashboard Component
I want to query iMi projects and trigger Jelmore sessions
So that users can browse projects and launch agent sessions

**Acceptance Criteria:**
- [ ] `iMiAdapter` executes `imi list --json` and returns `Vec<Project>`
- [ ] Parses JSON with `serde_json` into `Project { name, description, last_active }`
- [ ] `JelmoreAdapter` executes `jelmore start-session <id>` and `jelmore resume-session <id>`
- [ ] Error handling: CLI not found → display "CLI not installed" message
- [ ] Integration tests with mock `imi` and `jelmore` scripts

**Technical Notes:**
- Modules:
  - `zellij-server/src/integrations/imi.rs`
  - `zellij-server/src/integrations/jelmore.rs`
- One-shot CLI calls (not long-running like Bloodbank)
- Simpler than Bloodbank adapter (no subprocess lifecycle management)
- Milestone: M1

**Dependencies:** STORY-005 (Integration Adapter Framework)
**Blocks:** STORY-011 (iMi Browser Pane), STORY-012 (ZDrive Session Browser)

---

## Story Order

**Recommended implementation order:**

1. **STORY-005** (8 points) - Foundation, required by others
2. **STORY-007** (3 points) - Simpler adapters, validates framework
3. **STORY-006** (5 points) - Complex adapter with subprocess lifecycle

**Rationale:** Start with foundation, validate with simpler adapters, then tackle the complex Bloodbank stream subscription.

---

## Definition of Done

For a story to be considered complete:
- [ ] Code implemented and committed to feature branch
- [ ] Unit tests written and passing (≥80% coverage)
- [ ] Integration tests passing
- [ ] Code reviewed (self-review for single developer)
- [ ] Documentation updated (inline rustdoc, architecture doc if needed)
- [ ] No clippy warnings
- [ ] Acceptance criteria validated (all ✓)
- [ ] Plane ticket updated to "Done" status

---

## Risks and Mitigation

**Risk:** Subprocess management complexity (crashes, restarts, deadlocks)
- **Impact:** High - Could block Dashboard integration
- **Mitigation:** Comprehensive integration tests with fault injection, careful async design

**Risk:** External CLI behavior differs from expectations
- **Impact:** Medium - JSON parsing, exit codes, stderr handling
- **Mitigation:** Mock CLIs for tests, defensive parsing, error isolation

**Risk:** Tokio channel backpressure with fast event streams
- **Impact:** Low - Memory growth if events arrive faster than consumed
- **Mitigation:** Bounded channels with drop-oldest policy

---

## Sprint Metrics

| Metric | Value |
|--------|-------|
| Committed Points | 16 |
| Stories | 3 |
| Must Have | 3 (100%) |
| Blocking Stories | 1 (STORY-005) |
| Test Coverage Target | ≥80% |

---

## Sprint 1 Retrospective (Summary)

**Completed:**
- STORY-001: Persistence Manager (8 points) ✓
- STORY-003: Notification Bus (5 points) ✓
- STORY-004: Animation Engine (3 points) ✓
- Total: 24/24 points delivered

**Velocity:** 24 points

**Key Learnings:**
- ZDrive Controller implemented as external `zellij-driver` tool (cleaner separation)
- Async Rust patterns well-established, can apply to Integration Layer

---

## Next Steps After Sprint 2

**Sprint 3: Template System (10 points)**
- STORY-008: Template Registry
- STORY-009: Template Instantiation Logic

**Sprint 4: Dashboard Components (21 points)**
- STORY-010: Bloodbank Event Feed Pane (depends on STORY-006)
- STORY-011: iMi Project Browser Pane (depends on STORY-007)
- STORY-012: ZDrive Session Browser Pane (depends on STORY-007)
- STORY-013: Dashboard Layout Integration

---

## Commands

```bash
# Begin implementation
/dev-story STORY-005

# Check sprint status
/bmad:workflow-status

# After sprint completion
/bmad:sprint-close
```

---

**This plan was created using BMAD Method v6 - Phase 4 (Implementation Planning)**
