# Sprint Plan: Perth (33GOD IDE)

**Date:** 2026-01-22
**Scrum Master:** System Architect (Claude)
**Project Level:** 2
**Total Stories:** 18
**Total Points:** 82
**Planned Sprints:** 5

---

## Executive Summary

This sprint plan breaks down the Perth (33GOD IDE) implementation into 5 logical sprints, progressing from core infrastructure through Dashboard UI components to final integration and testing. Each sprint builds on the previous, respecting technical dependencies while delivering incremental value.

**Key Metrics:**
- Total Stories: 18
- Total Points: 82 points
- Sprints: 5
- Target: Milestone 1 Dashboard completion

**Sprint Overview:**
- Sprint 1: Core Infrastructure (24 points) - Persistence, ZDrive, Notifications, Animations
- Sprint 2: Integration Layer (16 points) - Adapter framework and external CLI integration
- Sprint 3: Template System (10 points) - Layout templates and instantiation
- Sprint 4: Dashboard Components (21 points) - Bloodbank, iMi, ZDrive panes
- Sprint 5: Integration & Testing (11 points) - E2E validation and performance tuning

---

## Story Inventory

### STORY-INF-001: Database Schema Setup

**Epic:** Epic 3 - Database Persistence Layer
**Priority:** Must Have
**Points:** 3

**User Story:**
As a System
I want a PostgreSQL database schema for sessions, tabs, panes, and templates
So that session state can be persisted and recovered reliably

**Acceptance Criteria:**
- [ ] `sessions` table with id, name, template_name, created_at, last_active
- [ ] `tabs` table with id, session_id, position, name, layout_blob (JSONB)
- [ ] `panes` table with id, tab_id, pane_id, pane_type, component_state (JSONB), title, cwd, command
- [ ] `pane_history` table with pane_id, chunk_index, content (ByteA)
- [ ] `templates` table with id, name, definition (JSONB), created_at, updated_at
- [ ] Indexes on sessions.name, sessions.last_active, templates.name (unique)
- [ ] sqlx migrations created and validated

**Technical Notes:**
- Use `sqlx-cli` for migration management
- JSONB for flexible schema (templates, component state)
- ByteA with compression for scrollback history
- Module: `zellij-server/src/persistence/schema.sql`

**Dependencies:**
- PostgreSQL 16+ installed
- `sqlx` crate added to Cargo.toml

---

### STORY-001: Persistence Manager

**Epic:** Epic 3 - Database Persistence Layer
**Priority:** Must Have
**Points:** 8

**User Story:**
As a User
I want my Perth session to automatically persist to PostgreSQL
So that I can recover my workspace after crashes or reboots

**Acceptance Criteria:**
- [ ] `PersistenceManager` struct implements write-behind caching strategy
- [ ] Async task queues state changes (sessions, tabs, panes) to DB
- [ ] On startup, `--restore-from-db <session_id>` reconstructs session from DB
- [ ] Graceful degradation if DB unavailable (logs error, continues without persistence)
- [ ] Session creation writes to `sessions` table
- [ ] Tab/pane creation writes to `tabs`/`panes` tables with JSONB layout snapshots
- [ ] Unit tests with `sqlx::MockDatabase`

**Technical Notes:**
- Module: `zellij-server/src/persistence/manager.rs`
- Use Tokio channels for async write queue
- Wrap all DB calls in `Result<T, PersistenceError>`
- NFR-003: Must not crash on DB failure

**Dependencies:**
- STORY-INF-001 (schema must exist)
- Tokio async runtime (existing Zellij dependency)

---

### STORY-002: ZDrive Controller

**Epic:** Epic 2 - Core Control Integration
**Priority:** Must Have
**Points:** 5

**User Story:**
As an Agent
I want to programmatically create tabs, rename panes, and inject input via CLI
So that I can automate workspace manipulation

