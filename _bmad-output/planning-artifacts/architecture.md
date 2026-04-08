---
stepsCompleted: [1, 2, 3, 4, 5, 6, 7, 8]
inputDocuments:
  - '/home/delorenj/code/perth/prd.md'
  - '/home/delorenj/code/perth/technical-perth-voice-zellij-2026-03-29.md'
  - '/home/delorenj/code/perth/braindump.md'
  - '/home/delorenj/code/perth/docs/bmad-workflow-plan-perth.md'
workflowType: 'architecture'
project_name: 'perth'
user_name: 'Jarad'
date: '2026-04-01'
lastStep: 8
status: 'complete'
completedAt: '2026-04-01'
---

# Architecture Decision Document

_Perth - Voice-driven Android client for Zellij sessions._

---

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**

Perth defines 15 functional requirements (FR1-FR15) spanning five architectural domains:

1. **Server Connectivity (FR1, FR2, FR13):** Connect to a zellij server, fetch sessions, handle reconnection. This is the foundational transport layer and the biggest technical unknown.
2. **Navigation & Display (FR3, FR4, FR5):** Represent Zellij tabs as swipeable mobile screens, maintain active pane awareness, and provide typed input fallback.
3. **Voice Capture & Modes (FR6, FR7, FR8, FR9, FR10):** Three distinct voice modes (command, transcription, task) with shared capture infrastructure. Mode selection must be explicit.
4. **Command Safety (FR10, FR11):** LLM-driven command interpretation with mandatory confirmation gate. No destructive commands without user approval.
5. **Infrastructure (FR12, FR14, FR15):** Error handling, settings management, local persistence of sessions and preferences.

**Non-Functional Requirements:**

- **Performance:** Session data loads fast on mobile. Voice-to-text with minimal delay. Smooth UI during state updates.
- **Reliability:** Recover from transient network failures. Graceful mic permission failures. Command mode never executes without confirmation.
- **Security:** API keys stored securely. Mic access runtime-gated. Command execution auditable.
- **Privacy:** Audio retained only when user saves. Transcription stays local unless explicitly sent to cloud.
- **Accessibility:** Voice controls readable on small screens. State changes visible without relying on audio.
- **Compatibility:** Android-only MVP. Compose-first. Modern Android permission and background rules.

**Scale & Complexity:**

- Primary domain: Mobile app (Android-native)
- Complexity level: High (Level 3) - Android + Voice AI + LLM + Server Protocol
- Estimated architectural components: 12-15 major modules
- Estimated stories: 20-35 across 6-8 epics

### Technical Constraints & Dependencies

1. **Zellij server contract is unknown.** The Zellij IPC is Rust-based. Perth must abstract the transport layer so it can evolve as the zellij API solidifies.
2. **Android audio lifecycle.** Background audio is increasingly restricted on Android 14+. Voice capture must be foreground-initiated.
3. **Command safety is non-negotiable.** LLM-interpreted commands must always pass through a confirmation gate before execution.
4. **On-device vs cloud voice.** Hybrid approach needed: on-device for privacy and offline, cloud fallback for accuracy.

### Cross-Cutting Concerns Identified

- **Authentication:** Server connection, LLM API keys, secure storage
- **Error handling:** Network drops, voice capture failures, LLM errors, command execution failures
- **State management:** Active session, active tab/pane, voice mode, connection status
- **Lifecycle management:** Android activity/fragment lifecycle, foreground service for voice, reconnection
- **Observability:** Command audit log, error reporting, connection diagnostics

---

## Starter Template Evaluation

### Primary Technology Domain

**Android Native (Kotlin + Jetpack Compose)** based on PRD requirements. This is not a cross-platform project. The PRD explicitly scopes to Android-only for MVP, and the voice + terminal integration benefits from native platform access.

### Starter Options Considered

| Option | Description | Verdict |
|--------|-------------|---------|
| Android Studio New Project (Compose Activity) | Google's official Compose starter | Selected |
| Now in Android (NiA) | Google's reference architecture app | Too complex for starting point |
| Jetpack Compose Template (community) | Various community starters | Inconsistent quality |

### Selected Starter: Android Studio Compose Activity Template

**Rationale:** Google's official Compose Activity template provides the cleanest starting point. It includes Compose setup, Material 3 theming, and Gradle configuration without opinionated architecture. Perth's architecture is sufficiently custom (voice, WebSocket, Zellij protocol) that a heavier starter would create more conflict than value.

**Initialization Command:**

```bash
# Via Android Studio: File > New > New Project > Empty Compose Activity
# Project name: Perth
# Package: sh.delo.perth
# Min SDK: API 28 (Android 9)
# Build: Kotlin DSL (build.gradle.kts)
```

**Architectural Decisions Provided by Starter:**

