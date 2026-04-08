# Perth - BMAD Workflow Plan

**Project:** Perth - Voice-driven Mobile-first Zellij Client  
**Created:** 2026-03-29  
**Project Level:** **3** (Complex Mobile Integration: Android + Voice AI + LLM + Server Protocol)  
**Source:** braindump.md

> ⚠️ **Oracle Review Findings**: Level 3 assessment based on scope (Android client, server protocol, voice capture, transcription, LLM-driven command execution, multi-mode UX). Reassess once zellij protocol is proven.

---

## Executive Summary

Perth is a mobile Zellij client with voice-driven interface. Based on the braindump, this project requires:
- Android mobile app
- Zellij server (zellij) connectivity
- Three voice interaction modes (Command, Transcription, Task)
- Swipe-based navigation between tabs/panes
- Live terminal state sync and input transport

**Estimated Scope:** 20-35 stories across 6-8 epics

---

## BMAD Workflow Sequence (CORRECTED)

```
┌─────────────────────────────────────────────────────────────────────────┐
│  PHASE 1: ANALYSIS (Required for Level 3)                             │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  [research] ──→ [brainstorming] ──→ [create-product-brief]            │
│                                                                          │
│  Deliverables:                                                            │
│  ✓ Technology Research (research-perth-*.md)                            │
│  ✓ Ideation Session Notes                                               │
│  ✓ Product Brief (product-brief-perth-*.md)                            │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│  PHASE 2: PLANNING                                                       │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  [prd] ──→ [create-ux-design] ──→ [create-architecture]               │
│       ↓          ↓                    ↓                                │
│  Full PRD    Mobile UI           System Design                          │
│  Requirements patterns           Client/Server/Voice/LLM                  │
│  User flows    +                 Data flows                            │
│               Voice UX                                                 │
│                                                                          │
│  Deliverables:                                                            │
│  ✓ PRD (prd-perth-*.md)                                               │
│  ✓ UX Design Document (ux-perth-*.md)                                  │
│  ✓ Architecture (architecture-perth-*.md)                              │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│  PHASE 3: SOLUTIONING                                                    │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  [check-implementation-readiness] ──→ [create-epics-and-stories]       │
│  ⚠️ CRITICAL GATE                                    ↓                   │
│  Validates PRD + Architecture + Epics alignment      [testarch-test-design]│
│                                                                          │
│  Deliverables:                                                            │
│  ✓ Implementation Readiness Report                                      │
│  ✓ Epic files (epic-*.md per feature area)                              │
│  ✓ Story files (story-*.md per user story)                              │
│  ✓ Test Design Document                                                 │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│  PHASE 4: IMPLEMENTATION                                                 │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  [sprint-planning] ──→ [create-story] ──→ [dev-story] ──→ [code-review] │
│       ↓                     ↓              ↓             ↓              │
│  Sprint tracker         Individual     Implementation    Review          │
│  Status file            stories        + Tests          Quality         │
│                                                                          │
│  Parallel tracks:                                                        │
│  - [testarch-framework] (setup mobile test infrastructure)              │
│  - [testarch-atdd] (write failing tests)                               │
│                                                                          │
│  ⚠️ Use custom [ticket-lifecycle] workflow from repo for ticket mgmt    │
│                                                                          │
│  Deliverables:                                                            │
│  ✓ Sprint Status (sprint-status-perth.yaml)                             │
│  ✓ Implemented & Tested Stories                                         │
│  ✓ Code Review Reports                                                   │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
                                    ↓
┌─────────────────────────────────────────────────────────────────────────┐
│  PHASE 5: VALIDATION                                                     │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  [testarch-nfr] ──→ [testarch-trace] ──→ [retrospective]               │
│                                                                          │
│  Deliverables:                                                            │
│  ✓ Non-Functional Requirements Assessment                               │
│  ✓ Requirements-to-Tests Traceability Matrix                           │
│  ✓ Retrospective Notes                                                   │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## Phase 1: Analysis

### Workflows

| Workflow | Purpose | Output |
|----------|---------|--------|
| `brainstorming` | Explore alternative UX approaches for voice interaction | Session notes |
| `research` | Investigate zellij API, voice tech, mobile frameworks | `docs/research-perth-*.md` |
| `create-product-brief` | Define product vision, target user, core value | `docs/product-brief-perth-*.md` |

### Research Focus Areas (REQUIRED)

1. **Zellij Server Protocol**
   - WebSocket vs REST API
   - Authentication flow
   - Session/tab/pane state sync
   - Command execution transport

2. **Voice Stack**
   - Android Speech Recognition API
   - Fallback: cloud-based (Google Speech, Whisper)
   - Audio permissions lifecycle

3. **Mobile Framework**
   - Kotlin/Compose (Android-native)
   - Decision criteria: voice integration, performance

4. **LLM Options**
   - Local vs cloud providers
   - Latency requirements for command mode

### Key Decisions Needed
- [ ] Mobile framework: Kotlin/Compose (Android-native)
- [ ] Voice API: Android Speech vs cloud fallback
- [ ] LLM provider: OpenAI vs local
- [ ] Server protocol: WebSocket (preferred for real-time sync)

---

## Phase 2: Planning

### Workflows (CORRECTED ORDER: PRD → UX → Architecture)

| Workflow | Purpose | Output | Note |
|----------|---------|--------|------|
| `prd` | Full product requirements document | `docs/prd-perth-*.md` | |
| `create-ux-design` | Define mobile UI patterns, swipe gestures, voice UX | `docs/ux-perth-*.md` | UX FROM PRD |
| `create-architecture` | System design: client, server, voice, LLM | `docs/architecture-perth-*.md` | PerBMAD: runs in Solutioning, but Perth runs it here |

> ⚠️ **BMAD Chaining Standard**: PRD must exist BEFORE UX generation. UX agent requires PRD as input.
> 
> 📌 **Note**: The repo's `create-architecture` workflow lives under `_bmad/bmm/workflows/3-solutioning/`, but Perth runs it in Phase 2 (Planning) because architecture decisions are needed before epic breakdown.

### Epic Structure (REVISED)

#### FOUNDATION EPIC (REQUIRED - not optional)

```
epic-perth-foundation.md    # Session Sync & Terminal Transport
├── story: zellij server connection & authentication
├── story: session list retrieval
├── story: tab/pane state sync (live updates)
├── story: terminal output rendering
├── story: input transport to active pane
├── story: swipe navigation between tabs/panes
└── story: reconnection/offline handling
```

#### SHARED EPIC

```
epic-perth-voice-shared.md  # Common Voice Infrastructure
├── story: microphone permissions & lifecycle
├── story: voice capture (start/stop UI)
├── story: speech-to-text transcription (shared)
└── story: audio session management
```

#### FEATURE EPICS

```
epic-perth-llm.md           # LLM Integration (PREREQUISITE for Command)
├── story: LLM provider integration
├── story: prompt templates per mode
├── story: response parsing & validation
└── story: error handling & fallbacks