**Acceptance Criteria:**
- [ ] `zellij drive create-tab --name <Name> --layout <Path>` creates named tab
- [ ] `zellij drive inject-text --pane-id <ID> --text <Content>` sends input to pane
- [ ] `zellij drive rename-pane --pane-id <ID> --name <Name>` renames pane
- [ ] CLI commands translate to internal Zellij `Action`s
- [ ] IPC integration with `zellij-server` via Unix domain socket
- [ ] Error handling for invalid pane IDs, missing layouts
- [ ] Integration tests for all 3 commands

**Technical Notes:**
- Module: `zellij-server/src/zdrive/controller.rs`
- CLI parsing via `clap` (existing framework)
- Map CLI args → `Action::CreateTab`, `Action::Write`, `Action::RenamePan`e
- FR-002, FR-003

**Dependencies:**
- Zellij core `Action` enum (exists)

---

### STORY-003: Notification Bus

**Epic:** Epic 1 - Native Notification System
**Priority:** Must Have
**Points:** 5

**User Story:**
As an Agent
I want to send visual notifications to specific panes/tabs
So that users see build failures or task completions without switching context

**Acceptance Criteria:**
- [ ] `zellij notify --pane-id <ID> --style <error|success|warning> --message <Text>` triggers notification
- [ ] Server-side: `NotificationBus` updates pane metadata with notification state
- [ ] Client-side: Renderer interprets metadata and applies visual effects (border color, icon)
- [ ] Notification persists until pane is focused
- [ ] Support for 3 styles: error (red), success (green), warning (yellow)
- [ ] Unit tests for notification routing

**Technical Notes:**
- Modules:
  - `zellij-server/src/notifications/bus.rs` - Server-side routing
  - `zellij-client/src/renderer.rs` - Client-side rendering hooks
- Notification stored in pane metadata (new field: `notification: Option<Notification>`)
- FR-001

**Dependencies:**
- None (standalone feature)

---

### STORY-004: Animation Engine

**Epic:** Epic 1 - Native Notification System
**Priority:** Should Have
**Points:** 3

**User Story:**
As a User
I want to see smooth, low-CPU animations for active session indicators
So that the Dashboard feels polished and responsive

**Acceptance Criteria:**
- [ ] `AnimationEngine` trait defines frame-based animation interface
- [ ] Candycane pattern implemented: `█▓▒░░▒▓█` repeating, shifting 1 cell/frame at 60fps
- [ ] Animation updates only dirty regions (horizontal bar for ZDrive loader)
- [ ] CPU usage <5% for single animation
- [ ] Graceful degradation to 30fps if CPU >80%
- [ ] Unit tests for frame generation logic

**Technical Notes:**
- Modules:
  - `zellij-client/src/animation/engine.rs` - Core animation loop
  - `zellij-client/src/animation/candycane.rs` - Candycane pattern
- Animations run in client render cycle, modifying cell styles (bg/fg colors)
- Time-based frame index: `frame = (elapsed_ms / 16) % pattern_length` (60fps = 16ms/frame)
- FR-005, NFR-001

**Dependencies:**
- None (client-side only)

---

### STORY-005: Integration Adapter Framework

**Epic:** Epic 5 - Integration Layer
**Priority:** Must Have
**Points:** 8

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
- Modules:
  - `zellij-server/src/integrations/adapter.rs` - Trait definition
  - `zellij-server/src/integrations/subprocess.rs` - SubprocessManager
- Use `tokio::process::Command` for async subprocess management
- `tokio::select!` for concurrent stdout/stderr reading
- Bounded channels to prevent memory growth
- NFR-005: Error isolation

**Dependencies:**
- Tokio async runtime

---

### STORY-006: Bloodbank Adapter

**Epic:** Epic 5 - Integration Layer
**Priority:** Must Have
**Points:** 5

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

**Dependencies:**
- STORY-005 (Integration Adapter Framework)
- Bloodbank CLI (external, mocked for tests)

---

### STORY-007: iMi & Jelmore Adapters

**Epic:** Epic 5 - Integration Layer
**Priority:** Must Have
**Points:** 3

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