- **Language & Runtime:** Kotlin 2.0+, JVM target 17
- **UI Framework:** Jetpack Compose with Material 3
- **Build Tooling:** Gradle 8.x with Kotlin DSL, AGP 8.x
- **Testing Framework:** JUnit (basic), Compose test rule (basic)
- **Code Organization:** Single-module with `src/main/java` and `src/androidTest/java`

**What the starter does NOT provide (Perth must add):**

- Multi-module project structure
- Dependency injection (Hilt)
- Navigation (Navigation Compose)
- Networking (OkHttp, Retrofit)
- Local storage (Room, DataStore)
- Voice capture infrastructure
- WebSocket transport layer

**Note:** Project initialization using this command should be the first implementation story.

---

## Core Architectural Decisions

### Decision Priority Analysis

**Critical Decisions (Block Implementation):**

1. Transport protocol for zellij server
2. Voice recognition stack selection
3. LLM provider for command mode
4. Android architecture pattern (MVVM layers)
5. Dependency injection framework

**Important Decisions (Shape Architecture):**

6. Navigation architecture
7. Local storage strategy
8. Testing framework stack
9. Error handling strategy
10. State management approach

**Deferred Decisions (Post-MVP):**

- Multi-server support
- Advanced offline caching
- Voice provider hot-swapping
- Command templates and shortcuts

### Data Architecture

**Local Storage:**

| Technology | Purpose | Rationale |
|------------|---------|-----------|
| Room 2.6.x | Session history, recent transcripts, command audit log | Structured relational data with migration support |
| DataStore (Preferences) | App settings, voice mode preference, last server | Key-value settings, type-safe, coroutine-native |
| EncryptedSharedPreferences | LLM API keys, server credentials | Android Keystore-backed encryption for secrets |

**Data Modeling Approach:**
- Room entities for persistent structured data (sessions, transcripts, audit log)
- DataStore for user preferences and app state
- In-memory StateFlow for transient UI state (active pane, connection status, voice capture state)
- No ORM beyond Room. Keep data layer thin.

**Migration Strategy:**
- Room auto-migrations for schema changes
- Fallback destructive migration only in development builds
- DataStore handles its own versioning

### Authentication & Security

**Server Authentication:**
- Connection to zellij via configurable server URL
- Auth mechanism TBD (depends on zellij contract). Architecture supports token-based auth via an `AuthInterceptor` on OkHttp.
- Credentials stored in EncryptedSharedPreferences

**LLM API Security:**
- API keys stored in EncryptedSharedPreferences
- Keys never logged or exposed in UI
- Network calls over HTTPS only

**Command Execution Security:**
- All commands pass through `CommandSafetyGate` before execution
- Destructive command detection via pattern matching + LLM classification
- Mandatory user confirmation dialog before any command runs
- Full audit log in Room database: timestamp, command, source transcript, user decision, result

**Microphone Permission:**
- Runtime permission request at point of use (not on app start)
- Graceful degradation to typed input if denied
- Permission rationale dialog shown on first denial

### API & Communication

**Zellij Transport (Critical Decision):**

Perth uses a **WebSocket-first transport adapter** with HTTP REST fallback.

```
┌──────────────┐     WebSocket      ┌──────────────┐
│  Perth App   │ ◄────────────────► │ Zellij Server│
│              │     (primary)      │              │
│  Transport   │                    │  Zellij IPC  │
│  Adapter     │ ◄── HTTP REST ──► │  Bridge      │
│              │     (fallback)     │              │
└──────────────┘                    └──────────────┘
```

**Transport Adapter Interface:**

```kotlin
interface ZellijTransport {
    suspend fun connect(config: ServerConfig): ConnectionResult
    suspend fun disconnect()
    fun sessionFlow(): Flow<List<ZellijSession>>
    fun paneOutputFlow(paneId: PaneId): Flow<PaneOutput>
    suspend fun sendInput(paneId: PaneId, input: String): Result<Unit>
    suspend fun sendCommand(paneId: PaneId, command: String): Result<CommandResult>
    val connectionState: StateFlow<ConnectionState>
}
```

This interface isolates the transport completely. Implementations can be:
- `WebSocketZellijTransport` (primary, real-time)
- `HttpZellijTransport` (fallback, polling)
- `MockZellijTransport` (testing)
- `CliZellijTransport` (future: local daemon bridge)

**Message Format:** JSON over WebSocket. Compact payloads. Binary/audio never sent through this channel.

**Error Handling Standards:**
- All network operations return `Result<T>` (Kotlin sealed type)
- Retry with exponential backoff for transient failures (max 3 retries)
- Connection state exposed as `StateFlow<ConnectionState>` (Connected, Connecting, Disconnected, Error)

### Frontend Architecture

**Architecture Pattern: MVVM + Clean Architecture Layers**

