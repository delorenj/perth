---
stepsCompleted: [1, 2, 3, 4]
inputDocuments: []
workflowType: 'research'
lastStep: 4
research_type: 'technical'
research_topic: 'Perth - Voice-driven Mobile Zellij Client'
research_goals: 'Investigate zealot server API, voice recognition stack, mobile framework options, and LLM integration approaches for Android app development'
user_name: 'Jarad'
date: '2026-03-29'
web_research_enabled: true
source_verification: true
---

# Research Report: Technical

**Date:** 2026-03-29
**Author:** Jarad
**Research Type:** Technical

---

## Research Overview

[Research overview and methodology will be appended here]

---

## Technical Research Scope Confirmation

**Research Topic:** Perth - Voice-driven Mobile Zellij Client
**Research Goals:** Investigate zealot server API, voice recognition stack, mobile framework options, and LLM integration approaches for Android app development

**Technical Research Scope:**

- Architecture Analysis - design patterns, frameworks, system architecture
- Implementation Approaches - development methodologies, coding patterns
- Technology Stack - languages, frameworks, tools, platforms
- Integration Patterns - APIs, protocols, interoperability
- Performance Considerations - scalability, optimization, patterns

**Research Methodology:**

- Current web data with rigorous source verification
- Multi-source validation for critical technical claims
- Confidence level framework for uncertain information
- Comprehensive technical coverage with architecture-specific insights

**Scope Confirmed:** 2026-03-29

---

<!-- Content will be appended sequentially through research workflow steps -->

---

## Technology Stack Analysis

### Programming Languages

**Kotlin** is the dominant language for Android development in 2026. The shift from Java is complete, with Kotlin being the official preferred language by Google.

_Key Characteristics:_
- Null safety built into the type system
- Coroutines for asynchronous programming (critical for network I/O)
- Extension functions enable clean Compose integration
- 100% interoperable with Java

_For Perth:_ Kotlin is the clear choice. Coroutines are essential for handling WebSocket connections to zealot server and voice processing pipelines.