**Dependencies:**
- STORY-005 (Integration Adapter Framework)
- iMi CLI, Jelmore CLI (external, mocked for tests)

---

### STORY-008: Template Registry

**Epic:** Epic 6 - Template System
**Priority:** Must Have
**Points:** 5

**User Story:**
As a User
I want to define reusable layout templates in YAML
So that I can instantiate consistent workspace layouts (Dashboard, Task-View)

**Acceptance Criteria:**
- [ ] `TemplateRegistry` loads YAML files from `~/.config/zellij/templates/`
- [ ] Parses YAML with `serde_yaml` into `Template` struct
- [ ] Stores templates in PostgreSQL `templates` table as JSONB
- [ ] `zellij template list` lists all templates
- [ ] `zellij template show <name>` displays template definition
- [ ] `zellij template create --name <name> --file <path>` imports YAML → DB
- [ ] Template schema: name, tabs (list of tab configs with pane types)
- [ ] Unit tests for YAML parsing and DB storage

**Technical Notes:**
- Module: `zellij-server/src/templates/registry.rs`
- Template schema supports pane types: `terminal`, `bloodbank-feed`, `imi-browser`, `zdrive-browser`
- YAML for developer experience, JSONB for runtime efficiency
- Milestone: M1

**Dependencies:**
- STORY-INF-001 (templates table must exist)

---

### STORY-009: Template Instantiation Logic

**Epic:** Epic 6 - Template System
**Priority:** Must Have
**Points:** 5

**User Story:**
As a User
I want to attach to a session with `--template` flag
So that my workspace is pre-configured with the Dashboard layout

**Acceptance Criteria:**
- [ ] `zellij attach 33GOD` or `zellij attach --template 33god-dashboard` triggers template instantiation
- [ ] Template Registry loads template from DB
- [ ] Server creates tab with name from template
- [ ] For each pane in template:
  - If `terminal`: Spawn PTY with configured command
  - If Dashboard component: Initialize component state, spawn subprocess if needed
- [ ] Client receives layout update and renders panes
- [ ] Template name stored in `sessions.template_name` for recovery
- [ ] Integration test: attach with template → verify 3 panes rendered

**Technical Notes:**
- Module: `zellij-server/src/templates/instantiate.rs`
- Orchestration logic: template → tabs → panes → component initialization
- Integrates with STORY-006, STORY-007 for Dashboard component spawning
- Milestone: M1

**Dependencies:**
- STORY-008 (Template Registry)
- STORY-006, STORY-007 (adapters for Dashboard components)

---

### STORY-010: Bloodbank Event Feed Pane

**Epic:** Epic 4 - Dashboard System
**Priority:** Must Have
**Points:** 5

**User Story:**
As a User
I want to see real-time Bloodbank events in the Dashboard
So that I can monitor system activity without leaving the IDE

**Acceptance Criteria:**
- [ ] `BloodbankEventFeedPane` component renders event list with scrollback
- [ ] Events displayed with timestamp, event type, message
- [ ] Last 100 events buffered in memory
- [ ] Scroll offset tracked in component state
- [ ] Read-only (no user interaction in M1)
- [ ] Integration with `BloodbankAdapter` via `Receiver<Event>`
- [ ] Graceful error display if Bloodbank subprocess crashes

**Technical Notes:**
- Module: `zellij-client/src/components/dashboard/bloodbank_feed.rs`
- Component state: `events: Vec<Event>`, `scroll_offset: usize`
- Subscribes to event stream on initialization
- Milestone: M1

**Dependencies:**
- STORY-006 (Bloodbank Adapter)
- STORY-009 (Template instantiation to spawn component)

---

### STORY-011: iMi Project Browser Pane

**Epic:** Epic 4 - Dashboard System
**Priority:** Must Have
**Points:** 5

**User Story:**
As a User
I want to browse iMi projects with pagination
So that I can quickly switch to recent projects

