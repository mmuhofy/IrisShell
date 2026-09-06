# Iris Shell — TODO

## Phase 1 — Terminal Core

### Session System (mid-execution)
- [x] Study Termux session lifecycle (TermuxService → TermuxShellManager → TermuxSession → TerminalSession → TerminalSessionClient)
- [x] Study ReTerminal id-keyed session map pattern
- [x] Create IrisSession wrapper (terminal/IrisSession.kt)
- [x] Create SessionLifecycleCallbacks interface (terminal/SessionLifecycleCallbacks.kt)
- [x] Refactor TerminalManager to use IrisSession list instead of 4 parallel structures
- [x] Fix onSessionFinished: clean up idToIndex + notify lifecycle callbacks
- [x] Wire PID tracking (TerminalSessionClientImpl → TerminalManager.onSessionPidChanged)
- [x] SessionManagerAdapter implements SessionLifecycleCallbacks
- [x] Reconcile logic: compare Room vs liveSessionIds instead of stale lastIds delta
- [x] Stitch UI modernization (SessionSwitcherSheet corner radius, SessionCard borders, PressScaleBox)

### Remaining Session System
- [ ] Implement captureLiveSnapshot() using emulator.getScreen() (tickerJob is no-op)
- [ ] Add pid column to SessionEntity for persistence
- [ ] Session preview swipe (shared element transition)
- [ ] Session groups / favorites
- [ ] Session search in switcher

### Completed (2026-09-06 — closeTab + app exit)
- [x] Fix closeTab: remove `if (irisSessions.size <= 1) return` guard
- [x] Add shouldExit StateFlow to SessionRepository (domain + data)
- [x] SessionManagerAdapter.start(): one-shot default session at startup
- [x] onLastSessionExited: signal app exit instead of creating default
- [x] SessionSwitcherViewModel: expose shouldExit
- [x] TerminalScreen/ReadyScreen: onExit callback → Activity.finish()
- [x] MainActivity: pass onExit via LocalContext

### PTY / Terminal
- [ ] Verify PTY session creation with PRoot + Ubuntu rootfs
- [ ] Handle PTY session resize on orientation change
- [ ] Terminal font loading from custom .ttf files

## Phase 2 — UI & Session System

- [ ] Block-based output engine (BlockEngine.kt)
- [ ] Semantic output parser (SemanticParser.kt)
- [ ] Ghost text autocomplete (GhostTextEngine.kt)
- [ ] Keyboard handle + extra keys bar
- [ ] Shortcut overlay (left/right picker)
- [ ] Session navigator (full-screen list)

## Phase 3 — Input System

- [ ] Ghost text inline autocomplete
- [ ] Multi-line input editing
- [ ] External keyboard support
- [ ] Voice input (Whisper API)

## Phase 4 — SSH & Remote

- [ ] SshjManager.kt — SSH client
- [ ] SshKeyVault.kt — encrypted key store with biometric unlock
- [ ] SSH Constellation view
- [ ] Port forwarding
- [ ] Jump host support

## Phase 5 — Safety, Polish & Distribution

- [ ] Dangerous command warning
- [ ] Smart sudo explanation
- [ ] Production tag
- [ ] F-Droid distribution setup
- [ ] Crash reporting
- [ ] Onboarding polish

## Phase 6 — Agent Intelligence

- [ ] AgentLoop.kt + MultiStepStreamer.kt (ported from Iris Code)
- [ ] Tool implementations (bash, read_file, write_file, ask_user, web_search)
- [ ] LLM provider adapters (Gemini, OpenAI, Anthropic, OpenRouter)
- [ ] Output intelligence (error detection, suggestions)
- [ ] Natural Language Cron
- [ ] Agent Watch
- [ ] Live Share
- [ ] Process Cinema
