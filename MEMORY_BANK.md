# Iris Shell — Memory Bank
_Last updated: 2026-09-07_

---

## 1. Project Identity

| Field | Value |
|-------|-------|
| App name | Iris Shell |
| Package | `com.iris.irisshell` |
| Tagline | "Your phone is a Unix machine. Finally." |
| License | MIT |
| Distribution | F-Droid first, GitHub Releases |
| Repo | github.com/mmuhofy/IrisShell |
| Ecosystem | Iris — by Muhofy |

---

## 2. Confirmed Stack

| Component | Decision | Notes |
|-----------|----------|-------|
| Language | Kotlin 2.1.0 | Iris Code parity |
| UI | Jetpack Compose BOM 2026.04.01 | Proven working combo |
| Architecture | MVVM + Clean Architecture | Strict layering |
| DI | Hilt 2.57 | Iris Code parity |
| Min SDK | 26 | Android 8.0+ |
| Target SDK | 28 | Android 14 |
| Compile SDK | 36 | — |
| Terminal Engine | termux-view + termux-terminal-emulator | Vendored |
| PTY | libtermux.so (JNI) | Prebuilt, port from Iris Code |
| Linux Env | PRoot v5.2.0 + Ubuntu 24.04 rootfs | Port from Iris Code |
| Storage | Room 2.8.4 + FTS5 | Session history |
| Preferences | DataStore 1.1.x | Settings, active session id |
| Async | Kotlin Coroutines + Flow | Reactive streams |
| Build | Gradle KTS + Version Catalog | Iris Code parity |

---

## 3. Architecture Layers

```
ui/           → Compose screens, ViewModels
domain/       → Pure Kotlin interfaces, use cases
data/         → Repository impls, Room DAOs, SessionManagerAdapter
terminal/     → PTY session management, TerminalManager, IrisSession
di/           → Hilt modules
util/         → Constants, helpers
```

### Session Data Flow

```
Compose Screen (SessionSwitcherSheet / ReadyScreen)
      ↓ UI events
SessionSwitcherViewModel (exposes allSessions, activeId, shouldExit)
      ↓ calls
ObserveActiveSessionUseCase / SessionRepository
      ↓ calls
SessionRepositoryImpl (Room + DataStore, _shouldExit MutableStateFlow)
      ↓ bridges
SessionManagerAdapter (implements SessionLifecycleCallbacks)
      ↓ calls
TerminalManager (IrisSession list, PTY lifecycle)
      ↓ owns
TerminalSession (PTY emulator) + TerminalSessionClientImpl
```

### Exit Signal Flow

```
User deletes last session OR last session exits naturally:
  TerminalManager.closeTab() or onSessionFinished()
      → irisSessions empty
      → onSessionsEmpty() / onLastSessionExited()
      → SessionManagerAdapter
      → sessionRepository.setShouldExit(true)
      → SessionRepositoryImpl._shouldExit.value = true
      → SessionSwitcherViewModel.shouldExit (StateFlow)
      → ReadyScreen LaunchedEffect { onExit() }
      → MainActivity: LocalContext.current.finish()
```

---

## 4. Session System — Current Implementation

### Key Files

| File | Role |
|------|------|
| `domain/session/SessionSnapshot.kt` | Session state model (id, name, state, timestamps, live lines) |
| `domain/session/SessionRepository.kt` | Repository interface |
| `data/session/SessionEntity.kt` | Room entity |
| `data/session/SessionDao.kt` | Room DAO |
| `data/session/SessionRepositoryImpl.kt` | Room + DataStore impl, _livePreviews StateFlow |
| `data/session/SessionManagerAdapter.kt` | Bridges Room ↔ TerminalManager; implements SessionLifecycleCallbacks |
| `terminal/IrisSession.kt` | Wrapper: TerminalSession + persistentId + name + pid |
| `terminal/SessionLifecycleCallbacks.kt` | Callback interface for session lifecycle events |
| `terminal/TerminalManager.kt` | PTY session lifecycle; single irisSessions list + idToIndex map |
| `terminal/TerminalSessionClientImpl.kt` | TerminalSessionClient impl; forwards onSessionFinished + onPidChanged |

### Key Improvements (2026-09-06)

**Bug fixes:**
- `TerminalManager.onSessionFinished()` now cleans up `idToIndex` mappings (previously leaked stale entries)
- `onSessionFinished()` now calls `SessionLifecycleCallbacks.onSessionFinished` so Room state updates to Closed when PTY exits
- PID tracking wired: `TerminalSessionClientImpl.setTerminalShellPid` → `TerminalManager.onSessionPidChanged` → stores pid on `IrisSession`

**Architecture:**
- Replaced 4 parallel structures (`_sessions`, `_tabNames`, `_idToIndex`, `_indexToId`) with single `MutableList<IrisSession>` + `idToIndex`
- `SessionManagerAdapter.reconcile()` now compares Room state against `TerminalManager.liveSessionIds()` instead of stale `lastIds` delta — ensures Closed sessions aren't re-spawned, restored Idle sessions ARE spawned
- `SessionManagerAdapter` implements `SessionLifecycleCallbacks`, wires itself via `terminalManager.lifecycleCallbacks = this` in `start()`

### Inspiration

- Termux session lifecycle: `TermuxService` → `TermuxShellManager` → `TermuxSession` → `TerminalSession` → `TerminalSessionClient`
- ReTerminal id-keyed session map: `HashMap<String, TerminalSession>` in SessionService
- Iris Code: `TerminalManager.kt` port baseline