**Acceptance Criteria:**
- [ ] `iMiProjectBrowserPane` component renders paginated project list
- [ ] Displays: Project name, short description
- [ ] 5 projects per page
- [ ] Hotkey navigation: j/k for scroll, h/l for pagination
- [ ] Ordered by `last_active` (most recent first)
- [ ] Cache project list for 5 minutes, refresh on manual trigger
- [ ] Component state: `projects: Vec<Project>`, `current_page: usize`, `selected_index: usize`

**Technical Notes:**
- Module: `zellij-client/src/components/dashboard/imi_browser.rs`
- Queries iMi on initialization via `iMiAdapter`
- Component state persisted to JSONB for recovery (optional in M1)
- Milestone: M1

**Dependencies:**
- STORY-007 (iMi Adapter)
- STORY-009 (Template instantiation to spawn component)

---

### STORY-012: ZDrive Session Browser Pane

**Epic:** Epic 4 - Dashboard System
**Priority:** Must Have
**Points:** 8

**User Story:**
As a User
I want to browse active and historic ZDrive sessions
So that I can resume previous work or start new agent sessions

**Acceptance Criteria:**
- [ ] `ZDriveSessionBrowserPane` component renders two sections:
  - Active sessions (top) with animated candycane loader
  - Historic sessions (bottom) ordered by recency
- [ ] Queries local ZDrive PostgreSQL DB (`sessions` table)
- [ ] Active sessions: `last_active < 1 hour`
- [ ] Historic sessions: `last_active >= 1 hour`, show last 20
- [ ] Hotkey navigation: j/k for scroll, Enter for selection
- [ ] Selection triggers `JelmoreAdapter` to spawn/resume session
- [ ] Animation: candycane pattern updates at 60fps
- [ ] Component state: `active_sessions`, `historic_sessions`, `selected_index`, `animation_frame`

**Technical Notes:**
- Module: `zellij-client/src/components/dashboard/zdrive_browser.rs`
- Most complex Dashboard component (DB queries + animation + interaction)
- Uses STORY-004 Animation Engine for candycane loader
- Uses STORY-007 Jelmore Adapter for session activation
- Milestone: M1

**Dependencies:**
- STORY-001 (Persistence Manager for DB access)
- STORY-004 (Animation Engine)
- STORY-007 (Jelmore Adapter)
- STORY-009 (Template instantiation to spawn component)

---

### STORY-013: Dashboard Layout Integration

**Epic:** Epic 4 - Dashboard System
**Priority:** Must Have
**Points:** 3

**User Story:**
As a User
I want the Dashboard to render all 3 panes in a consistent layout
So that I have a unified entry point for 33GOD workflows

**Acceptance Criteria:**
- [ ] Dashboard template (`33god-dashboard.yaml`) defines 3-pane layout:
  - Bloodbank Event Feed (top 33%)
  - iMi Project Browser (middle 33%)
  - ZDrive Session Browser (bottom 34%)
- [ ] Template instantiation creates all 3 panes
- [ ] Component lifecycle: initialize → subscribe to data sources → render
- [ ] All panes functional and interactive
- [ ] Integration test: `zellij attach 33GOD` → verify Dashboard rendered

**Technical Notes:**
- Glue logic tying STORY-010, STORY-011, STORY-012 together
- Template definition stored in `~/.config/zellij/templates/33god-dashboard.yaml`
- Milestone: M1

**Dependencies:**
- STORY-009 (Template instantiation)
- STORY-010 (Bloodbank Feed Pane)
- STORY-011 (iMi Browser Pane)
- STORY-012 (ZDrive Session Browser Pane)

---

### STORY-014: Milestone 1 End-to-End Integration

**Epic:** Milestone 1 - Dashboard Validation
**Priority:** Must Have
**Points:** 5

**User Story:**
As a Product Owner
I want to validate Milestone 1 acceptance criteria
So that I confirm the Dashboard is fully functional