```
┌─────────────────────────────────────────────┐
│  Presentation Layer (Compose UI)            │
│  ├── Screens (SessionList, Terminal, Voice) │
│  ├── Components (PaneView, VoiceControl)    │
│  └── Theme (Material 3, Typography, Color)  │
├─────────────────────────────────────────────┤
│  State Layer (ViewModels + StateFlow)       │
│  ├── SessionViewModel                       │
│  ├── TerminalViewModel                      │
│  ├── VoiceViewModel                         │
│  └── CommandViewModel                       │
├─────────────────────────────────────────────┤
│  Domain Layer (Use Cases)                   │
│  ├── ConnectToSession                       │
│  ├── NavigatePane                           │
│  ├── CaptureVoice                           │
│  ├── TranscribeSpeech                       │
│  ├── InterpretCommand                       │
│  └── ExecuteCommand                         │
├─────────────────────────────────────────────┤
│  Data Layer (Repositories + Adapters)       │
│  ├── ZellijRepository (transport adapter)   │
│  ├── VoiceRepository (speech recognition)   │
│  ├── LlmRepository (command interpretation) │
│  ├── SessionRepository (Room + cache)       │
│  └── SettingsRepository (DataStore)         │
└─────────────────────────────────────────────┘
```

**State Management:**
- ViewModel + StateFlow as single source of truth per screen
- Unidirectional data flow: UI Event -> ViewModel -> Use Case -> Repository -> State Update -> Recomposition
- `rememberSaveable` for UI state that survives config changes
- No global state store. Each ViewModel owns its domain state.

**Navigation:**
- Navigation Compose with type-safe routes
- Top-level destinations: SessionList, Terminal (with tab pager), Settings
- Terminal screen uses `HorizontalPager` for tab swipe navigation
- Within each tab, pane selection via tap or vertical swipe

**Dependency Injection:**
- Hilt (Dagger) for compile-time DI
- `@HiltViewModel` for all ViewModels
- `@Singleton` for repositories and transport adapter
- Module bindings for interface implementations (transport, voice, LLM)

### Voice Architecture

**Hybrid Voice Stack:**

```
┌──────────────────────────────────────────────┐
│  Voice Capture Layer (Android AudioRecord)   │
│  └── Foreground-initiated, permission-gated  │
├──────────────────────────────────────────────┤
│  Speech Recognition Dispatcher               │
│  ├── Primary: ML Kit GenAI Speech (on-device)│
│  ├── Fallback: OpenAI Whisper API (cloud)    │
│  └── Interface: SpeechRecognizer             │
├──────────────────────────────────────────────┤
│  Mode Router                                 │
│  ├── Transcription Mode → paste to pane      │
│  ├── Task Mode → write task.md               │
│  └── Command Mode → LLM → safety → execute  │
└──────────────────────────────────────────────┘
```

**SpeechRecognizer Interface:**

```kotlin
interface SpeechRecognizer {
    suspend fun recognize(audio: AudioStream): Result<Transcript>
    val isAvailable: StateFlow<Boolean>
}
```

Implementations: `MlKitSpeechRecognizer`, `WhisperSpeechRecognizer`

**LLM Provider for Command Mode:**
- OpenAI API (GPT-4o-mini or equivalent) for command interpretation
- Structured output: command plan as JSON with steps, risk level, confirmation requirements
- Prompt templates per mode stored in resources
- Response validation before presenting to user

### Infrastructure & Deployment

**Distribution:**
- Google Play Store (primary)
- Direct APK (testing/beta)
- F-Droid (future, if open-sourced)

**CI/CD:**
- GitHub Actions for build, lint, test on every push
- Android App Bundle for release builds
- Automated signing via GitHub Secrets

**Monitoring:**
- Firebase Crashlytics for crash reporting (optional, can be deferred)
- Structured logging via Timber with custom tree for command audit
- No analytics in MVP

**Min SDK:** API 28 (Android 9.0) - covers 95%+ of active devices, required for EncryptedSharedPreferences

### Decision Impact Analysis

**Implementation Sequence:**

1. Project scaffold (Compose + Hilt + Navigation)
2. Transport adapter interface + mock implementation
3. Session list UI + ViewModel
4. Terminal display with tab pager
5. Voice capture infrastructure
6. Speech recognition (ML Kit primary)
7. Transcription mode (voice -> paste)
8. Task mode (voice -> task.md)
9. LLM integration (OpenAI)
10. Command mode (voice -> LLM -> safety -> execute)

**Cross-Component Dependencies:**

- Voice modes all depend on shared voice capture layer
- Command mode depends on both voice capture AND LLM integration
- All modes depend on transport adapter for pane targeting
- Settings affects voice provider selection, server config, and LLM keys

---

## Implementation Patterns & Consistency Rules

### Naming Patterns

**Kotlin Code Naming:**

| Element | Convention | Example |
|---------|-----------|---------|
| Package | lowercase, dot-separated | `sh.delo.perth.data.repository` |
| Class | PascalCase | `SessionViewModel` |
| Interface | PascalCase (no I prefix) | `ZellijTransport` |
| Function | camelCase | `connectToServer()` |
| Property | camelCase | `connectionState` |
| Constant | SCREAMING_SNAKE | `MAX_RETRY_COUNT` |
| Composable | PascalCase | `SessionListScreen` |
| State holder | camelCase with State suffix | `sessionListState` |
| Flow | camelCase | `sessionFlow` |
| StateFlow | camelCase with underscore-private pattern | `_state` / `state` |

