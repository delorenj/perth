---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - '/home/delorenj/code/perth/prd.md'
  - '/home/delorenj/code/perth/_bmad-output/planning-artifacts/architecture.md'
  - '/home/delorenj/code/perth/docs/bmad-workflow-plan-perth.md'
workflowType: 'epics'
project_name: 'perth'
user_name: 'Jarad'
date: '2026-04-01'
status: 'complete'
completedAt: '2026-04-01'
---

# Perth - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for Perth, decomposing the requirements from the PRD and Architecture into implementable stories organized by user value.

## Requirements Inventory

### Functional Requirements

- FR1: Perth must connect to a configured zellij server and persist the connection details securely.
- FR2: Perth must fetch and display available Zellij sessions and allow the user to open one.
- FR3: Perth must represent tabs as mobile screens and allow swipe navigation across tabs and panes.
- FR4: Perth must clearly indicate the active pane and target all voice/text actions to that pane.
- FR5: Perth must allow direct typed input when voice is unavailable or undesirable.
- FR6: Perth must provide explicit mode selection for command, transcription, and task modes.
- FR7: Perth must record speech with a clear start/stop control and microphone status.
- FR8: Perth must transcribe speech verbatim and paste the text into the active terminal pane.
- FR9: Perth must transcribe speech verbatim and write the result into a `task.md` file in the active pane.
- FR10: Perth must interpret speech with an LLM, propose a plan, and execute terminal commands only after user confirmation.
- FR11: Perth must block or require confirmation for destructive or ambiguous commands.
- FR12: Perth must show clear errors for connection failures, transcription failures, and command execution failures.
- FR13: Perth must handle network drops and allow the user to reconnect to the same session state.
- FR14: Perth must allow the user to configure server details, preferred voice provider, and behavior preferences.
- FR15: Perth must store recent sessions, preferences, and safe metadata locally.

### Non-Functional Requirements

- NFR1: Session data should load quickly enough to feel usable on mobile.
- NFR2: Voice-to-text results should appear with minimal delay.
- NFR3: UI navigation should remain smooth while session state updates.
- NFR4: The app should recover from transient network failures.
- NFR5: Voice capture should fail gracefully if permissions are missing.
- NFR6: Command mode should never execute without an explicit confirmation step.
- NFR7: API keys and sensitive settings must be stored securely (EncryptedSharedPreferences).
- NFR8: Microphone access must use runtime permission gating.
- NFR9: Command execution must include confirmation and auditability.
- NFR10: Audio should be retained only when the user explicitly saves it.
- NFR11: Transcription content should stay local unless explicitly sent to a cloud provider.
- NFR12: Voice mode controls must be readable and operable on small screens.
- NFR13: Important state changes must be visible without relying on audio alone.
- NFR14: Android-only for MVP, Compose-first UI architecture.

### Additional Requirements

- AR1: Architecture specifies Android Studio Compose Activity as starter template. Epic 1 Story 1 must scaffold the project.
- AR2: Hilt dependency injection must be set up in the project foundation.
- AR3: Navigation Compose with type-safe routes must be configured.
- AR4: ZellijTransport interface must be defined with MockZellijTransport for development.
- AR5: AppResult<T> sealed class must be used for all async operation returns.
- AR6: Room database, DataStore, and EncryptedSharedPreferences must be configured for local storage.
- AR7: Hybrid voice stack: ML Kit GenAI Speech (primary, on-device) + OpenAI Whisper API (fallback, cloud).
- AR8: CommandSafetyGate must classify and gate all commands before execution.
- AR9: Command audit log must record every command attempt with timestamp, transcript, decision, and result.
- AR10: Feature-based project structure with core shared module per architecture document.

### UX Design Requirements

No UX Design document was produced for Perth MVP. UX patterns are defined inline in the PRD and architecture (swipe navigation, voice mode selector, command confirmation dialog).

### FR Coverage Map

