---
workflowType: 'prd'
inputDocuments:
  - '/Users/delorenj/code/perth/docs/research/technical-perth-voice-zellij-2026-03-29.md'
---

# Product Requirements Document - Perth

**Author:** Jarad  
**Date:** 2026-03-29  
**Project Type:** Android mobile app  
**Domain:** Developer productivity / terminal workflow  
**Context:** Greenfield

## Product Summary

Perth is a voice-driven Android client for Zellij sessions. It connects to a zellij server, lets the user browse sessions, tabs, and panes on mobile, and supports three voice interaction modes: command mode, transcription mode, and task mode.

The app is designed for hands-free terminal interaction when typing is inconvenient, while still preserving a typed fallback for direct input.

## Problem Statement

Terminal workflows are fast on desktop but awkward on mobile. Perth solves this by letting a user connect to Zellij sessions from Android, navigate panes with swipe gestures, and control the active pane primarily through voice.

## Goals

- Connect to a zellij-backed Zellij session from Android
- Show sessions, tabs, and panes clearly on mobile
- Support swipe-based navigation between tabs and panes
- Provide three voice modes: command, transcription, and task
- Preserve verbatim transcription when requested
- Execute terminal commands safely with confirmation
- Offer a typed fallback when voice is unavailable

## Non-Goals

- Replacing desktop Zellij
- Building a general-purpose chat assistant
- Running arbitrary destructive commands without confirmation
- Creating a server-side orchestration platform
- Supporting non-Android platforms in the MVP

## Project Classification

- **Project Type:** Mobile app
- **Domain:** Developer productivity / terminal tooling
- **Context:** Greenfield
- **Complexity:** High

## Personas

### 1. Power User
Uses Zellij heavily and wants to interact with sessions while away from a keyboard.

### 2. Voice-First Operator
Prefers speaking commands or notes instead of typing on a small screen.

### 3. Mobile Terminal User
Needs quick access to a specific session, tab, or pane from a phone.

## User Journeys

### 1. Connect and Resume a Session
1. Open Perth
2. Connect to the zellij server
3. View available sessions
4. Select a session
5. Resume the active tabs and panes

### 2. Navigate with Swipe Gestures
1. Open a session
2. Swipe left/right to move between tabs
3. Swipe within the active tab to move between panes
4. Observe the active pane update clearly

### 3. Transcribe Speech into the Terminal
1. Choose transcription mode
2. Speak a prompt or note
3. Receive verbatim text
4. Paste the text into the active terminal pane

### 4. Create a Task File by Voice
1. Choose task mode
2. Speak the content for a task file
3. Receive verbatim transcription
4. Write the text into a `task.md` file in the active pane

### 5. Run a Safe Command by Voice
1. Choose command mode
2. Speak the desired action
3. LLM converts it into a plan
4. User reviews and confirms
5. Perth executes allowed terminal commands in the active pane

## Functional Requirements

### FR1 - Server Connection
Perth must connect to a configured zellij server and persist the connection details securely.

### FR2 - Session Browser
Perth must fetch and display available Zellij sessions and allow the user to open one.

### FR3 - Tab and Pane Navigation
Perth must represent tabs as mobile screens and allow swipe navigation across tabs and panes.

### FR4 - Active Pane Awareness
Perth must clearly indicate the active pane and target all voice/text actions to that pane.

### FR5 - Typed Input Fallback
Perth must allow direct typed input when voice is unavailable or undesirable.

### FR6 - Voice Mode Selection
Perth must provide explicit mode selection for command, transcription, and task modes.

### FR7 - Voice Capture
Perth must record speech with a clear start/stop control and microphone status.

### FR8 - Transcription Mode
Perth must transcribe speech verbatim and paste the text into the active terminal pane.

### FR9 - Task Mode
Perth must transcribe speech verbatim and write the result into a `task.md` file in the active pane.

### FR10 - Command Mode
Perth must interpret speech with an LLM, propose a plan, and execute terminal commands only after user confirmation.

### FR11 - Command Safety
Perth must block or require confirmation for destructive or ambiguous commands.

### FR12 - Error Handling
Perth must show clear errors for connection failures, transcription failures, and command execution failures.