**Acceptance Criteria:**
- [ ] User runs `zellij attach 33GOD`
- [ ] Dashboard tab renders with 3 panes:
  - Bloodbank Event Feed showing real-time events
  - iMi Project Browser with pagination
  - ZDrive Session Browser with active/historic sessions + candycane animation
- [ ] User navigates ZDrive pane (j/k), selects session (Enter)
- [ ] Jelmore spawns/resumes Task-View session
- [ ] New tab opens for agent session
- [ ] End-to-end flow validated manually and via automated test

**Technical Notes:**
- Integration testing scenario from Milestone 1 document
- Validates all components working together
- Manual testing: launch real Bloodbank, iMi, Jelmore services
- Automated test: mock services with test fixtures
- Milestone: M1

**Dependencies:**
- STORY-013 (Dashboard Layout Integration)
- All prior stories must be complete

---

### STORY-015: Performance Validation

**Epic:** NFR Validation
**Priority:** Should Have
**Points:** 3

**User Story:**
As a System
I want to validate performance NFRs are met
So that Perth feels responsive and doesn't degrade Zellij's core performance

**Acceptance Criteria:**
- [ ] NFR-001: Input latency increase <5ms (benchmark before/after Dashboard)
- [ ] NFR-004: Dashboard pane input response <50ms (key press → visual update)
- [ ] Animation CPU usage <5% for single candycane loader
- [ ] Dashboard launch <500ms from `zellij attach` to rendered panes
- [ ] Session browser query <100ms for 1000 sessions
- [ ] Bloodbank event rendering handles 100 events/sec without lag
- [ ] Performance report generated with benchmark results

**Technical Notes:**
- Use Rust benchmarking: `cargo bench`
- Profile with `perf` or `flamegraph`
- Identify hotspots and optimize
- NFR-001, NFR-004

**Dependencies:**
- STORY-014 (E2E integration complete for testing)

---

### STORY-016: Error Isolation & Reliability Tests

**Epic:** NFR Validation
**Priority:** Should Have
**Points:** 3

**User Story:**
As a System
I want to validate error handling and reliability NFRs
So that Perth degrades gracefully under failure conditions