- FR1: Epic 1 - Server Connection (Story 1.2)
- FR2: Epic 1 - Session Browser (Story 1.3)
- FR3: Epic 2 - Tab/Pane Swipe Navigation (Story 2.1, 2.2)
- FR4: Epic 2 - Active Pane Awareness (Story 2.2)
- FR5: Epic 2 - Typed Input Fallback (Story 2.3)
- FR6: Epic 3 - Voice Mode Selection (Story 3.2)
- FR7: Epic 3 - Voice Capture (Story 3.1)
- FR8: Epic 3 - Transcription Mode (Story 3.3, 3.4)
- FR9: Epic 4 - Task Mode (Story 4.1)
- FR10: Epic 5 - Command Mode LLM Interpretation (Story 5.1, 5.2, 5.3)
- FR11: Epic 5 - Command Safety (Story 5.3, 5.4)
- FR12: Epic 6 - Error Handling (Story 6.1, 6.2)
- FR13: Epic 1 - Reconnect Handling (Story 1.4)
- FR14: Epic 1 - Settings (Story 1.5)
- FR15: Epic 1 - Local Persistence (Story 1.2, 1.3, 1.5)

## Epic List

### Epic 1: Connect and Browse Sessions
Users can connect to a zellij server, browse available Zellij sessions, select a session to open, and reconnect after network drops. This is the foundation that all other epics build upon.
**FRs covered:** FR1, FR2, FR13, FR14, FR15

### Epic 2: Navigate Terminal and Type Input
Users can view terminal tabs and panes on mobile, swipe between tabs, see which pane is active, and type input directly into the active pane.
**FRs covered:** FR3, FR4, FR5

### Epic 3: Voice Capture and Transcription Mode
Users can capture voice input, select a voice mode, transcribe speech to text, and paste verbatim transcription into the active terminal pane.
**FRs covered:** FR6, FR7, FR8

### Epic 4: Voice Task Mode
Users can speak and have their words transcribed verbatim into a `task.md` file written to the active pane's working directory.
**FRs covered:** FR9

### Epic 5: Voice Command Mode and Safety
Users can speak a desired action, have an LLM interpret it into a command plan, review and confirm the plan, and execute commands safely in the active pane.
**FRs covered:** FR10, FR11

### Epic 6: Error Resilience and Feedback
The app shows clear, actionable error messages for connection failures, voice capture issues, transcription errors, and command execution failures.
**FRs covered:** FR12

---

## Epic 1: Connect and Browse Sessions

Users can connect to a zellij server, browse available Zellij sessions, select a session to open, and reconnect after network drops. This epic establishes the project foundation, transport layer, and session management that all subsequent epics depend on.

### Story 1.1: Scaffold Project and Core Infrastructure

As a developer,
I want the Perth Android project initialized with Compose, Hilt, Navigation, and the core module structure,
So that all subsequent stories have a consistent foundation to build on.

**Acceptance Criteria:**

**Given** no existing Android project
**When** the project is created using Android Studio Compose Activity template
**Then** the project compiles and runs showing a blank Compose screen
**And** Hilt is configured with `@HiltAndroidApp` on PerthApp
**And** Navigation Compose is set up with a PerthNavHost and placeholder routes for SessionList, Terminal, and Settings
**And** the feature-based package structure matches the architecture document (`core/`, `feature/session/`, `feature/terminal/`, `feature/voice/`, `feature/command/`, `feature/settings/`)
**And** `AppResult<T>` and `AppException` sealed classes are defined in `core/result/`
**And** `ZellijTransport` interface is defined in `core/network/`
**And** `MockZellijTransport` returns hardcoded session data for development
**And** Room database, DataStore, and EncryptedSharedPreferences are configured in `core/data/`
**And** Timber is configured for structured logging
**And** the version catalog (`libs.versions.toml`) lists all dependencies with pinned versions
**And** a basic CI workflow (`ci.yml`) runs `./gradlew build` on push

### Story 1.2: Connect to Zellij Server

As a power user,
I want to enter my zellij server URL and connect to it,
So that I can access my Zellij sessions remotely from my phone.

**Acceptance Criteria:**

**Given** the app is open and no server is configured
**When** I navigate to Settings and enter a server URL
**Then** the URL is persisted in EncryptedSharedPreferences
**And** the app attempts a WebSocket connection to the server
**And** the connection state is displayed (Connecting, Connected, Error)
**And** on successful connection, the app navigates to the Session List screen
**And** on connection failure, an error message is shown with a retry option
**And** the `ServerConfig` model stores URL and optional auth token

### Story 1.3: Browse and Select Zellij Sessions

As a power user,
I want to see a list of my available Zellij sessions and select one to open,
So that I can resume work on a specific session from my phone.

**Acceptance Criteria:**