_Source:_ [Bitcot - Native Android Development with Jetpack Compose and Kotlin 2026](https://www.bitcot.com/native-android-development-with-jetpack-compose-and-kotlin/), [Technotalkative - Modern Android Development 2026](https://technotalkative.com/modern-android-development-guide/)

---

### Development Frameworks and Libraries

#### Jetpack Compose (UI Framework)

Jetpack Compose is the 2026 industry standard for Android UI, reducing UI code by up to 50% compared to XML-based layouts.

_Major Features in 2026:_
- Kotlin 2.2 integration with improved compilation times
- AI-assisted UI generation capabilities
- Enhanced state management with stable IDs
- Material Design 3 components built-in

_State Management for Large Apps (2026):_
- ViewModel + StateFlow is the dominant pattern
- Remember/rememberSaveable for UI state
- Navigation Compose for screen management

_For Perth:_ Compose is essential for:
- Swipe navigation between tabs/panes
- Voice mode selector UI
- Real-time terminal rendering

_Source:_ [AndroidLab - Jetpack Compose in 2026](https://medium.com/@androidlab/jetpack-compose-in-2026-everything-you-need-to-know-8975d48ad2a0), [Abi Farhan - State Management in Compose Apps 2026](https://medium.com/@abifarhan/state-management-strategies-in-large-scale-compose-apps-the-2026-complete-guide-de51b324749b)

#### Voice Recognition Stack Options

| Approach | Provider | Pros | Cons | Latency |
|----------|----------|------|------|---------|
| **Native** | Android SpeechRecognizer | Works offline, no API cost | Limited accuracy | ~200ms |
| **ML Kit** | Google GenAI Speech | High accuracy, on-device option | Alpha status | ~150ms |
| **Cloud** | OpenAI Whisper API | Highest accuracy | Requires internet, API cost | ~500ms |
| **Local** | Whisper Android port | Privacy, offline | Battery intensive | ~300ms |

_For Perth (Recommendation):_
- **Primary:** Google ML Kit GenAI Speech Recognition (on-device mode for privacy, cloud fallback)
- **Fallback:** OpenAI Whisper API for higher accuracy when needed
- **Local option:** Whisper Android for complete offline capability

_Source:_ [Google ML Kit - GenAI Speech Recognition](https://developers.google.com/ml-kit/genai/speech-recognition/android), [Android Voice Transcription Guide 2026](https://zackproser.com/blog/android-voice-transcription-guide-2026), [Whisper Android on Google Play](https://play.google.com/store/apps/details?hl=en_US&id=com.signalmaster.asr)

#### Zellij/Server Communication

_Zellij Components:_
- **Plugin API** - Extensible plugin system (Zellij 0.15+, 2026)
- **Programmatic Control** - API for external control
- **IPC (Inter-Process Communication)** - Client-server communication via Rust IPC

_For Perth:_ Need to investigate whether zealot (the Zellij session server) exposes a WebSocket or HTTP API. The Zellij IPC is Rust-based, so a Kotlin wrapper would need to be built.

_Source:_ [Zellij Plugin API Documentation](https://zellij.dev/documentation/plugin-api.html), [Zellij Programmatic Control](https://zellij.dev/documentation/programmatic-control.html), [Zellij IPC Source](https://docs.rs/zellij-utils/latest/src/zellij_utils/ipc.rs.html), [Zellij Plugin Development Guide](https://dasroot.net/posts/2026/03/developing-plugins-for-zellij-comprehensive-guide/)

---

### Database and Storage Technologies

**Local Storage Options for Perth:**

| Technology | Use Case | Suitability |
|------------|----------|-------------|
| **Room** | Session history, preferences | ✅ Recommended |
| **DataStore** | User settings, API keys | ✅ Recommended |
| **EncryptedSharedPreferences** | LLM API keys | ✅ Required for security |

_For Perth:_
- Room for session history and task templates
- DataStore for app preferences
- EncryptedSharedPreferences for storing LLM API credentials securely

_Source:_ [Android best practices documentation](https://developer.android.com/topic/libraries/architecture/data-store)

---

### Development Tools and Platforms

#### IDE and Build Tools
- **Android Studio Ladybug** (2024.2+) - Latest stable IDE
- **Kotlin 2.0+** - Required for Compose performance
- **Gradle 8.x** - Build system
- **AGP 8.x** - Android Gradle Plugin

#### Version Control
- **Git** - Standard
- **GitHub/Gitea** - For private repos

#### Testing Frameworks
- **JUnit 5** - Unit testing
- **MockK** - Kotlin mocking
- **Espresso** - UI testing (Android-native)
- **Robolectric** - Unit testing Android components

_For Perth:_ No Playwright/Cypress - this is Android-native, not web.

_Source:_ [Android Developer Documentation](https://developer.android.com/)

---

### Cloud Infrastructure and Deployment

**Perth is a client-only app** - no server infrastructure required.

_Deployment Targets:_
- **Google Play Store** - Primary distribution
- **F-Droid** - Open source alternative
- **Direct APK** - For testing/beta

_Optional Backend Services:_
- **Firebase** - Analytics, crash reporting, Cloud Messaging
- **Supabase** - If backend needed for sync

_For Perth:_ Likely no backend needed. Voice processing happens on-device or via third-party APIs (Whisper, OpenAI).

---

### Technology Adoption Trends

#### Mobile Development (2026)
- Declarative UI (Compose) is now 95%+ adoption for new projects
- AI-assisted development tools becoming standard
- On-device ML for privacy-sensitive features (voice, camera)

#### Voice Technology
- Local/on-device transcription gaining momentum (privacy, offline)
- Whisper-based solutions leading accuracy benchmarks
- Hybrid approaches (on-device + cloud fallback) becoming standard

#### Terminal Multiplexers
- Zellij gaining market share from tmux
- Plugin ecosystems expanding
- Remote session management becoming cloud-native

_Source:_ [Jetpack Compose 2026 Guide](https://technotalkative.com/modern-android-development-guide/), [Android Voice Transcription 2026](https://zackproser.com/blog/android-voice-transcription-guide-2026)

---

### Technology Stack Recommendation Summary

| Component | Recommended Technology | Confidence |
|-----------|----------------------|------------|
| Language | Kotlin 2.0+ | High |
| UI Framework | Jetpack Compose | High |
| State Management | ViewModel + StateFlow | High |
| Voice (Primary) | Google ML Kit GenAI Speech | Medium |
| Voice (Fallback) | OpenAI Whisper API | High |
| Local Voice | Whisper Android | Medium |
| Server Comm | WebSocket/HTTP (investigate zealot) | Low |
| Local Storage | Room + DataStore | High |
| Secure Storage | EncryptedSharedPreferences | High |
| Testing | JUnit + MockK + Espresso | High |
| Distribution | Google Play Store | High |

---

**Key Research Gaps Identified:**
1. ⚠️ Zealot server API contract - is there a documented API?
2. ⚠️ Zellij IPC protocol - how to communicate from Kotlin to Rust IPC?
3. ⚠️ Real-time session sync mechanism - WebSocket or polling?
4. ⚠️ Command execution security - how to send commands safely?

---

## Architectural Patterns and Design

### System Architecture Patterns

Perth should use a **layered, state-driven Android architecture**:

- **Presentation layer**: Jetpack Compose screens and navigation
- **State layer**: ViewModel + StateFlow as the source of truth
- **Domain layer**: use-cases for session selection, mode switching, transcription, and command execution
- **Data layer**: repositories/adapters for zealot, voice recognition, local cache, and LLM providers

Compose is explicitly state-driven and immutable at the UI level, so Perth should hoist state into ViewModels and keep composables focused on rendering and event emission. Save/restore UI state with Compose state APIs where needed.

_Source:_ [Compose UI Architecture](https://developer.android.com/develop/ui/compose/architecture), [Save UI state in Compose](https://developer.android.com/develop/ui/compose/state-saving), [Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle)

### Design Principles and Best Practices

- **Unidirectional data flow**: user action → ViewModel/use-case → state update → UI recomposition
- **Single source of truth**: session state, active pane, and active voice mode should each have one authoritative owner
- **Adapter boundary for zealot**: isolate the server contract behind an interface so the app can swap between WebSocket, HTTP, CLI bridge, or local daemon later
- **Command safety gate**: command mode should never execute destructive actions without explicit confirmation
- **Ephemeral audio handling**: keep audio buffers short-lived unless user explicitly saves them

_Source:_ [Compose UI Architecture](https://developer.android.com/develop/ui/compose/architecture), [Programmatic Control - Zellij User Guide](https://zellij.dev/documentation/programmatic-control.html)

### Scalability and Performance Patterns

- Use **coroutines + structured concurrency** for voice capture, transcription, and network I/O
- Prefer a **persistent live channel** for session state if zealot supports it; fall back to polling/delta sync only if needed
- Cache recent session snapshots locally to reduce redraws and reconnect churn
- Keep terminal rendering incremental rather than reloading full panes when possible

_Source:_ [Compose UI Architecture](https://developer.android.com/develop/ui/compose/architecture), [Zellij Programmatic Control](https://zellij.dev/documentation/programmatic-control.html)

### Integration and Communication Patterns

Zellij already exposes **programmatic control** and CLI-based control paths, so Perth should treat zealot integration as a transport problem first, not a UI problem. A small gateway layer can normalize whichever protocol becomes available:

- **If zealot exposes an API**: use a transport adapter (likely HTTP/WebSocket)
- **If zealot is CLI-driven**: use a local bridge/daemon and invoke Zellij control commands
- **If Zellij IPC is required**: implement a Kotlin-native client or thin Rust bridge

For data exchange, prefer compact JSON for control messages and keep binary/audio payloads out of the core UI layer.

_Source:_ [Zellij Programmatic Control](https://zellij.dev/documentation/programmatic-control.html), [Controlling Zellij through the CLI](https://zellij.dev/documentation/controlling-zellij-through-cli.html), [Zellij Plugin API](https://zellij.dev/documentation/plugin-api.html), [Zellij IPC Source](https://docs.rs/zellij-utils/latest/src/zellij_utils/ipc.rs.html)

### Security Architecture Patterns

- Request **runtime microphone permission** only when the user starts voice capture
- Treat voice capture as **foreground-initiated**; Android background audio behavior is increasingly restricted
- Store credentials with Android secure storage APIs rather than plaintext prefs
- Protect command execution with explicit confirmations, allow/deny rules, and audit logging

_Source:_ [Request runtime permissions](https://developer.android.com/training/permissions/requesting), [Background audio hardening](https://developer.android.com/about/versions/17/changes/bg-audio), [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences), [Foreground service timeouts](https://developer.android.com/develop/background-work/services/fgs/timeout)

### Data Architecture Patterns

- **Room** for session history, recent transcripts, and local metadata
- **DataStore** for user settings like voice mode, preferred provider, and last-connected server
- **Secure storage** for API keys and secrets
- Keep raw audio and transcripts ephemeral unless persistence is explicitly requested

_Source:_ [DataStore](https://developer.android.com/topic/libraries/architecture/data-store), [EncryptedSharedPreferences](https://developer.android.com/reference/androidx/security/crypto/EncryptedSharedPreferences)

### Deployment and Operations Architecture

Perth is primarily a **client-only Android app**. Operationally, the app should be distributed via Play Store, F-Droid, or APK for testing. Backend services are optional and should only exist if needed for sync, analytics, or crash reporting.

_Source:_ [Core app quality guidelines](https://developer.android.com/docs/quality-guidelines/archive/core/core-app-quality-2026-03-20)

### Architecture Recommendations for Perth

1. Build the app as a layered Android client with Compose + ViewModel + repositories.
2. Wrap zealot integration behind a transport adapter so the API shape can evolve.
3. Keep voice capture foreground-initiated and permission-gated.
4. Treat command mode as a high-risk workflow with confirmation and logging.
5. Make local persistence minimal, secure, and mostly metadata-focused.

### Remaining Architecture Research Gaps

1. ⚠️ Exact zealot server contract is still unknown.
2. ⚠️ Need confirmation whether a local daemon is required for Zellij control.
3. ⚠️ Need final decision on bidirectional live sync transport.
4. ⚠️ Need explicit command safety policy before implementation.

---

## Implementation Approaches and Technology Adoption

### Technology Adoption Strategies

Perth should adopt a **greenfield, compose-first, hybrid voice stack** strategy:

- **Build new UI in Jetpack Compose** rather than migrating legacy XML
- **Start with Kotlin + coroutines** as the base async model
- **Use on-device speech first**, with cloud fallback for accuracy or unsupported devices
- **Isolate zealot integration** behind a transport abstraction so the protocol can evolve

Android’s migration guidance shows Compose can interoperate with Views, but Perth does not need a staged migration because it is a new app. The app should still follow Compose’s state-hoisting model and performance practices.

_Source:_ [Set up continuous integration](https://developer.android.com/studio/projects/continuous-integration), [Migrate existing View-based apps](https://developer.android.com/develop/ui/compose/migrate), [Compose UI Architecture](https://developer.android.com/develop/ui/compose/architecture), [Compose Performance](https://developer.android.com/develop/ui/compose/performance)

### Development Workflows and Tooling

Recommended workflow:

- **Android Studio + Gradle** for development and builds
- **CI pipeline** that runs lint, unit tests, and device/UI tests on every change
- **Android App Bundle** for distribution and Play Store release testing
- **Compose performance checks** to keep rendering fast and avoid excessive recomposition

The Android docs explicitly recommend CI systems that build and test on every check-in, and app bundles are the recommended distribution format for broad device support.

_Source:_ [Set up continuous integration](https://developer.android.com/studio/projects/continuous-integration), [Build and test your Android App Bundle](https://developer.android.com/guide/app-bundle/test), [Compose Performance](https://developer.android.com/develop/ui/compose/performance)

### Testing and Quality Assurance

Testing stack for Perth:

- **JUnit + MockK** for unit tests
- **Robolectric** for Android component tests without a full device
- **Espresso** for UI and end-to-end Android flows
- **Compose screenshot testing** only if visual regression becomes important and the team accepts its experimental status

Android’s testing docs emphasize testing as a core part of development, and the quality guidelines define minimum app quality expectations.

_Source:_ [Test apps on Android](https://developer.android.com/training/testing), [Core app quality guidelines](https://developer.android.com/docs/quality-guidelines/core-app-quality), [Compose Preview Screenshot Testing](https://developer.android.com/studio/preview/compose-screenshot-testing)

### Deployment and Operations Practices

Deployment approach:

- **Internal alpha/beta** through Play Console tracks or direct APKs
- **Production** through Play Store and optionally F-Droid
- **Release gating** with App Bundle validation and quality checks
- **Operational monitoring** via crash reporting and basic app-health telemetry if needed

For a voice app, release discipline matters more than server scaling. The biggest operational risks are audio permissions, device variability, and command safety.

_Source:_ [Build and test your Android App Bundle](https://developer.android.com/guide/app-bundle/test), [Prepare your app for release](https://developer.android.com/studio/publish/preparing), [Core app quality guidelines](https://developer.android.com/docs/quality-guidelines/core-app-quality)

### Team Organization and Skills

Small-team roles for Perth:

- **Android engineer**: Compose, lifecycle, permissions, release pipeline
- **Voice/ML engineer**: speech recognition integration, transcription quality, latency tuning
- **Protocol/integration engineer**: zealot transport, sync, session mapping
- **Product/UX owner**: voice modes, confirmations, task flows

Critical skills:

- Kotlin coroutines and Flow
- Jetpack Compose state management
- Android permissions and audio lifecycle
- Secure storage and API-key handling
- UI testing with Espresso
- Protocol design and failure handling

### Cost Optimization and Resource Management

Cost controls:

- Prefer **on-device speech** where possible to reduce API spend
- Use **cloud transcription only as fallback** for difficult audio
- Cache recent sessions and avoid re-fetching unchanged panes
- Keep audio buffers short-lived unless explicitly saved
- Use **R8/app optimization** to keep the app fast and small

_Source:_ [Enable app optimization](https://developer.android.com/build/enable-app-optimization), [Google ML Kit GenAI Speech Recognition](https://developers.google.com/ml-kit/genai/speech-recognition/android), [Speech to text | OpenAI API](https://developers.openai.com/docs/guides/speech-to-text)

### Risk Assessment and Mitigation

| Risk | Impact | Mitigation |
|------|--------|------------|
| Unknown zealot API | Blocks integration | Build transport adapter and confirm API early |
| Voice latency | Poor UX | Use on-device first, async pipelines, caching |
| Permission friction | Capture failure | Request mic permission only at point of use |
| Background audio limits | Recording interruptions | Keep capture foreground-initiated |
| Command execution safety | Dangerous actions | Confirmation gate, audit log, allowlist |
| Battery usage | Drains device | Short sessions, no unnecessary background work |

_Source:_ [Request runtime permissions](https://developer.android.com/training/permissions/requesting), [Background audio hardening](https://developer.android.com/about/versions/17/changes/bg-audio), [Foreground service timeouts](https://developer.android.com/develop/background-work/services/fgs/timeout)

## Technical Research Recommendations

### Implementation Roadmap

1. Confirm zealot protocol and session model.
2. Build Android shell in Kotlin + Compose.
3. Implement session list, swipe navigation, and terminal rendering.
4. Add voice capture with permission gating and on-device speech.
5. Add transcription fallback and task-mode verbatim output.
6. Add command mode with safety confirmations.
7. Harden testing, release, and app-quality checks.

### Technology Stack Recommendations

- **Kotlin + Jetpack Compose** for the app shell
- **ViewModel + StateFlow** for state management
- **Room + DataStore + secure storage** for local data
- **Espresso + JUnit + MockK + Robolectric** for testing
- **Hybrid speech stack**: on-device first, Whisper fallback

### Skill Development Requirements

- Compose state hoisting and performance patterns
- Android runtime permissions and audio lifecycle handling
- Coroutines/Flow for async device and network work
- Protocol adapter design for uncertain server contracts
- Mobile QA and release discipline

### Success Metrics and KPIs

- Time to connect to zealot session
- Time from voice input to rendered result
- Transcription accuracy rate
- Command approval and failure rate
- Crash-free sessions
- Reconnect success rate
- Battery impact during voice capture

## Research Conclusion

Perth should be built as an Android-native, Compose-first client with a layered architecture, secure local storage, and a hybrid speech pipeline. The main technical unknown is the zealot server contract, so that should be resolved before committing to the transport layer or command execution design.