---

## 5. Session State Machine

```
Idle (Room only, not yet spawned)
  ↓ addTabWithId / reconcile spawns PTY
Running (in Room + in irisSessions)
  ↓ process exits naturally → onSessionFinished → Room → Closed
  ↓ user deletes from Room → reconcile → closeTab → PTY killed
    → if last session → onLastSessionExited → shouldExit=true → Activity.finish()
Closed (Room only, removed from irisSessions)
  ↓ user restores → restoreSession → Idle → reconcile spawns PTY
    → shouldExit reset to false on create()
```

### Bug Fixes (2026-09-06 — closeTab + app exit)

- **BUG**: `closeTab` had `if (irisSessions.size <= 1) return` guard preventing
  the last session from ever being closed. User deletes session → Room row
  removed → `reconcile()` calls `closeTab` → guard blocks it → terminal
  never closes, session stuck "Running" in Room forever.
- **FIX**: Removed guard; `closeTab` now allows closing last session.
  When `irisSessions` becomes empty, immediately calls
  `onSessionFinished(persistentId, -1)` + `onLastSessionExited()`.
- **FIX**: `onLastSessionExited` now sets `shouldExit=true` (signals app exit)
  instead of creating a replacement session. Mirrors Termux's
  `TermuxService.updateNotification() → requestStopService()` pattern.
- **FIX**: Default session creation moved from `TerminalViewHost.LaunchedEffect`
  (UI layer) to `SessionManagerAdapter.reconcile()` (running on appScope,
  survives Activity recreation). `create()` resets `shouldExit=false`.
- **FIX**: `SessionRepository.create()` resets `_shouldExit` to false when a
  new session is created (so session switcher Create button cancels exit).
- **FIX**: `start()` resets `shouldExit=false` on fresh process launch;
  `ReadyScreen` uses `yield()` guard before onExit to let `reconcile()`
  create default on process-reuse relaunch.
- **ADDED**: `SessionRepository.shouldExit` + `setShouldExit` (domain interface).
  `SessionSwitcherViewModel.shouldExit` (StateFlow). `TerminalScreen.onExit`
  callback → `MainActivity` uses `LocalContext.finish()`.

### Termux Patterns Studied (2026-09-06)

- **TermuxShellManager** (termux-shared/shell/TermuxShellManager.java): simple
  `List<TermuxSession>` + static ID counter. No parallel arrays. Iris Shell
  mirrors with single `MutableList<IrisSession>`.
- **TermuxService** (app/TermuxService.java): `mShellManager.mTermuxSessions`
  is the single source of truth. `onTermuxSessionExited` removes from list.
  `updateNotification()` calls `requestStopService()` when sessions empty.
- **TermuxActivity** (app/TermuxActivity.java): `onServiceConnected` checks
  `isTermuxSessionsEmpty()` → creates new session if visible, or
  `finishActivityIfNotFinishing()` if not. Iris Shell mirrors:
  startup creates default session, user deletion triggers exit.

---

## 6. Visual Identity

| Element | Value |
|---------|-------|
| Background | `#0C0C0C` |
| Surface | `#141414` |
| Surface 2 | `#1A1A1A` |
| Primary accent | `#E8C547` (warm gold) |
| Text primary | `#EEEEEE` |
| Text secondary | `#888888` |

---

## 7. Current Status

### Completed (Session System modernization)
- ✅ `IrisSession.kt` — session wrapper (TermuxSession-inspired)
- ✅ `SessionLifecycleCallbacks.kt` — callback interface
- ✅ `TerminalManager.kt` — refactored to IrisSession list, fixed onSessionFinished, PID tracking
- ✅ `TerminalSessionClientImpl.kt` — forwards PID + session finished callbacks
- ✅ `SessionManagerAdapter.kt` — implements lifecycle callbacks, live-ids-based reconcile
- ✅ Stitch UI modernization (SessionSwitcherSheet + SessionCard)

### To Build
- Same as docs/TODO.md (full feature backlog)

### Completed (Onboarding Wizard — 2026-09-07)
- ✅ Design system components: `DroshLogo`, `SetupButton`, `DeviceCheckItem`, `ShellSelector`, `PackageProfileSelector`
- ✅ New 4-scene flow: Welcome → DeviceCheck → Preferences → ShellSetup (Zsh conditional)
- ✅ `WelcomeScene` replaced TerminalBackdrop with DroshLogo + SetupButton
- ✅ `OnboardingScreen` hoists preference state; conditional ShellSetup skip for Bash
- ✅ Old `ArchitectureScene`/`ReadyScene` left as dead code (no longer referenced)
- ✅ GitHub Actions build passing (`2209f58` — fix Kotlin compose API mismatches)

### Compose API Issues Resolved
- `if (selected) X : Y` → `if (selected) X else Y` (Kotlin requires `else`, not `:`)
- `KeyboardOptions()`: `keyboardCapitalization` → `capitalization`, `autoCorrect` required
- `painterResource()` must be called at Composable body, not inside `LaunchedEffect`
- `drawArc()`: `startAngleDegrees`/`sweepAngleDegrees` → `startAngle`/`sweepAngle`
- `Path.arcTo()`: `forceNewSubgroup` → `forceMoveTo`
- `Checkbox`/`CheckboxDefaults`: import from `material3`, not `foundation`
- `Surface(...) { }` trailing lambda: close with `}` not `)`
