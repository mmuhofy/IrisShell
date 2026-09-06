# Iris Shell — Memory Bank
_Last updated: 2026-09-06_

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
Compose Screen (SessionSwitcherSheet)
      ↓ UI events
SessionSwitcherViewModel
      ↓ calls
ObserveActiveSessionUseCase / SessionRepository
      ↓ calls
SessionRepositoryImpl (Room + DataStore)
      ↓ bridges
SessionManagerAdapter (implements SessionLifecycleCallbacks)
      ↓ calls
TerminalManager (IrisSession list, PTY lifecycle)
      ↓ owns
TerminalSession (PTY emulator) + TerminalSessionClientImpl
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
Closed (Room only, removed from irisSessions)
  ↓ user restores → restoreSession → Idle → reconcile spawns PTY
```

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