epic-perth-voice-command.md # Command Mode (depends on LLM + Voice Shared)
├── story: voice → LLM interpretation
├── story: command planning & confirmation UI
├── story: terminal command execution
├── story: command safety/confirmation boundaries
├── story: failure rollback/auditability
└── story: prompt injection prevention

epic-perth-voice-transcribe.md # Transcription Mode
├── story: voice → verbatim transcription
├── story: paste to active Zellij terminal pane
└── story: confirmation before paste

epic-perth-voice-task.md    # Task Mode (VERBATIM requirement)
├── story: voice → verbatim transcription (EXACT words, no LLM)
├── story: format as task.md
├── story: write to active Zellij pane
└── story: confirmation before write
```

#### INFRASTRUCTURE EPIC

```
epic-perth-android.md       # Android-specific Concerns
├── story: Android permissions (mic, network)
├── story: background audio handling
├── story: notification for active session
└── story: battery optimization

epic-perth-testing.md       # Test Infrastructure
├── story: unit tests (JVM + Android)
├── story: integration tests (mock zellij)
├── story: E2E tests (Espresso for Android)
└── story: voice interaction tests
```

---

## Phase 3: Solutioning

### Workflows

| Workflow | Purpose | Output |
|----------|---------|--------|
| `check-implementation-readiness` | **CRITICAL GATE** - validates PRD + Architecture + Epics | `docs/readiness-perth-*.md` |
| `create-epics-and-stories` | Transform PRD → implementable units | Epic + Story files |
| `testarch-test-design` | Design testing approach per epic | `docs/test-design-perth.md` |

### Implementation Readiness Checklist

- [ ] PRD covers all 3 voice modes with explicit verbatim requirements
- [ ] Architecture addresses zellij server contract
- [ ] Foundation epic covers terminal sync & input transport
- [ ] Command mode safety/confirmation requirements documented
- [ ] Android permissions and audio lifecycle planned

### Key Decisions Needed
- [ ] Authentication flow for zellij server
- [ ] Voice mode switching UI (how to select mode)
- [ ] Error handling strategy (offline, API failures, LLM failures)
- [ ] Command confirmation boundaries (what requires user approval)

### Critical Concerns to Address

#### Command Execution Safety ⚠️
- What commands require explicit user confirmation?
- How to prevent prompt injection in voice → command pipeline?
- Rollback/undo strategy for destructive commands
- Audit trail for executed commands

#### Zellij Server Contract
- Is the API documented? Or is this also a design task?
- Real-time state sync mechanism
- Authentication tokens, session management
- Command input protocol

#### Android-Specific
- Microphone permission flow
- Background audio (recording while app backgrounded?)
- Network state changes, reconnection
- Battery optimization exceptions

---

## Phase 4: Implementation

### Sprint Structure (CORRECTED DEPENDENCIES)

> ⚠️ **LLM Integration must precede Command Mode** - Command mode depends on LLM working

| Sprint | Epic Focus | Dependencies | Stories |
|--------|-----------|--------------|---------|
| Sprint 1 | Foundation | - | 6-7 stories |
| Sprint 2 | Android Shell | Sprint 1 | 3-4 stories |
| Sprint 3 | LLM Integration | Sprint 1 | 3-4 stories |
| Sprint 4 | Voice Shared | Sprint 2 | 3-4 stories |
| Sprint 5 | Transcription Mode | Sprint 4 | 2-3 stories |
| Sprint 6 | Task Mode (VERBATIM) | Sprint 4 | 2-3 stories |
| Sprint 7 | Command Mode | Sprint 3, Sprint 4 | 4-5 stories |
| Sprint 8 | Polish + Testing | Sprint 7 | 3-4 stories |

### Workflows

| Workflow | Purpose | Output |
|----------|---------|--------|
| `sprint-planning` | Generate sprint tracker | `sprint-status-perth.yaml` |
| `ticket-lifecycle` | **Custom repo workflow** - ticket management | Plane sync |
| `create-story` | Create individual story | `docs/stories/story-*.md` |
| `dev-story` | Implement + test | Code + tests |
| `code-review` | Adversarial review | Review report |
| `testarch-framework` | Setup test infrastructure | Framework config | ⚠️ PerBMAD: web-only (Playwright/Cypress) — use Android test stack (Espresso, JUnit) instead |
| `testarch-atdd` | Write failing tests first | Test files |

### Test Infrastructure (Mobile, not Web)

| Test Type | Framework | Scope |
|-----------|-----------|-------|
| Unit | JUnit + MockK | ViewModels, business logic |
| Integration | MockWebServer + Espresso | API layer |
| E2E | **Espresso** (Android) | Full user flows |
| Voice | Robolectric + custom | Voice interactions |

> ⚠️ **No Playwright/Cypress** - this is an Android app, not a web app.
> The PerBMAD `testarch-framework` is web-only. Use Android-native testing instead.

---

## Phase 5: Validation

### Workflows

| Workflow | Purpose | Output |
|----------|---------|--------|
| `testarch-nfr` | Assess NFRs | NFR assessment report |
| `testarch-trace` | Traceability matrix | Matrix + gate decision |
| `retrospective` | Lessons learned | Retrospective doc |

### NFR Focus Areas

- **Performance**: Voice latency, terminal sync latency
- **Reliability**: Network reconnection, offline mode
- **Security**: Command injection prevention, server auth
- **Accessibility**: Voice UI for mobile

---

## Immediate Next Steps

### Recommended Workflow Order (CORRECTED)

```
1. /research
   └─→ Investigate zellij API, voice stack, mobile framework
   
