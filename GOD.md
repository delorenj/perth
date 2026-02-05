# Perth - GOD Document

> **Guaranteed Organizational Document** - Developer-facing reference for Perth
>
> **Last Updated**: 2026-02-02
> **Domain**: Workspace Management
> **Status**: Development

---

## Product Overview

**Perth** is a specialized fork of [Zellij](https://github.com/zellij-org/zellij) designed as the primary terminal IDE for the **33GOD ecosystem**. Named after the Australian city (continuing 33GOD's place-based naming convention), Perth transforms the standard terminal multiplexer into an agentic workspace environment optimized for multi-agent AI workflows.

Perth extends Zellij's foundation with native support for visual notifications, frame-based animations, PostgreSQL-backed session persistence, and a structured Dashboard interface that integrates with 33GOD services (Bloodbank, iMi, Jelmore).

**Key Capabilities:**
- **Native Visual Notifications**: Per-pane and per-tab alerts with styled borders (error/success/warning)
- **Animation Engine**: Low-CPU frame-based animations for activity indicators (candycane loader)
- **PostgreSQL Session Persistence**: Full session state survives crashes and reboots with write-behind caching
- **ZDrive Integration**: Programmatic workspace control via external `zdrive` CLI with Redis-backed context
- **Dashboard System** (Milestone 1): Multi-pane interface integrating Bloodbank, iMi, and ZDrive browsers
- **Integration Adapter Layer**: Clean abstraction for external CLI tool integration (Bloodbank, iMi, Jelmore)
- **Template Registry**: Predefined, reproducible layout patterns (Dashboard, Task-View)

---

## Architecture Position

```mermaid
graph TB
    subgraph "33GOD Workspace Management Domain"
        IMI[iMi<br/>Worktree Manager]
        JM[Jelmore<br/>Session Manager]
        ZD[Zellij-Driver<br/>Terminal Context]
        PT[Perth<br/>Zellij Distribution]
    end

    subgraph "33GOD Infrastructure Domain"
        BB[Bloodbank<br/>Event Bus]
    end

    subgraph "External Infrastructure"
        PG[(PostgreSQL)]
        REDIS[(Redis)]
    end

    IMI -->|worktree events| BB
    JM -->|session events| BB
    ZD -->|context tracking| REDIS
    ZD -->|controls via IPC| PT
    PT -->|Dashboard subscribes| BB
    PT -->|session persistence| PG
    PT -->|queries projects| IMI
    PT -->|spawns sessions| JM
```

**Role in Pipeline**: Perth serves as the **primary human-agent interface** in the 33GOD ecosystem. It provides the terminal environment where agents execute tasks, humans monitor progress, and both collaborate through visual notifications and session management.

**Architectural Pattern**: Modular Monolith with Event-Driven Extensions + Adapter Integration Layer

**Rationale**:
- Zellij is already a modular monolith; Perth extends specific modules (Server, Client) without breaking the single-binary deployment model
- Dashboard and external integrations follow an **Adapter pattern** to isolate 33GOD-specific logic from core Zellij functionality
- Template-driven layouts enable reproducible UI patterns without hardcoding Dashboard logic into core rendering

---

## Event Contracts

### Bloodbank Events Emitted

_Perth does not emit events to Bloodbank._ It operates as a consumer/observer in the 33GOD event ecosystem.

### Bloodbank Events Consumed

Perth's Dashboard components consume events via external CLI adapters (subprocess execution), not direct Bloodbank subscription.

| Event Source | Adapter | Handler | Purpose |
|--------------|---------|---------|---------|
| `bloodbank subscribe --format json` | `BloodbankAdapter` | Bloodbank Event Feed Pane | Real-time event stream display |
| `imi list --json` | `iMiAdapter` | iMi Project Browser Pane | Project registry queries |
| `jelmore start-session <id>` | `JelmoreAdapter` | ZDrive Session Browser | Spawn/resume agent sessions |

**Note**: Event consumption is implemented via subprocess management rather than direct RabbitMQ connection. This provides clean separation between Perth (terminal primitives) and Bloodbank (event infrastructure).

---

## Non-Event Interfaces

### CLI Interface

```bash
# Session Management
perth attach [session_name]         # Attach to session (or create if new)
perth attach --template <name>      # Attach with template (e.g., 33god-dashboard)
perth attach --restore-from-db <id> # Restore session from PostgreSQL

# Notifications
perth notify --pane-id <ID> --style <error|success|warning> --message <Text>

# Template Management
perth template list                 # List available templates
perth template show <name>          # Display template definition
perth template create --name <name> --file <path>  # Import YAML template

# Drive Commands (workspace manipulation)
perth drive create-tab --name <Name> --layout <Path>
perth drive inject-text --pane-id <ID> --text <Content>
perth drive rename-pane --pane-id <ID> --name <Name>
```

**Commands:**

| Command | Arguments | Description |
|---------|-----------|-------------|
| `perth attach` | `[session_name]` | Create or attach to named session |
| `perth attach --template` | `<template_name>` | Attach with predefined layout |
| `perth notify` | `--pane-id`, `--style`, `--message` | Send visual notification to pane |
| `perth template list` | - | List registered templates |
| `perth template show` | `<name>` | Display template definition |
| `perth template create` | `--name`, `--file` | Import YAML template to registry |
| `perth drive create-tab` | `--name`, `--layout` | Create named tab with layout |
| `perth drive inject-text` | `--pane-id`, `--text` | Send text input to pane |
| `perth drive rename-pane` | `--pane-id`, `--name` | Rename specific pane |

### ZDrive CLI (External Companion Tool)

Perth integrates with the external `zdrive` CLI for context-aware workspace control:

```bash
# Pane Navigation
zdrive pane <name>                  # Focus or create pane by semantic name
zdrive pane <name> --tab <tab>      # With explicit tab placement

# Intent Tracking
zdrive pane log <pane> "<message>"  # Log development progress
zdrive pane log <pane> "<msg>" --type milestone  # Log milestone
zdrive pane history <pane>          # Review intent history

# Identifier-Based Session Attachment
zdrive attach --yiid <agent_id>              # Attach by Yi agent ID
zdrive attach --plane-ticket <ticket_id>     # Attach by Plane ticket
zdrive attach --bloodbank-event <event_id>   # Attach by Bloodbank correlation ID

# Session Management
zdrive sessions --show-identifiers  # List sessions with metadata
```

**ZDrive Architecture:**
- Implemented as external `zellij-driver` tool (separate from Perth binary)
- Redis-backed state for metadata persistence
- Controls Perth via `zellij action` subprocess calls (IPC over Unix socket)
- Provides clean separation: zdrive handles **context management**, Perth handles **terminal primitives**

### Configuration Interface

**Config Location**: `~/.config/zellij/config.yaml` (inherits Zellij config path)

**Perth-Specific Configuration:**

```yaml
# PostgreSQL connection (overridden by DATABASE_URL env var)
database_url: postgres://perth:perth@localhost:5432/perth

# Notification defaults
notifications:
  auto_clear_on_focus: true
  animation_fps: 60
```

**Environment Variables:**

| Variable | Purpose | Default |
|----------|---------|---------|
| `DATABASE_URL` | PostgreSQL connection string | None (persistence disabled) |
| `ZDRIVE_REDIS_URL` | Redis URL for ZDrive context | `redis://localhost:6379` |

---

## Technical Deep-Dive

### Technology Stack
- **Language**: Rust (Edition 2021)
- **Runtime**: Tokio (async runtime from Zellij)
- **Database**: PostgreSQL 16+ (via `sqlx` crate)
- **Caching**: Redis (for ZDrive context, via external tool)
- **Build System**: Cargo with `xtask` task runner
- **WASM Plugins**: WebAssembly plugins (inherited from Zellij)

### Architecture Pattern

Perth follows a **Modular Monolith with Event-Driven Extensions** pattern:

```
Perth Binary (Monolith)
├── zellij-server/        # Core server logic, extended with Perth modules
│   ├── persistence/      # PostgreSQL session persistence
│   ├── notifications/    # Notification routing bus
│   ├── integrations/     # External CLI adapters (Bloodbank, iMi, Jelmore)
│   └── templates/        # Layout template registry
├── zellij-client/        # Rendering engine, extended with Perth modules
│   ├── animation/        # Frame-based animation engine
│   └── components/       # Dashboard pane components
└── zellij-utils/         # Shared utilities (notification types, etc.)
```

### Key Implementation Details

#### Persistence Manager (`zellij-server/src/persistence/`)

Implements **write-behind caching** for PostgreSQL persistence:

```rust
pub struct PersistenceManager {
    pool: Option<PgPool>,
    write_queue_tx: UnboundedSender<WriteOperation>,
    is_available: bool,
}
```

- State changes are queued to `write_queue_tx` and written asynchronously
- Does not block main event loop (NFR-001: <5ms latency increase)
- Graceful degradation: if DB unavailable, continues in ephemeral mode (NFR-003)
- Session/tab/pane CRUD operations with JSONB for flexible schemas

#### Notification Bus (`zellij-server/src/notifications/`)

Routes notifications from CLI commands to pane rendering:

```rust
pub struct NotificationBus {
    pending_notifications: HashMap<PaneId, Notification>,
}
```

- Last-write-wins semantics (new notification replaces existing)
- Auto-clear on pane focus (configurable)
- Three styles: Error (red), Success (green), Warning (yellow)

#### Animation Engine (`zellij-client/src/animation/`)

Frame-based animation system with dirty region optimization:

```rust
pub trait AnimationEngine: Send + Sync {
    fn next_frame(&mut self) -> Option<AnimationFrame>;
    fn target_fps(&self) -> u32;
    fn reset(&mut self);
    fn adaptive_fps(&self, cpu_usage_percent: f32) -> u32;
}
```

- 60fps target with adaptive degradation to 30fps under high CPU (>80%)
- Dirty region tracking: only update animated cells, not full pane
- Candycane pattern: `[block][dark][medium][light]` shifting at 60fps

**Candycane Animation Pattern:**
```
Frame 0: ████████
Frame 1: ▓████▓▒░
Frame 2: ▒▓████▓▒
Frame 3: ░▒▓████▓
Frame 4: ████████ (cycles)
```

#### Integration Adapter Layer (`zellij-server/src/integrations/`)

Abstraction for external CLI tool integration:

```rust
#[async_trait]
pub trait IntegrationAdapter: Send + Sync {
    async fn call(&self, args: &[&str]) -> IntegrationResult<String>;
    async fn subscribe(&self, args: &[&str]) -> IntegrationResult<Receiver<String>>;
    async fn stop(&self) -> IntegrationResult<()>;
    fn is_healthy(&self) -> bool;
    fn name(&self) -> &str;
}
```

**Adapter Implementations:**
- `BloodbankAdapter`: Long-running `bloodbank subscribe` subprocess with auto-restart
- `iMiAdapter`: One-shot `imi list --json` CLI calls
- `JelmoreAdapter`: One-shot `jelmore start-session` / `jelmore resume-session` calls

**Error Isolation:**
- Each adapter runs in isolated async task
- Failures caught at adapter boundary, displayed in affected pane only
- Automatic restart with exponential backoff (3 retries: 1s, 2s, 4s)

### Data Models

#### PostgreSQL Schema

```sql
-- Sessions table
CREATE TABLE sessions (
    id UUID PRIMARY KEY,
    name TEXT NOT NULL,
    template_name TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    last_active TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_sessions_name ON sessions(name);
CREATE INDEX idx_sessions_last_active ON sessions(last_active);

-- Tabs table
CREATE TABLE tabs (
    id UUID PRIMARY KEY,
    session_id UUID REFERENCES sessions(id) ON DELETE CASCADE,
    position INT NOT NULL,
    name TEXT NOT NULL,
    layout_blob JSONB,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

-- Panes table
CREATE TABLE panes (
    id UUID PRIMARY KEY,
    tab_id UUID REFERENCES tabs(id) ON DELETE CASCADE,
    pane_id TEXT NOT NULL,
    pane_type TEXT NOT NULL,  -- 'terminal', 'bloodbank-feed', 'imi-browser', 'zdrive-browser'
    component_state JSONB,
    title TEXT,
    cwd TEXT,
    command TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

-- Templates table
CREATE TABLE templates (
    id UUID PRIMARY KEY,
    name TEXT UNIQUE NOT NULL,
    definition JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
CREATE UNIQUE INDEX idx_templates_name ON templates(name);
```

#### Rust Data Models

```rust
// Session record
pub struct SessionRecord {
    pub id: Uuid,
    pub name: String,
    pub template_name: Option<String>,
    pub created_at: DateTime<Utc>,
    pub last_active: DateTime<Utc>,
}

// Notification types
pub enum NotificationStyle {
    Error,   // Red border
    Success, // Green border
    Warning, // Yellow border
}

pub struct Notification {
    pub style: NotificationStyle,
    pub message: String,
}
```

### Configuration

**Template YAML Schema:**

```yaml
name: "33god-dashboard"
tabs:
  - name: "Dashboard"
    panes:
      - type: "bloodbank-feed"
        position: { x: 0, y: 0, width: 100%, height: 33% }
      - type: "imi-browser"
        position: { x: 0, y: 33%, width: 100%, height: 33% }
      - type: "zdrive-browser"
        position: { x: 0, y: 66%, width: 100%, height: 34% }
```

**Supported Pane Types:**
- `terminal`: Standard PTY-backed pane
- `bloodbank-feed`: Real-time Bloodbank event stream
- `imi-browser`: Paginated iMi project list
- `zdrive-browser`: Active/historic session browser with animations

---

## Development

### Prerequisites

- **Rust Toolchain**: 1.92+ (stable)
- **Protocol Buffers**: `protoc` compiler
- **PostgreSQL**: 16+ (optional, for persistence)
- **Redis**: For ZDrive context (optional)

### Setup

```bash
# Clone Perth repository
git clone https://github.com/33GOD/perth.git
cd perth

# Build all components (uses xtask)
cargo xtask build

# Set up PostgreSQL (optional)
export DATABASE_URL=postgres://perth:perth@localhost:5432/perth
sqlx database create
sqlx migrate run

# Install locally
cargo install --path .
```

### Running Locally

```bash
# Development run (debug mode)
cargo xtask run

# Development run with arguments
cargo xtask run -- -l strider

# Release build run
cargo run --release

# With PostgreSQL persistence
DATABASE_URL=postgres://perth:perth@localhost:5432/perth cargo xtask run
```

### Testing

```bash
# Run all unit tests
cargo xtask test

# Run specific test module
cargo test --package zellij-server -- persistence::tests

# Run clippy linting
cargo xtask clippy

# Format code
cargo xtask format

# Run E2E tests (Docker required)
docker-compose up -d
cargo xtask ci e2e --build
cargo xtask ci e2e --test
```

### Code Style

- **Formatting**: Always run `cargo xtask format` before commits
- **Linting**: `cargo xtask clippy` (CI reports `clippy::correctness` as errors)
- **Error Handling**:
  - Prefer `Result<T>` over `.unwrap()` / `.expect()`
  - Use `zellij_utils::errors::prelude::*`
  - Attach context with `.context("message")`
  - Use `.non_fatal()` for non-fatal errors in void functions

---

## Deployment

### Installation Methods

**From Source (Recommended):**
```bash
cargo install --path .
```

**Binary Distribution:**
- GitHub Releases (manual for Milestone 1)
- See `docs/THIRD_PARTY_INSTALL.md` for OS-specific packages

### Configuration

**Configuration File:** `~/.config/zellij/config.yaml`

**Template Directory:** `~/.config/zellij/templates/`

**Database Configuration:**
- Primary: `DATABASE_URL` environment variable
- Fallback: `database_url` in config.yaml
- If neither set: persistence disabled (ephemeral mode)

### Production Considerations

**Database:**
- PostgreSQL 16+ recommended
- Connection pooling: 10 max, 2 min connections
- Async writes with 5-second acquire timeout

**Performance Targets:**
- Input latency increase: <5ms (NFR-001)
- Dashboard pane response: <50ms (NFR-004)
- Animation CPU usage: <5% for single candycane loader
- Dashboard launch: <500ms to rendered panes

**Reliability:**
- Session persistence RPO: 1 second (write-behind flush interval)
- Session recovery RTO: 5 seconds (restore from DB)
- Graceful degradation if PostgreSQL unavailable

---

## Roadmap

### Milestone 1: The Holocene Dashboard (In Progress)

**Goal:** Transform Perth into an agentic IDE with a functional Dashboard tab.

| Sprint | Status | Features |
|--------|--------|----------|
| **Sprint 1** | Completed | Persistence Manager, Notification Bus, Animation Engine, ZDrive Integration |
| **Sprint 2** | In Progress | Integration Adapter Framework, Bloodbank/iMi/Jelmore Adapters |
| **Sprint 3** | Not Started | Template Registry, Layout Instantiation |
| **Sprint 4** | Not Started | Dashboard Components (Bloodbank Feed, iMi Browser, ZDrive Browser) |
| **Sprint 5** | Not Started | E2E Integration, Performance Validation |

### Future Milestones

- **Milestone 2**: Advanced agent collaboration features
- **Milestone 3**: Voice integration (TalkyTonny)
- **Milestone 4**: Upstream Zellij feature sync

---

## References

- **Domain Doc**: `docs/domains/workspace-management/GOD.md`
- **System Doc**: `docs/GOD.md`
- **Source**: `perth/` (this repository)
- **PRD**: `perth/docs/prd-perth-2026-01-22.md`
- **Architecture**: `perth/docs/architecture-perth-2026-01-22.md`
- **Sprint Plan**: `perth/docs/sprint-plan-perth-2026-01-22.md`
- **ZDrive Notes**: `perth/docs/zdrive-integration-notes.md`
- **Upstream Zellij**: https://github.com/zellij-org/zellij
- **Zellij Documentation**: https://zellij.dev/documentation/