**File Naming:**

| Element | Convention | Example |
|---------|-----------|---------|
| Kotlin source | PascalCase matching class | `SessionViewModel.kt` |
| Compose screen | PascalCase with Screen suffix | `SessionListScreen.kt` |
| Compose component | PascalCase | `VoiceControlPanel.kt` |
| Test | PascalCase with Test suffix | `SessionViewModelTest.kt` |
| Resource layout | Not applicable (Compose) | N/A |
| String resource | snake_case | `error_connection_failed` |

**Database Naming:**

| Element | Convention | Example |
|---------|-----------|---------|
| Table | snake_case, plural | `sessions`, `command_audit_logs` |
| Column | snake_case | `session_id`, `created_at` |
| Foreign key | referenced_table_id | `session_id` |
| Index | idx_table_column | `idx_sessions_server_url` |

**API/JSON Naming:**

| Element | Convention | Example |
|---------|-----------|---------|
| JSON field | snake_case | `session_id`, `pane_output` |
| WebSocket message type | dot.separated | `session.list`, `pane.output` |
| Event name | dot.separated.past_tense | `session.connected`, `command.executed` |

### Structure Patterns

**Module Organization:** Feature-based with shared core.

Each feature module contains:
```
feature-name/
├── data/           # Repository implementations, data sources
├── domain/         # Use cases, domain models, interfaces
└── ui/             # Composables, ViewModels, UI state
```

**Test Co-location:**
- Unit tests mirror source structure under `src/test/`
- Android tests (Espresso) under `src/androidTest/`
- Test utilities in `src/testFixtures/` (shared between test and androidTest)

### Format Patterns

**Result Type (all async operations):**

```kotlin
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val exception: AppException) : AppResult<Nothing>()
}
```

**AppException hierarchy:**

```kotlin
sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Network(message: String, cause: Throwable? = null) : AppException(message, cause)
    class Server(val code: Int, message: String) : AppException(message)
    class Voice(message: String, cause: Throwable? = null) : AppException(message, cause)
    class Command(message: String) : AppException(message)
    class Storage(message: String, cause: Throwable? = null) : AppException(message, cause)
}
```

**UI State Pattern:**

```kotlin
data class ScreenUiState(
    val isLoading: Boolean = false,
    val error: AppException? = null,
    val data: ScreenData? = null
)
```

Every screen ViewModel exposes a single `StateFlow<ScreenUiState>`.

### Communication Patterns

**ViewModel to Repository:** Direct suspend function calls via use cases. No event bus.

**Repository to ViewModel:** Flow emissions for live data (session state, pane output). Suspend return for one-shot operations.

**Cross-ViewModel Communication:** Navigation arguments or shared repository state. No ViewModel-to-ViewModel direct calls.

**Logging:**

```kotlin
Timber.d("Session connected: sessionId=%s", sessionId)  // Debug
Timber.w("Reconnect attempt %d of %d", attempt, maxRetries)  // Warning
Timber.e(exception, "Command execution failed: %s", command)  // Error
```

All logs use structured format with `key=value` pairs. No sensitive data in logs.

### Process Patterns

**Loading State:** Each ViewModel manages its own loading state via `UiState.isLoading`. No global loading indicator.

**Error Recovery:**
1. Network errors: Automatic retry with exponential backoff (1s, 2s, 4s, max 3)
2. Voice errors: Show error in UI, offer retry or fallback to typed input
3. LLM errors: Show error, offer retry or manual command entry
4. Storage errors: Log and show user-friendly message

**Command Execution Flow:**

```
Voice Input → Transcription → LLM Interpretation → Command Plan
    → Safety Classification → Confirmation Dialog → Execute → Audit Log
```

Every step is observable. Any step can fail and the flow stops with a user-visible error.

### Enforcement Guidelines

**All AI Agents MUST:**

1. Use the `AppResult<T>` sealed class for all async operation returns
2. Follow the ViewModel `_state`/`state` pattern for StateFlow exposure
3. Use Hilt `@Inject` for all dependencies, never manual instantiation
4. Place files in the correct feature module and layer package
5. Write unit tests for all ViewModels and use cases
6. Use `Timber` for logging, never `Log.d` directly
7. Return `AppResult.Error` instead of throwing exceptions in repositories

**Anti-Patterns to Avoid:**

- Direct Android framework calls from ViewModels (use repository abstractions)
- Mutable state exposed from ViewModels (always expose immutable StateFlow)
- Business logic in Composables (delegate to ViewModel)
- Hardcoded strings in UI (use string resources)
- Blocking calls on main thread (use `withContext(Dispatchers.IO)`)