**Acceptance Criteria:**
- [ ] NFR-003: Kill PostgreSQL during session → Zellij continues, logs error
- [ ] NFR-005: Simulate `imi` CLI missing → only iMi pane shows error, others functional
- [ ] Bloodbank subprocess crash → error message + manual restart option
- [ ] Integration adapter failures isolated (one adapter crash doesn't affect others)
- [ ] DB connection failure → graceful fallback to ephemeral mode
- [ ] Reliability test suite: 10 negative test cases

**Technical Notes:**
- Integration tests with fault injection
- Use `kill -9` to simulate crashes
- Mock missing CLIs by removing from PATH
- Validate error messages displayed in panes
- NFR-003, NFR-005

**Dependencies:**
- STORY-014 (E2E integration complete for testing)

---

## Sprint Allocation

### Sprint 1: Core Infrastructure (24 points)

**Goal:** Establish foundational systems for persistence, CLI control, notifications, and animations

**Stories:**
- STORY-INF-001: Database Schema Setup (3 points) - Infrastructure
- STORY-001: Persistence Manager (8 points) - Must Have
- STORY-002: ZDrive Controller (5 points) - Must Have
- STORY-003: Notification Bus (5 points) - Must Have
- STORY-004: Animation Engine (3 points) - Should Have

**Total:** 24 points

**Deliverables:**
- PostgreSQL schema with all tables and indexes
- Session persistence with async DB writes
- CLI commands for tab/pane manipulation and input injection
- Visual notification system with 3 styles (error, success, warning)
- Candycane animation pattern for active indicators

**Risks:**
- PostgreSQL integration complexity (async writes, recovery logic)
- IPC coordination between CLI, server, and client

**Dependencies:**
- PostgreSQL 16+ installed
- Existing Zellij codebase familiarity

---

### Sprint 2: Integration Layer (16 points)

**Goal:** Build clean abstraction for external CLI tool integration with error isolation

**Stories:**
- STORY-005: Integration Adapter Framework (8 points) - Must Have
- STORY-006: Bloodbank Adapter (5 points) - Must Have
- STORY-007: iMi & Jelmore Adapters (3 points) - Must Have

**Total:** 16 points

**Deliverables:**
- `IntegrationAdapter` trait with subprocess management
- Bloodbank real-time event subscription
- iMi project list query
- Jelmore session spawn/resume

**Risks:**
- Subprocess lifecycle management (crashes, restarts)
- JSON parsing errors from external CLIs

**Dependencies:**
- Bloodbank CLI (external, mock for tests)
- iMi CLI (external, mock for tests)
- Jelmore CLI (external, mock for tests)

---

### Sprint 3: Template System (10 points)

**Goal:** Enable predefined layout templates for reproducible workspace configurations

**Stories:**
- STORY-008: Template Registry (5 points) - Must Have
- STORY-009: Template Instantiation Logic (5 points) - Must Have

**Total:** 10 points

**Deliverables:**
- YAML template loading and JSONB storage
- CLI commands for template management (`list`, `show`, `create`)
- Template instantiation on `zellij attach --template`
- `33god-dashboard` template definition

**Risks:**
- YAML parsing errors
- Template schema evolution (backwards compatibility)

**Dependencies:**
- STORY-INF-001 (templates table)
- STORY-006, STORY-007 (adapters for Dashboard components)

---

### Sprint 4: Dashboard Components (21 points)

**Goal:** Implement the 3-pane Dashboard interface with Bloodbank, iMi, and ZDrive browsers

**Stories:**
- STORY-010: Bloodbank Event Feed Pane (5 points) - Must Have
- STORY-011: iMi Project Browser Pane (5 points) - Must Have
- STORY-012: ZDrive Session Browser Pane (8 points) - Must Have
- STORY-013: Dashboard Layout Integration (3 points) - Must Have

**Total:** 21 points

**Deliverables:**
- Bloodbank Event Feed with real-time event rendering
- iMi Project Browser with pagination (j/k/h/l navigation)
- ZDrive Session Browser with active/historic sessions and candycane animation
- Unified Dashboard layout with all 3 panes functional

**Risks:**
- Component state management complexity
- Animation performance (candycane at 60fps)
- Integration of multiple data sources

**Dependencies:**
- Sprint 2 (Integration Layer)
- Sprint 3 (Template System)
- STORY-004 (Animation Engine)

---

### Sprint 5: Integration & Testing (11 points)

**Goal:** Validate Milestone 1 acceptance criteria and ensure NFRs are met

**Stories:**
- STORY-014: Milestone 1 End-to-End Integration (5 points) - Must Have
- STORY-015: Performance Validation (3 points) - Should Have
- STORY-016: Error Isolation & Reliability Tests (3 points) - Should Have

**Total:** 11 points

**Deliverables:**
- Milestone 1 acceptance validated (Dashboard launch → session selection → Jelmore activation)
- Performance benchmarks (input latency, animation CPU, Dashboard launch time)
- Reliability tests (DB failure, CLI missing, subprocess crash)
- Deployment-ready Perth binary

**Risks:**
- Integration bugs discovered late
- Performance bottlenecks requiring refactoring

**Dependencies:**
- Sprint 4 (Dashboard Components complete)
- Real Bloodbank, iMi, Jelmore services for manual testing

---

## Epic Traceability

| Epic ID | Epic Name | Stories | Total Points | Sprints |
|---------|-----------|---------|--------------|---------|
| Epic 1 | Native Notification System | STORY-003, STORY-004 | 8 points | Sprint 1 |
| Epic 2 | Core Control Integration | STORY-002 | 5 points | Sprint 1 |
| Epic 3 | Database Persistence Layer | STORY-INF-001, STORY-001 | 11 points | Sprint 1 |
| Epic 4 | Dashboard System | STORY-010, STORY-011, STORY-012, STORY-013 | 21 points | Sprint 4 |
| Epic 5 | Integration Layer | STORY-005, STORY-006, STORY-007 | 16 points | Sprint 2 |
| Epic 6 | Template System | STORY-008, STORY-009 | 10 points | Sprint 3 |
| Milestone 1 | Dashboard Validation | STORY-014, STORY-015, STORY-016 | 11 points | Sprint 5 |

---

## Functional Requirements Coverage

| FR ID | FR Name | Story | Sprint |
|-------|---------|-------|--------|
| FR-001 | Native Visual Notifications | STORY-003 | 1 |
| FR-002 | Integrated CLI Control | STORY-002 | 1 |
| FR-003 | Programmatic Input Injection | STORY-002 | 1 |
| FR-004 | Postgres-Backed Session Persistence | STORY-001 | 1 |
| FR-005 | Advanced UI Animations | STORY-004 | 1 |
| (Implicit) | Dashboard Entry Point | STORY-013 | 4 |
| (Implicit) | Bloodbank Integration | STORY-006, STORY-010 | 2, 4 |
| (Implicit) | iMi Integration | STORY-007, STORY-011 | 2, 4 |
| (Implicit) | ZDrive UI | STORY-012 | 4 |
| (Implicit) | Jelmore Integration | STORY-007, STORY-012 | 2, 4 |

---

## Non-Functional Requirements Coverage

| NFR ID | NFR Name | Solution | Validation Story |
|--------|----------|----------|------------------|
| NFR-001 | Performance Overhead (<5ms) | Async DB writes, dirty region rendering, caching | STORY-015 |
| NFR-002 | Compatibility | Optional persistence, conditional Dashboard | STORY-001, STORY-009 |
| NFR-003 | Database Reliability | Graceful failure, fallback to ephemeral mode | STORY-016 |
| NFR-004 | Component Responsiveness (<50ms) | Sync input handling, async data updates | STORY-015 |
| NFR-005 | Integration Error Isolation | Isolated adapter tasks, error boundaries | STORY-016 |

---

## Risks and Mitigation

### High Risks

**Risk:** PostgreSQL integration adds complexity to core Zellij logic
- **Impact:** High - Could destabilize existing multiplexer functionality
- **Mitigation:** Wrap all DB logic in async tasks, graceful fallback to ephemeral mode if DB unavailable (STORY-001)
- **Owner:** Sprint 1

**Risk:** Subprocess management for Bloodbank/iMi/Jelmore is brittle
- **Impact:** High - Dashboard panes could crash or become unresponsive
- **Mitigation:** Health checks, automatic restart with exponential backoff, error isolation per adapter (STORY-005, STORY-006)
- **Owner:** Sprint 2

### Medium Risks

**Risk:** Animation performance (candycane loader) consumes excessive CPU
- **Impact:** Medium - Could violate NFR-001 (<5ms latency increase)
- **Mitigation:** Dirty region rendering (only update animated cells), degrade to 30fps if CPU >80% (STORY-004)
- **Owner:** Sprint 1, validated in Sprint 5 (STORY-015)

**Risk:** Template schema evolution breaks existing templates
- **Impact:** Medium - Users lose saved layouts
- **Mitigation:** Version template schema in DB, migration path for v1 → v2 (STORY-008)
- **Owner:** Sprint 3

### Low Risks

**Risk:** Bloodbank/iMi/Jelmore CLIs not installed on user system
- **Impact:** Low - Dashboard panes show "CLI not installed" error
- **Mitigation:** Graceful error messages, don't crash Zellij (STORY-005, STORY-007)
- **Owner:** Sprint 2, validated in Sprint 5 (STORY-016)

---

## Dependencies

### External Dependencies

**Bloodbank CLI:**
- Required for: STORY-006, STORY-010
- Fallback: Mock service for testing
- Risk: API changes break JSON parsing

**iMi CLI:**
- Required for: STORY-007, STORY-011
- Fallback: Mock service for testing
- Risk: CLI not installed on user system

**Jelmore CLI:**
- Required for: STORY-007, STORY-012
- Fallback: Mock service for testing
- Risk: Session spawn failures

**PostgreSQL 16+:**
- Required for: All persistence features (STORY-001, STORY-012)
- Fallback: Ephemeral mode (no persistence)
- Risk: DB connection failures

### Internal Dependencies

**Story Dependencies:**
```
Sprint 1:
  STORY-INF-001 → STORY-001 (schema must exist before Persistence Manager)

Sprint 2:
  STORY-005 → STORY-006, STORY-007 (adapters depend on framework)

Sprint 3:
  STORY-INF-001 → STORY-008 (templates table must exist)
  STORY-006, STORY-007 → STORY-009 (adapters needed for Dashboard component spawning)

Sprint 4:
  STORY-006 → STORY-010 (Bloodbank adapter needed for Event Feed)
  STORY-007 → STORY-011 (iMi adapter needed for Project Browser)
  STORY-001, STORY-004, STORY-007 → STORY-012 (DB, animation, Jelmore needed for Session Browser)
  STORY-010, STORY-011, STORY-012 → STORY-013 (all panes needed for Dashboard integration)

Sprint 5:
  STORY-013 → STORY-014, STORY-015, STORY-016 (Dashboard must be complete for testing)
```

---

## Definition of Done

For a story to be considered complete:

- [ ] **Code Implemented:** All acceptance criteria met, code committed to feature branch
- [ ] **Unit Tests Written:** ≥80% coverage for new modules
- [ ] **Integration Tests Passing:** E2E scenarios validated
- [ ] **Documentation Updated:** Architecture doc, inline comments for complex logic
- [ ] **Manual Testing:** Feature validated in local dev environment
- [ ] **Performance Validated:** No regressions, NFRs met (if applicable)
- [ ] **Code Reviewed:** Self-review or peer review (if team size >1)
- [ ] **Deployed to Local:** Binary built and tested end-to-end

**Notes:**
- No PR reviews needed (single developer)
- Documentation updates in architecture doc, not separate docs
- Deployment means local `cargo install --path .`

---

## Open Questions for Review

1. **Bloodbank restart policy:** Is 3 retries with exponential backoff sufficient, or should we implement a circuit breaker pattern?
   - **Decision:** Proceed with 3 retries for M1, add circuit breaker in post-M1 iteration if needed

2. **iMi cache TTL:** Should the 5-minute project list cache be configurable via config file?
   - **Decision:** Hardcode 5min for M1, add config option in post-M1 if users request it

3. **Template validation:** Should we add `zellij template validate <file>` CLI command to catch malformed templates before import?
   - **Decision:** Add basic YAML parse validation on import (STORY-008), skip separate validate command for M1

---

## Next Steps

### Immediate Action

**Begin Sprint 1: Core Infrastructure**

Run `/dev-story STORY-INF-001` to start with database schema setup, or run `/dev-story STORY-001` to dive into Persistence Manager implementation.

**Recommended Order:**
1. STORY-INF-001 (Database schema) - Foundation for all persistence
2. STORY-001 (Persistence Manager) - Core infrastructure
3. STORY-002 (ZDrive Controller) - Independent of DB
4. STORY-003 (Notification Bus) - Independent of DB
5. STORY-004 (Animation Engine) - Client-side only

### Sprint Cadence

Estimated effort-based progression (not time-based):
- Sprint 1: XL effort (24 points) - Most complex (DB + IPC + client-server coordination)
- Sprint 2: L effort (16 points) - Subprocess management complexity
- Sprint 3: M effort (10 points) - Straightforward YAML parsing + DB storage
- Sprint 4: L effort (21 points) - UI components + state management
- Sprint 5: M effort (11 points) - Integration testing + validation

**No sprint deadlines** - progress measured by story completion, not calendar dates.

---

**This plan was created using BMAD Method v6 - Phase 4 (Implementation Planning)**