**Given** the app is connected to a zellij server
**When** the Session List screen loads
**Then** all available Zellij sessions are displayed with their names
**And** each session shows a summary (number of tabs, creation time if available)
**And** tapping a session navigates to the Terminal screen for that session
**And** the selected session is persisted locally via Room as a recent session
**And** if no sessions are available, an empty state message is shown
**And** pull-to-refresh fetches the latest session list from the server

### Story 1.4: Reconnect After Network Drop

As a mobile terminal user,
I want the app to detect network drops and reconnect automatically,
So that I do not lose my session when my connection is briefly interrupted.

**Acceptance Criteria:**

**Given** the app is connected to an active session
**When** the network connection drops
**Then** the connection state changes to Disconnected and a banner is shown
**And** the app attempts automatic reconnection with exponential backoff (1s, 2s, 4s, max 3 attempts)
**And** on successful reconnect, the session state is restored and the banner disappears
**And** on exhausted retries, an error message is shown with a manual Reconnect button
**And** the user can tap Reconnect to retry immediately
**And** during disconnection, the terminal display shows the last known pane state (cached)

### Story 1.5: Configure Server and Preferences

As a power user,
I want a settings screen where I can manage my server connection, voice preferences, and app behavior,
So that I can customize Perth to my workflow.

**Acceptance Criteria:**

**Given** the app is open
**When** I navigate to the Settings screen
**Then** I can view and edit the zellij server URL
**And** I can view the current connection status
**And** I can select the preferred voice provider (on-device or cloud)
**And** I can view recent sessions stored locally
**And** I can clear recent session history
**And** all settings are persisted via DataStore and survive app restarts
**And** sensitive values (server credentials, API keys) are stored in EncryptedSharedPreferences

---

## Epic 2: Navigate Terminal and Type Input

Users can view terminal output in a mobile-friendly layout, swipe between Zellij tabs, identify the active pane, and type text directly into the terminal when voice input is not desired.

### Story 2.1: Display Tabs with Swipe Navigation

As a power user,
I want to swipe left and right to navigate between Zellij tabs on my phone,
So that I can quickly move between different workspaces in my session.

**Acceptance Criteria:**

**Given** a session is selected and has multiple tabs
**When** the Terminal screen loads
**Then** the active tab's pane content is displayed
**And** a tab indicator bar shows all tab names with the current tab highlighted
**And** swiping left navigates to the next tab
**And** swiping right navigates to the previous tab
**And** tapping a tab name in the indicator bar navigates directly to that tab
**And** tab transitions are smooth with no visible jank
**And** the `HorizontalPager` component from Compose Foundation is used for swipe navigation

### Story 2.2: Display Panes with Active Pane Awareness

As a power user,
I want to see the panes within the current tab and clearly identify which pane is active,
So that I know where my voice and text input will be directed.

**Acceptance Criteria:**

**Given** a tab is displayed and contains one or more panes
**When** the tab renders
**Then** each pane shows its terminal output content
**And** the active pane has a visible border or highlight distinguishing it from other panes
**And** tapping a pane makes it the active pane
**And** the active pane ID is tracked in `TerminalViewModel` state
**And** all voice and text actions target the active pane
**And** pane output updates are received via the `paneOutputFlow` from `ZellijTransport`
**And** for single-pane tabs, that pane is automatically active with no selection needed

### Story 2.3: Typed Input to Active Pane

As a mobile terminal user,
I want to type text into the active pane using my phone keyboard,
So that I can interact with the terminal when voice input is inconvenient.

**Acceptance Criteria:**

**Given** a pane is active in the Terminal screen
**When** I tap the input area at the bottom of the screen
**Then** the on-screen keyboard appears
**And** text I type is visible in the input field
**And** pressing Enter sends the text to the active pane via `ZellijTransport.sendInput()`
**And** the input field clears after sending
**And** the sent text appears in the pane output (echoed by the terminal)
**And** the input field supports standard keyboard features (autocorrect can be toggled off in settings)

---

## Epic 3: Voice Capture and Transcription Mode

Users can activate voice input, select a voice interaction mode, speak into the microphone, and paste verbatim transcriptions directly into the active terminal pane.

### Story 3.1: Microphone Permission and Voice Capture

As a voice-first operator,
I want to start and stop voice recording with a clear button and see microphone status,
So that I know when the app is listening and can control my voice input.

