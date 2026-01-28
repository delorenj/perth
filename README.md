<h1 align="center">
  <br>
  <img src="https://raw.githubusercontent.com/zellij-org/zellij/main/assets/logo.png" alt="logo" width="200">
  <br>
  Perth
  <br>
  <br>
</h1>

<p align="center">
  <strong>The 33GOD Agentic IDE</strong><br>
  <em>A Zellij fork optimized for multi-agent workflows</em>
</p>

<p align="center">
  <a href="https://discord.gg/CrUAFH3"><img alt="Discord Chat" src="https://img.shields.io/discord/771367133715628073?color=5865F2&label=discord&style=flat-square"></a>
  <a href="https://matrix.to/#/#zellij_general:matrix.org"><img alt="Matrix Chat" src="https://img.shields.io/matrix/zellij_general:matrix.org?color=1d7e64&label=matrix%20chat&style=flat-square&logo=matrix"></a>
  <a href="https://zellij.dev/documentation/"><img alt="Zellij documentation" src="https://img.shields.io/badge/zellij-documentation-fc0060?style=flat-square"></a>
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/zellij-org/zellij/main/assets/demo.gif" alt="demo">
</p>

<h4 align="center">
  [<a href="#33god-features">33GOD Features</a>]
  [<a href="#installation">Installation</a>]
  [<a href="https://zellij.dev/documentation/configuration">Configuration</a>]
  [<a href="#perth-roadmap">Roadmap</a>]
  [<a href="https://zellij.dev/documentation/faq">FAQ</a>]
</h4>

# What is Perth?