---

## Project Structure & Boundaries

### Complete Project Directory Structure

```
perth/
├── README.md
├── build.gradle.kts                    # Root build config
├── settings.gradle.kts                 # Module declarations
├── gradle.properties                   # Gradle config
├── gradle/
│   ├── libs.versions.toml              # Version catalog
│   └── wrapper/
├── .github/
│   └── workflows/
│       ├── ci.yml                      # Build + test on push
│       └── release.yml                 # Release build + signing
├── .gitignore
├── .editorconfig
│
├── app/                                # Application module
│   ├── build.gradle.kts
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   ├── kotlin/sh/delo/perth/
│   │   │   │   ├── PerthApp.kt         # Application class (Hilt entry)
│   │   │   │   ├── MainActivity.kt     # Single activity
│   │   │   │   ├── navigation/
│   │   │   │   │   ├── PerthNavHost.kt
│   │   │   │   │   └── Route.kt        # Type-safe route definitions
│   │   │   │   │
│   │   │   │   ├── feature/
│   │   │   │   │   ├── session/
│   │   │   │   │   │   ├── ui/
│   │   │   │   │   │   │   ├── SessionListScreen.kt
│   │   │   │   │   │   │   ├── SessionListViewModel.kt
│   │   │   │   │   │   │   └── SessionListUiState.kt
│   │   │   │   │   │   ├── domain/
│   │   │   │   │   │   │   ├── ConnectToSessionUseCase.kt
│   │   │   │   │   │   │   └── GetSessionsUseCase.kt
│   │   │   │   │   │   └── data/
│   │   │   │   │   │       └── SessionRepositoryImpl.kt
│   │   │   │   │   │
│   │   │   │   │   ├── terminal/
│   │   │   │   │   │   ├── ui/
│   │   │   │   │   │   │   ├── TerminalScreen.kt
│   │   │   │   │   │   │   ├── TerminalViewModel.kt
│   │   │   │   │   │   │   ├── TerminalUiState.kt
│   │   │   │   │   │   │   ├── TabPager.kt
│   │   │   │   │   │   │   └── PaneView.kt
│   │   │   │   │   │   ├── domain/
│   │   │   │   │   │   │   ├── NavigatePaneUseCase.kt
│   │   │   │   │   │   │   └── SendInputUseCase.kt
│   │   │   │   │   │   └── data/
│   │   │   │   │   │       └── TerminalRepositoryImpl.kt
│   │   │   │   │   │
│   │   │   │   │   ├── voice/
│   │   │   │   │   │   ├── ui/
│   │   │   │   │   │   │   ├── VoiceControlPanel.kt
│   │   │   │   │   │   │   ├── VoiceModeSelector.kt
│   │   │   │   │   │   │   ├── VoiceViewModel.kt
│   │   │   │   │   │   │   └── VoiceUiState.kt
│   │   │   │   │   │   ├── domain/
│   │   │   │   │   │   │   ├── CaptureVoiceUseCase.kt
│   │   │   │   │   │   │   ├── TranscribeSpeechUseCase.kt
│   │   │   │   │   │   │   └── VoiceMode.kt
│   │   │   │   │   │   └── data/
│   │   │   │   │   │       ├── VoiceRepositoryImpl.kt
│   │   │   │   │   │       ├── MlKitSpeechRecognizer.kt
│   │   │   │   │   │       └── WhisperSpeechRecognizer.kt
│   │   │   │   │   │
│   │   │   │   │   ├── command/
│   │   │   │   │   │   ├── ui/
│   │   │   │   │   │   │   ├── CommandConfirmationDialog.kt
│   │   │   │   │   │   │   ├── CommandPlanView.kt
│   │   │   │   │   │   │   ├── CommandViewModel.kt
│   │   │   │   │   │   │   └── CommandUiState.kt
│   │   │   │   │   │   ├── domain/
│   │   │   │   │   │   │   ├── InterpretCommandUseCase.kt
│   │   │   │   │   │   │   ├── ExecuteCommandUseCase.kt
│   │   │   │   │   │   │   ├── CommandSafetyGate.kt
│   │   │   │   │   │   │   ├── CommandPlan.kt
│   │   │   │   │   │   │   └── SafetyClassification.kt
│   │   │   │   │   │   └── data/
│   │   │   │   │   │       ├── LlmRepositoryImpl.kt
│   │   │   │   │   │       └── CommandAuditRepositoryImpl.kt
│   │   │   │   │   │
│   │   │   │   │   └── settings/
│   │   │   │   │       ├── ui/
│   │   │   │   │       │   ├── SettingsScreen.kt
│   │   │   │   │       │   ├── SettingsViewModel.kt
│   │   │   │   │       │   └── SettingsUiState.kt
│   │   │   │   │       └── data/
│   │   │   │   │           └── SettingsRepositoryImpl.kt
│   │   │   │   │
│   │   │   │   ├── core/
│   │   │   │   │   ├── data/
│   │   │   │   │   │   ├── db/
│   │   │   │   │   │   │   ├── PerthDatabase.kt
│   │   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   │   ├── SessionDao.kt
│   │   │   │   │   │   │   │   └── CommandAuditDao.kt
│   │   │   │   │   │   │   └── entity/
│   │   │   │   │   │   │       ├── SessionEntity.kt
│   │   │   │   │   │   │       └── CommandAuditEntity.kt
│   │   │   │   │   │   ├── datastore/
│   │   │   │   │   │   │   └── UserPreferences.kt
│   │   │   │   │   │   └── secure/
│   │   │   │   │   │       └── SecureStorage.kt
│   │   │   │   │   │
│   │   │   │   │   ├── domain/
│   │   │   │   │   │   ├── model/
│   │   │   │   │   │   │   ├── ZellijSession.kt
│   │   │   │   │   │   │   ├── ZellijTab.kt
│   │   │   │   │   │   │   ├── ZellijPane.kt
│   │   │   │   │   │   │   ├── PaneId.kt
│   │   │   │   │   │   │   ├── PaneOutput.kt
│   │   │   │   │   │   │   ├── ServerConfig.kt
│   │   │   │   │   │   │   ├── ConnectionState.kt
│   │   │   │   │   │   │   └── Transcript.kt
│   │   │   │   │   │   └── repository/
│   │   │   │   │   │       ├── SessionRepository.kt
│   │   │   │   │   │       ├── VoiceRepository.kt
│   │   │   │   │   │       ├── LlmRepository.kt
│   │   │   │   │   │       ├── CommandAuditRepository.kt
│   │   │   │   │   │       └── SettingsRepository.kt
│   │   │   │   │   │
│   │   │   │   │   ├── network/
│   │   │   │   │   │   ├── ZellijTransport.kt          # Interface
│   │   │   │   │   │   ├── WebSocketZellijTransport.kt  # Primary impl
│   │   │   │   │   │   └── SpeechRecognizer.kt          # Interface
│   │   │   │   │   │
│   │   │   │   │   ├── result/
│   │   │   │   │   │   ├── AppResult.kt
│   │   │   │   │   │   └── AppException.kt
│   │   │   │   │   │
│   │   │   │   │   └── di/
│   │   │   │   │       ├── AppModule.kt
│   │   │   │   │       ├── NetworkModule.kt
│   │   │   │   │       ├── DatabaseModule.kt
│   │   │   │   │       ├── VoiceModule.kt
│   │   │   │   │       └── LlmModule.kt
│   │   │   │   │
│   │   │   │   └── ui/
│   │   │   │       └── theme/
│   │   │   │           ├── Theme.kt
│   │   │   │           ├── Color.kt
│   │   │   │           ├── Type.kt
│   │   │   │           └── Shape.kt
│   │   │   │
│   │   │   └── res/
│   │   │       ├── values/
│   │   │       │   ├── strings.xml
│   │   │       │   ├── colors.xml
│   │   │       │   └── themes.xml
│   │   │       ├── drawable/
│   │   │       └── mipmap/
│   │   │
│   │   ├── test/                       # JVM unit tests
│   │   │   └── kotlin/sh/delo/perth/
│   │   │       ├── feature/
│   │   │       │   ├── session/
│   │   │       │   │   └── SessionListViewModelTest.kt
│   │   │       │   ├── terminal/
│   │   │       │   │   └── TerminalViewModelTest.kt
│   │   │       │   ├── voice/
│   │   │       │   │   └── VoiceViewModelTest.kt
│   │   │       │   └── command/
│   │   │       │       ├── CommandViewModelTest.kt
│   │   │       │       └── CommandSafetyGateTest.kt
│   │   │       └── core/
│   │   │           └── network/
│   │   │               └── WebSocketZellijTransportTest.kt
│   │   │
│   │   ├── androidTest/                # Instrumented tests
│   │   │   └── kotlin/sh/delo/perth/
│   │   │       ├── feature/
│   │   │       │   ├── session/
│   │   │       │   │   └── SessionListScreenTest.kt
│   │   │       │   └── terminal/
│   │   │       │       └── TerminalScreenTest.kt
│   │   │       └── core/
│   │   │           └── data/
│   │   │               └── db/
│   │   │                   └── PerthDatabaseTest.kt
│   │   │
│   │   └── testFixtures/               # Shared test utilities
│   │       └── kotlin/sh/delo/perth/
│   │           ├── FakeZellijTransport.kt
│   │           ├── FakeSpeechRecognizer.kt
│   │           └── TestData.kt
│
├── _bmad/                              # BMAD methodology (not shipped)
├── _bmad-output/                       # BMAD artifacts (not shipped)
├── docs/                               # Project documentation
└── design-artifacts/                   # Design files
```