**Acceptance Criteria:**

**Given** the user has not yet granted microphone permission
**When** the user taps the voice capture button for the first time
**Then** the Android runtime permission dialog is shown for RECORD_AUDIO
**And** if permission is granted, voice capture starts immediately
**And** if permission is denied, a message explains voice input requires microphone access and typed input remains available
**And** if permission was previously denied with "Don't ask again," a message directs the user to app settings

**Given** microphone permission is granted
**When** the user taps the voice capture button
**Then** a visible recording indicator (pulsing icon or animation) shows capture is active
**And** tapping the button again stops capture
**And** audio is captured as a stream suitable for the speech recognizer
**And** audio data is ephemeral and not persisted unless explicitly saved
**And** the `VoiceViewModel` tracks capture state (Idle, Recording, Processing)

### Story 3.2: Voice Mode Selection

As a voice-first operator,
I want to explicitly choose between transcription, task, and command modes before speaking,
So that the app processes my speech according to my intent.

**Acceptance Criteria:**

**Given** the Terminal screen is active with a session
**When** the voice control panel is visible
**Then** three mode buttons are displayed: Transcription, Task, and Command
**And** exactly one mode is selected at a time (default: Transcription)
**And** the selected mode is visually highlighted
**And** the selected mode persists via DataStore across app restarts
**And** the mode selector is accessible and labeled for screen readers
**And** switching modes while not recording takes effect immediately
**And** switching modes while recording stops the current capture first

### Story 3.3: Speech-to-Text Transcription

As a voice-first operator,
I want my speech transcribed to text accurately,
So that I can use voice as an input method for the terminal.

**Acceptance Criteria:**

**Given** voice capture is active and Transcription mode is selected
**When** I stop the recording
**Then** the audio is sent to the primary speech recognizer (ML Kit GenAI on-device)
**And** the transcribed text is displayed in a preview area before sending
**And** if the primary recognizer fails, the fallback recognizer (OpenAI Whisper API) is attempted
**And** if both recognizers fail, an error message is shown with the option to retry or type manually
**And** the transcription result is verbatim (no LLM interpretation)
**And** processing state is shown (a spinner or progress indicator)
**And** the `SpeechRecognizer` interface is used, allowing provider swapping via Hilt

### Story 3.4: Paste Transcription to Active Pane

As a voice-first operator,
I want to send my transcribed text to the active terminal pane,
So that I can control the terminal by voice without typing.

**Acceptance Criteria:**

**Given** a transcription result is displayed in the preview area
**When** I tap the Send button
**Then** the transcribed text is sent to the active pane via `ZellijTransport.sendInput()`
**And** the preview area clears
**And** the text appears in the pane output (echoed by the terminal)
**And** I can edit the transcription in the preview area before sending
**And** I can cancel the transcription to discard it
**And** if the send fails (network error), an error is shown with retry option

---

## Epic 4: Voice Task Mode

Users can speak and have their words transcribed verbatim into a `task.md` file, written to the working directory of the active pane.

### Story 4.1: Create Task File from Voice

As a voice-first operator,
I want to speak a task description and have it written as a `task.md` file in my active pane's directory,
So that I can quickly capture tasks without typing on my phone.

**Acceptance Criteria:**

**Given** Task mode is selected and voice capture is active
**When** I stop the recording
**Then** the audio is transcribed verbatim using the same speech recognizer as Transcription mode
**And** the transcribed text is displayed in a preview area
**And** a confirmation dialog shows: "Write this to task.md in the active pane?"
**And** if I confirm, the text is sent as a command to write `task.md` via `ZellijTransport.sendCommand()`
**And** the command uses a safe write operation (e.g., `cat > task.md << 'EOF'\n{text}\nEOF`)
**And** if a `task.md` already exists, the user is warned and can choose to overwrite or append
**And** on success, a confirmation message is shown
**And** on failure, an error message is shown with retry option
**And** I can edit the transcription before confirming

---

## Epic 5: Voice Command Mode and Safety

Users can speak a desired action in natural language, have an LLM interpret it into a command plan, review the plan, and execute approved commands safely in the active pane.

### Story 5.1: LLM Integration for Command Interpretation

As a power user,
I want to configure an LLM provider so command mode can interpret my voice input,
So that I can use natural language to control the terminal.

**Acceptance Criteria:**