2. /brainstorming
   └─→ Explore voice UX approaches
   
3. /create-product-brief
   └─→ Define Perth's vision and core value proposition
   └─→ Note: this order (research → brainstorming → brief) matches the workflow diagram
   
4. /prd  ← PRD BEFORE UX
   └─→ Create comprehensive PRD with explicit verbatim requirements
   
5. /create-ux-design
   └─→ Plan mobile UI patterns (UX FROM PRD)
   
6. /create-architecture  ← REQUIRED for level 3
   └─→ System design (zellij contract, voice stack, LLM)
   
7. /check-implementation-readiness  ← CRITICAL GATE
   └─→ Validate PRD + Architecture alignment
   
8. /create-epics-and-stories
   └─→ Break into implementable units
   
9. /sprint-planning
   └─→ Generate sprint tracker with correct dependencies
```

### Critical Path

```
braindump → research → brainstorming → product-brief → PRD → UX → architecture → readiness → epics → sprint 1 → ...
```

---

## Ticket References

After completing planning, create Plane tickets for each epic:

| Epic | Ticket Label | Priority | Sprint |
|------|-------------|----------|--------|
| Foundation (Session Sync) | `perth/foundation` | P0 | Sprint 1 |
| Android Shell | `perth/android` | P0 | Sprint 2 |
| LLM Integration | `perth/llm` | P0 | Sprint 3 |
| Voice Shared | `perth/voice-shared` | P0 | Sprint 4 |
| Transcription Mode | `perth/transcribe` | P1 | Sprint 5 |
| Task Mode (VERBATIM) | `perth/task` | P1 | Sprint 6 |
| Command Mode | `perth/command` | P1 | Sprint 7 |
| Polish + Testing | `perth/testing` | P2 | Sprint 8 |

---

## Notes

- **Use custom `ticket-lifecycle` workflow** from `_bmad/custom/workflows/ticket-lifecycle/` for ticket management
- **Phase 1 is REQUIRED for Level 3** - Perth needs deep research into zellij protocol, voice stack, and mobile framework
- **Architecture is REQUIRED** (level 3) - don't skip
- **check-implementation-readiness is CRITICAL GATE** - don't proceed to solutioning without passing
- **testarch-* workflows** run in parallel with implementation
- **testarch-framework is web-only (PerBMAD)** - use Android-native testing instead
- **Keep BMAD docs in sync** with Plane board state
- **Command Mode requires LLM** - sprint ordering reflects this dependency

---

## Oracle Review Checklist

- [x] Workflow sequence corrected (PRD → UX → Architecture)
- [x] Foundation epic added (session sync, terminal rendering, input transport)
- [x] Project level corrected to Level 3
- [x] Sprint dependencies fixed (LLM before Command Mode)
- [x] Task mode marked as VERBATIM requirement
- [x] Android-specific concerns added
- [x] Command execution safety documented
- [x] Test framework corrected to mobile (Espresso) + PerBMAD warning
- [x] Custom ticket-lifecycle workflow referenced
- [x] Critical path includes UX + brainstorming
- [x] Phase 1 marked as required for Level 3
- [x] Architecture phase placement clarified (PerBMAD deviation)