### Architectural Boundaries

**API Boundaries:**

| Boundary | Interface | Implementation |
|----------|-----------|----------------|
| Zellij Server | `ZellijTransport` | `WebSocketZellijTransport` |
| Speech Recognition | `SpeechRecognizer` | `MlKitSpeechRecognizer`, `WhisperSpeechRecognizer` |
| LLM Provider | `LlmRepository` | `LlmRepositoryImpl` (OpenAI) |
| Local Storage | `SessionRepository`, etc. | Room + DataStore implementations |

**Component Boundaries:**

- Each feature (`session`, `terminal`, `voice`, `command`, `settings`) is self-contained
- Features communicate only through `core/domain/model` shared types
- No direct feature-to-feature imports
- `core/` provides shared infrastructure (database, network, DI, result types)

**Data Boundaries:**

- Repositories are the sole data access point for ViewModels (via use cases)
- Room database is accessed only through DAOs, never raw queries from features
- Network calls go through the transport adapter interface, never OkHttp directly from features
- Secure storage is accessed only through `SecureStorage` wrapper

### Requirements to Structure Mapping

**Epic: Foundation & Session Sync (Epic 1)**
- Transport: `core/network/ZellijTransport.kt`, `WebSocketZellijTransport.kt`
- Session: `feature/session/` (all layers)
- Models: `core/domain/model/ZellijSession.kt`, `ZellijTab.kt`, `ZellijPane.kt`
- Tests: `test/core/network/`, `test/feature/session/`