**Given** the Settings screen
**When** I enter an OpenAI API key
**Then** the key is stored in EncryptedSharedPreferences
**And** the app validates the key by making a lightweight API call
**And** on valid key, a success indicator is shown
**And** on invalid key, an error message explains the issue
**And** the `LlmRepository` uses the configured key for all command mode requests
**And** the LLM provider is accessible via Hilt injection

### Story 5.2: Voice to Command Plan

As a power user,
I want to speak a desired action and see the LLM's interpretation as a command plan,
So that I can review what will be executed before it runs.

**Acceptance Criteria:**

**Given** Command mode is selected and voice capture completes
**When** the transcription is sent to the LLM
**Then** the LLM returns a structured command plan (JSON with steps, commands, and risk level)
**And** the plan is displayed in a readable format showing each step and its command
**And** each step shows a risk classification (safe, caution, destructive)
**And** if the LLM cannot interpret the request, a message says "Could not determine a command. Try rephrasing." with retry option
**And** if the LLM API call fails, an error is shown with retry and fallback to typed input
**And** the user can see the original transcription alongside the plan

### Story 5.3: Command Confirmation and Safety Gate

As a power user,
I want to explicitly approve or reject each command before it runs,
So that no destructive or unintended commands execute without my consent.

**Acceptance Criteria:**

**Given** a command plan is displayed
**When** the CommandSafetyGate classifies the commands
**Then** safe commands show a green indicator
**And** cautionary commands show a yellow indicator with a warning message
**And** destructive commands (rm, drop, kill, format, etc.) show a red indicator with an explicit warning
**And** I can approve individual steps or approve all
**And** I can reject individual steps or reject all
**And** rejected steps are removed from the execution plan
**And** no command executes without at least one explicit tap on an Approve button
**And** the confirmation dialog cannot be bypassed programmatically

### Story 5.4: Execute Approved Commands

As a power user,
I want approved commands to execute in the active pane and see results,
So that I can accomplish terminal tasks by voice.

**Acceptance Criteria:**

**Given** a command plan has been approved (partially or fully)
**When** I tap Execute
**Then** approved commands are sent sequentially to the active pane via `ZellijTransport.sendCommand()`
**And** the result of each command is shown (success or failure)
**And** if a command fails, execution stops and the user is asked whether to continue with remaining commands
**And** every command attempt is logged in the Room audit table with: timestamp, original transcript, command text, safety classification, user decision (approved/rejected), and execution result
**And** the audit log is viewable from Settings (future, not required in this story)
**And** the UI returns to the terminal view after execution completes

---

## Epic 6: Error Resilience and Feedback

The app provides clear, actionable error messages for all failure scenarios, ensuring users always understand what went wrong and how to recover.

### Story 6.1: Connection and Network Error Handling

As a mobile terminal user,
I want clear error messages when connection problems occur,
So that I can understand what happened and take action to fix it.

**Acceptance Criteria:**

**Given** the app encounters a connection error
**When** the error occurs (server unreachable, auth failure, timeout, WebSocket close)
**Then** a user-friendly error message is displayed (not a raw stack trace)
**And** the message categorizes the error (network, server, authentication)
**And** each error type offers specific recovery actions:
  - Network: "Check your connection and retry"
  - Server: "Server may be down. Try again later."
  - Auth: "Check your server credentials in Settings."
  - Timeout: "Connection timed out. Retry?"
**And** a Retry button is always available
**And** errors are logged via Timber for debugging
**And** the app never crashes from unhandled network exceptions

### Story 6.2: Voice and Command Error Handling

As a voice-first operator,
I want clear feedback when voice capture or command execution fails,
So that I can retry or switch to typed input.

**Acceptance Criteria:**

**Given** a voice or command operation fails
**When** the error occurs (mic unavailable, transcription failure, LLM error, command execution error)
**Then** each error type shows a specific message:
  - Mic unavailable: "Microphone not available. Check permissions." with link to settings
  - Transcription failure: "Could not transcribe audio. Tap to retry or type instead."
  - LLM error: "Command interpretation failed. Check API key or retry."
  - Command error: "Command failed: {error}. No further commands executed."
**And** all voice errors offer a "Type instead" fallback action
**And** all command errors show which step failed and which steps were not executed
**And** errors do not leave the app in an inconsistent state (recording stops, processing indicators clear)
**And** errors are logged with enough context for debugging