### FR13 - Reconnect Handling
Perth must handle network drops and allow the user to reconnect to the same session state.

### FR14 - Settings
Perth must allow the user to configure server details, preferred voice provider, and behavior preferences.

### FR15 - Local Persistence
Perth must store recent sessions, preferences, and safe metadata locally.

## Non-Functional Requirements

### Performance
- Session data should load quickly enough to feel usable on mobile
- Voice-to-text results should appear with minimal delay
- UI navigation should remain smooth while session state updates

### Reliability
- The app should recover from transient network failures
- Voice capture should fail gracefully if permissions are missing
- Command mode should never execute without an explicit confirmation step

### Security
- API keys and sensitive settings must be stored securely
- Microphone access must use runtime permission gating
- Command execution must include confirmation and auditability

### Privacy
- Audio should be retained only when the user explicitly saves it
- Transcription content should stay local unless explicitly sent to a cloud provider

### Accessibility
- Voice mode controls must be readable and operable on small screens
- Important state changes must be visible without relying on audio alone

### Compatibility
- Android-only for MVP
- Compose-first UI architecture
- Supports modern Android permission and background behavior rules

## Success Criteria

### User Success
- A user can connect to a session and navigate panes without a desktop keyboard.
- A user can complete the transcription and task flows by voice.
- A user can safely use command mode with visible confirmation.

### Business Success
- The app demonstrates a workable mobile Zellij workflow in pilot testing.
- The three voice modes each complete an end-to-end flow.
- The product creates a clear productivity advantage for mobile terminal work.

### Technical Success
- The app uses a stable Android architecture with Compose, ViewModel, and repositories.
- Command mode is isolated behind a safe transport and confirmation layer.
- Voice capture works with Android permission and lifecycle constraints.
- Local state remains secure and recoverable.

### Measurable Outcomes
- Session connection succeeds reliably on supported networks.
- Users can complete all core workflows without restarting the app.
- Transcription and task-mode output preserve spoken content verbatim.
- Command mode never executes an unconfirmed destructive action.

## Product Scope

### MVP - Minimum Viable Product
- Android app shell
- Secure server connection
- Session list and session selection
- Tab/pane display and swipe navigation
- Typed input fallback
- Voice capture controls
- Transcription mode
- Task mode
- Safe command mode with confirmation
- Local settings and secure storage

### Growth Features (Post-MVP)
- Session search
- Recent session history
- Better offline caching
- More advanced voice provider switching
- Command templates and shortcuts
- More granular pane/session metadata

### Vision (Future)
- Multi-server support
- Advanced automation workflows
- Richer sync and state recovery
- Optional desktop companion integration

## Epics

### Epic 1 - Foundation and Session Sync
- Connect to zellij
- Sync sessions, tabs, and panes
- Maintain active pane state

### Epic 2 - Android Shell and Navigation
- Compose UI
- Swipe navigation
- Screen/state organization

### Epic 3 - Voice Capture and Transcription
- Mic permissions
- Voice capture lifecycle
- Verbatim transcription

### Epic 4 - Task Mode
- Verbatim output to `task.md`
- Active pane targeting

### Epic 5 - Command Mode and Safety
- LLM interpretation
- Planning UI
- Confirmation gate
- Command execution safety

### Epic 6 - Settings, Storage, and Security
- Preferences
- Secure storage
- Connection settings

### Epic 7 - Testing and Quality
- Unit tests
- Integration tests
- Android UI tests
- Release checks

## Key Risks and Open Questions

1. What is the exact zellij server contract?
2. Does Zellij control require a local bridge or daemon?
3. What is the safest command approval policy for MVP?
4. Which speech provider will be primary at launch?

## Requirements Traceability

| Requirement | Epic |
|-------------|------|
| FR1, FR2, FR3, FR4, FR13 | Epic 1 |
| FR3, FR5, FR6, FR14 | Epic 2 |
| FR7, FR8 | Epic 3 |
| FR9 | Epic 4 |
| FR10, FR11 | Epic 5 |
| FR12, FR14, FR15 | Epic 6 |
| NFRs, release quality | Epic 7 |

## Recommendation

Proceed with architecture after this PRD. The first architecture decision to resolve is the zellij transport contract, because it determines the session sync layer and command execution path.