**Epic: Android Shell & Navigation (Epic 2)**
- Navigation: `navigation/PerthNavHost.kt`, `Route.kt`
- Terminal: `feature/terminal/ui/TerminalScreen.kt`, `TabPager.kt`, `PaneView.kt`
- Theme: `ui/theme/`
- Tests: `androidTest/feature/terminal/`

**Epic: Voice Capture & Transcription (Epic 3)**
- Voice: `feature/voice/` (all layers)
- Recognizers: `feature/voice/data/MlKitSpeechRecognizer.kt`, `WhisperSpeechRecognizer.kt`
- Interface: `core/network/SpeechRecognizer.kt`
- Tests: `test/feature/voice/`

**Epic: Task Mode (Epic 4)**
- Shares voice infrastructure from Epic 3
- Task-specific logic in `VoiceViewModel` via `VoiceMode.Task`

**Epic: Command Mode & Safety (Epic 5)**
- Command: `feature/command/` (all layers)
- Safety: `feature/command/domain/CommandSafetyGate.kt`
- LLM: `feature/command/data/LlmRepositoryImpl.kt`
- Audit: `core/data/db/dao/CommandAuditDao.kt`
- Tests: `test/feature/command/CommandSafetyGateTest.kt`

**Epic: Settings, Storage & Security (Epic 6)**
- Settings: `feature/settings/`
- Database: `core/data/db/`
- Secure: `core/data/secure/SecureStorage.kt`
- Preferences: `core/data/datastore/UserPreferences.kt`

### Data Flow

```
┌─────────┐    WebSocket     ┌──────────┐
│ Zellij   │ ◄─────────────► │ Transport│
│ Server   │    JSON msgs     │ Adapter  │
└─────────┘                  └────┬─────┘
                                  │ Flow<PaneOutput>
                                  ▼
                           ┌──────────────┐
                           │  Repository  │
                           │  (caches +   │
                           │   Room)      │
                           └──────┬───────┘
                                  │ Flow<UiState>
                                  ▼
                           ┌──────────────┐
                           │  ViewModel   │
                           │  (StateFlow) │
                           └──────┬───────┘
                                  │ State
                                  ▼
                           ┌──────────────┐
                           │  Compose UI  │
                           │  (renders)   │
                           └──────────────┘
```

---

## Architecture Validation Results

### Coherence Validation

**Decision Compatibility:**
All technology choices are compatible. Kotlin 2.0+ with Compose, Hilt, Room, DataStore, OkHttp, and Navigation Compose are all part of the standard Android Jetpack ecosystem. No version conflicts expected.

**Pattern Consistency:**
- Naming conventions are consistent: camelCase for Kotlin code, snake_case for database and JSON, PascalCase for files/classes
- All async operations use coroutines + Flow
- All data access goes through repository interfaces
- All state exposure uses StateFlow

**Structure Alignment:**
- Feature-based organization maps cleanly to epics
- Core module provides shared infrastructure without circular dependencies
- Test structure mirrors source structure

### Requirements Coverage Validation