**Perth** is a specialized fork of [Zellij](https://github.com/zellij-org/zellij) designed to be the primary IDE for the **33GOD ecosystem**. It extends Zellij's terminal multiplexer capabilities with native support for:

- **Visual Notifications** - Per-pane alerts with styled borders (error/success/warning)
- **Animation Engine** - Frame-based UI animations for activity indicators
- **PostgreSQL Persistence** - Session state survives crashes and reboots
- **ZDrive Integration** - Programmatic workspace control with Redis-backed context

Perth is built for managing complex, multi-agent workflows where dozens of AI agents operate concurrently. It provides immediate visual feedback on background activity, robust session recovery, and seamless CLI automation.

## 33GOD Features

### Native Notification System (Sprint 1)
Visual alerts on panes and tabs when background tasks complete:
```bash
zellij notify --pane-id 3 --style error --message "Build failed"
```
- **Styles:** error (red), success (green), warning (yellow)
- **Auto-clear:** Notifications dismiss when pane receives focus

### Animation Engine (Sprint 1)
Low-CPU animations for active session indicators:
- Candycane pattern: `[block]|[dark]|[medium]|[light]` shifting at 60fps
- Dirty region updates for efficient rendering
- Adaptive FPS (degrades to 30fps under high CPU load)

### PostgreSQL Session Persistence (Sprint 1)
Full session state persisted to database:
```bash
export DATABASE_URL=postgres://perth:perth@localhost:5432/perth
zellij attach my-session  # State auto-persisted
```
- Write-behind caching (non-blocking)
- Graceful degradation if database unavailable

### ZDrive CLI Integration (Sprint 1)
Programmatic workspace control via external `zdrive` tool:
```bash
zdrive pane api-server        # Navigate by semantic name
zdrive pane log api "Started" # Track development intent
zdrive attach --plane-ticket STORY-042  # Identifier-based attachment
```
- Redis-backed metadata persistence
- Intent history with timestamps
- Agent/human attribution tracking

## What is Zellij?

[Zellij](https://github.com/zellij-org/zellij) is a workspace aimed at developers, ops-oriented people and anyone who loves the terminal. Similar programs are sometimes called "Terminal Multiplexers".

Perth inherits all Zellij features: layouts, floating/stacked panes, WebAssembly plugins, web-client, and more. See the [Zellij documentation](https://zellij.dev/documentation/) for base functionality.

## Installation

### Prerequisites

**For 33GOD features (notifications, persistence):**
- PostgreSQL 16+ (optional but recommended)
- Redis (for ZDrive CLI, optional)

**For base Zellij functionality:**
- No additional dependencies

### From Source (Recommended)

```bash
# Clone the repository
git clone https://github.com/33GOD/perth.git
cd perth

# Build and install
cargo install --path .

# Optional: Set up PostgreSQL persistence
export DATABASE_URL=postgres://perth:perth@localhost:5432/perth
```

### Package Managers

See [THIRD_PARTY_INSTALL.md](./docs/THIRD_PARTY_INSTALL.md) for OS-specific packages.

### Configuration

Perth configuration lives in `~/.config/zellij/config.yaml`. 33GOD-specific settings:

```yaml
# PostgreSQL connection (overridden by DATABASE_URL env var)
database_url: postgres://perth:perth@localhost:5432/perth

# Notification defaults
notifications:
  auto_clear_on_focus: true
  animation_fps: 60
```

### Try Zellij without installing

For base Zellij functionality (without 33GOD features):
```bash
bash <(curl -L https://zellij.dev/launch)
```

## How do I start a development environment?

* Clone the project
* In the project folder, for debug builds run: `cargo xtask run`
* To run all tests: `cargo xtask test`

For more build commands, see [CONTRIBUTING.md](CONTRIBUTING.md).

## Configuration
For configuring Zellij, please see the [Configuration Documentation](https://zellij.dev/documentation/configuration.html).

## About issues in this repository
Issues in this repository, whether open or closed, do not necessarily indicate a problem or a bug in the software. They only indicate that the reporter wanted to communicate their experiences or thoughts to the maintainers. The Zellij maintainers do their best to go over and reply to all issue reports, but unfortunately cannot promise these will always be dealt with or even read. Your understanding is appreciated.

## Perth Roadmap

### Milestone 1: The Holocene Dashboard

**Goal:** Transform Perth into an agentic IDE with a functional Dashboard tab.

| Sprint | Status | Features |
|--------|--------|----------|
| **Sprint 1** | Completed | Persistence Manager, Notification Bus, Animation Engine, ZDrive Integration |
| **Sprint 2** | Not Started | Integration Adapter Framework, Bloodbank/iMi/Jelmore Adapters |
| **Sprint 3** | Not Started | Template Registry, Layout Instantiation |
| **Sprint 4** | Not Started | Dashboard Components (Bloodbank Feed, iMi Browser, ZDrive Browser) |
| **Sprint 5** | Not Started | E2E Integration, Performance Validation |

See [sprint-plan-perth-2026-01-22.md](./docs/sprint-plan-perth-2026-01-22.md) for detailed story breakdown.

### Upstream Zellij Roadmap

Perth maintains compatibility with upstream Zellij. See the [Zellij roadmap](https://zellij.dev/roadmap) for base features.

## Documentation

- **[PRD](./docs/prd-perth-2026-01-22.md)** - Product Requirements Document
- **[Architecture](./docs/architecture-perth-2026-01-22.md)** - System Architecture
- **[Sprint Plan](./docs/sprint-plan-perth-2026-01-22.md)** - Story breakdown and sprint allocation
- **[Sprint 1 Summary](./docs/sprint-1-summary.md)** - Completed Sprint 1 deliverables
- **[ZDrive Notes](./docs/zdrive-integration-notes.md)** - External ZDrive CLI integration

## Origin of the Name

**Perth:** Named for the Australian city, continuing the tradition of naming 33GOD components after places. Perth represents a "fresh start" destination, fitting for an IDE designed for new agent-driven workflows.

**Zellij:** [From Wikipedia](https://en.wikipedia.org/wiki/Zellij) - Zellij (Arabic: zillij) is a style of mosaic tilework made from individually hand-chiseled tile pieces. The pieces were typically of different colors and fitted together to form various patterns based on tessellations, most notably elaborate Islamic geometric motifs such as radiating star patterns. This form of Islamic art is found in the architecture of Morocco, Algeria, Tunisia, and al-Andalus.

## License

MIT

## Sponsored by
<a href="https://terminaltrove.com/"><img src="https://avatars.githubusercontent.com/u/121595180?s=200&v=4" width="80px"></a>
