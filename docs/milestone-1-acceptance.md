# Milestone 1 Acceptance Criteria: The Holocene Dashboard

**Goal:** Verify the transformation of Perth (Zellij Fork) into an agentic IDE (33GOD) by demonstrating a functional "Dashboard" tab.

## Scenario: The "33GOD" Session

1.  **Session Start**: User opens a session named `33GOD`.
2.  **Initial View**: User is greeted with the **Dashboard Tab**.

## Dashboard Layout & Components

The Dashboard tab contains three specific panes:

### 1. Bloodbank Event Feed Pane
*   **Content**: Real-time feed of events from the Bloodbank event bus.
*   **Functionality**: Read-only display of system events (PoC level).
*   **Requirement**: Must verify integration with `bloodbank` CLI or service.

### 2. iMi Project Info Pane
*   **Content**: List of registered projects from `iMi`.
*   **Data**: Project Name, Short Description.
*   **Ordering**: Most recently active first.
*   **UI**: List ~5 items, simple pagination (hotkeys for next/prev page).
*   **Requirement**: Must verify integration with `imi` CLI/DB.

### 3. ZellijDrive (ZDrive) Pane
*   **Content**: Session information populated from the **ZDrive PostgreSQL DB**.
*   **Structure**:
    *   Active Sessions (Top)
    *   Historic Sessions (Bottom, ordered by recency)
*   **Visuals**:
    *   **Active Indicator**: "Animated cyclic candycane-like lightgreen/darkgreen color mappy twisty loader" (horizontal, width of list entry).
    *   *Note: This validates the Animation Engine (Epic 1).*
*   **Interaction**:
    *   Selecting a session **Activates** it.
    *   Activation opens the **Task-View Template**.
    *   *Note: This validates ZDrive Controller & CLI (Epic 2).*

## Task-View Session (Context)
*   **Definition**: The session where the agentic coder (Claude, Gemini, etc.) runs.
*   **Instantiation**: Managed by **Jelmore** (Unified CLI for agentic tools).
*   **Flow**: The Dashboard (ZDrive Pane) uses Jelmore to launch/resume these sessions.

---

**Success Definition**: Milestone 1 is complete when this Dashboard can be launched, rendered with the specified animations, and successfully navigates to a Task-View session using the native ZDrive infrastructure.