| Requirement | Architectural Support | Status |
|-------------|----------------------|--------|
| FR1 - Server Connection | `ZellijTransport` interface + `WebSocketZellijTransport` | Covered |
| FR2 - Session Browser | `SessionRepository` + `SessionListScreen` | Covered |
| FR3 - Tab/Pane Navigation | `TabPager` + `HorizontalPager` + swipe gestures | Covered |
| FR4 - Active Pane Awareness | `TerminalViewModel` state + `PaneId` targeting | Covered |
| FR5 - Typed Input Fallback | `SendInputUseCase` + text field in terminal UI | Covered |
| FR6 - Voice Mode Selection | `VoiceModeSelector` + `VoiceMode` enum | Covered |
| FR7 - Voice Capture | `CaptureVoiceUseCase` + Android AudioRecord | Covered |
| FR8 - Transcription Mode | `TranscribeSpeechUseCase` + paste to pane | Covered |
| FR9 - Task Mode | `TranscribeSpeechUseCase` + write task.md | Covered |
| FR10 - Command Mode | `InterpretCommandUseCase` + `ExecuteCommandUseCase` | Covered |
| FR11 - Command Safety | `CommandSafetyGate` + confirmation dialog | Covered |
| FR12 - Error Handling | `AppResult` + `AppException` hierarchy | Covered |
| FR13 - Reconnect Handling | `ConnectionState` + auto-reconnect in transport | Covered |
| FR14 - Settings | `SettingsRepository` + `SettingsScreen` | Covered |
| FR15 - Local Persistence | Room + DataStore + EncryptedSharedPreferences | Covered |

**NFR Coverage:**
- Performance: Coroutines, incremental pane updates, on-device speech
- Reliability: Auto-reconnect, graceful permission handling, confirmation gates
- Security: EncryptedSharedPreferences, runtime permissions, command audit log
- Privacy: On-device speech primary, audio ephemeral by default
- Accessibility: Material 3 components with built-in accessibility support

### Implementation Readiness Validation

**Decision Completeness:** All critical decisions documented with rationale. Transport adapter interface defined. Voice stack selected with fallback strategy.

**Structure Completeness:** Full project tree with every file and directory specified. Feature modules mapped to epics.

**Pattern Completeness:** Naming, structure, format, communication, and process patterns all defined with examples and anti-patterns.

### Gap Analysis Results

**Known Gaps (Acceptable for MVP):**

1. **Zellij server contract details.** Mitigated by transport adapter interface. The `ZellijTransport` interface can be implemented against whatever protocol zellij exposes.
2. **Terminal rendering fidelity.** How to render ANSI terminal output in Compose is a detailed implementation concern, not an architectural gap. A `PaneView` composable will handle this.
3. **LLM prompt templates.** Specific prompt engineering for command interpretation is an implementation detail. The architecture supports it via `LlmRepository`.

**No Critical Gaps.** Architecture is ready for implementation.

### Architecture Completeness Checklist

**Requirements Analysis**
- [x] Project context thoroughly analyzed (15 FRs, 6 NFR categories)
- [x] Scale and complexity assessed (Level 3, High)
- [x] Technical constraints identified (zellij unknown, audio lifecycle, command safety)
- [x] Cross-cutting concerns mapped (auth, error handling, state, lifecycle, observability)

**Architectural Decisions**
- [x] Critical decisions documented with rationale
- [x] Technology stack fully specified (Kotlin, Compose, Hilt, Room, OkHttp, ML Kit, OpenAI)
- [x] Integration patterns defined (transport adapter, speech recognizer interface)
- [x] Performance considerations addressed (on-device speech, coroutines, incremental updates)

**Implementation Patterns**
- [x] Naming conventions established (Kotlin, DB, JSON, files)
- [x] Structure patterns defined (feature-based, layered)
- [x] Communication patterns specified (StateFlow, unidirectional, no event bus)
- [x] Process patterns documented (error handling, loading states, command flow)

**Project Structure**
- [x] Complete directory structure defined
- [x] Component boundaries established (feature isolation, core shared infra)
- [x] Integration points mapped (transport, voice, LLM interfaces)
- [x] Requirements to structure mapping complete (all epics mapped)

### Architecture Readiness Assessment

**Overall Status:** READY FOR IMPLEMENTATION

**Confidence Level:** High

**Key Strengths:**
- Transport adapter isolates the biggest unknown (zellij protocol) behind a clean interface
- Hybrid voice stack provides on-device privacy with cloud accuracy fallback
- Command safety gate is architecturally enforced, not optional
- Feature-based project structure maps directly to epics for clean parallel development
- Standard Android Jetpack stack minimizes integration risk

**Areas for Future Enhancement:**
- Multi-server support (post-MVP)
- Voice provider hot-swapping UI
- Offline session caching and sync
- Command template library
- Advanced terminal rendering (rich ANSI support)

### Implementation Handoff

**AI Agent Guidelines:**
- Follow all architectural decisions exactly as documented
- Use implementation patterns consistently across all components
- Respect project structure and feature module boundaries
- Refer to this document for all architectural questions
- When in doubt about zellij transport details, implement against `MockZellijTransport` first

**First Implementation Priority:**
1. Initialize project with Android Studio Compose Activity template
2. Set up Hilt, Navigation Compose, and multi-module structure
3. Define `ZellijTransport` interface and `MockZellijTransport`
4. Build `SessionListScreen` against mock data
