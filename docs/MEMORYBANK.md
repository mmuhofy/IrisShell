# Iris Shell — Memory Bank
_Last updated: 2026-07-16_

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

## 2. Vision

Termux brought the terminal to Android in 2012. Iris Shell reinvents it for 2026. Not a Termux fork — a ground-up reimagination of what a mobile terminal should be: agent-native, semantically aware, and built for the way people actually use their phones. The goal is a single application that combines desktop terminal power, mobile-first UX, semantic UI, and AI workflow — making it the first terminal environment where the agent and the shell are the same thing.

**Target users:** Termux power users, mobile developers, DevOps engineers, CTF players, students learning Linux.

**Why it's different:**
- Every other Android terminal is a shell with a keyboard. Iris Shell is an intelligent environment.
- Warp did this for desktop. Nobody did it for Android.
- Open-source, free, bring-your-own-model — no cloud lock-in.

---

## 3. Confirmed Stack

| Component | Decision | Notes |
|-----------|----------|-------|
| Language | Kotlin 2.1.0 | Iris Code parity |
| UI | Jetpack Compose BOM 2026.04.01 | Proven working combo |
| Architecture | MVVM + Clean Architecture | Strict layering |
| DI | Hilt 2.57 | Iris Code parity |
| Min SDK | 26 | Android 8.0+ |
| Target SDK | 36 | Android 16 |
| Terminal Engine | termux-view + termux-terminal-emulator | Vendored from Iris Code |
| PTY | libtermux.so (JNI) | Prebuilt, port from Iris Code |
| Linux Env | PRoot v5.2.0 + Ubuntu 24.04 rootfs | Port from Iris Code |
| Agent Loop | MultiStepStreamer + AgentLoop | Port from Iris Code |
| LLM (v1.0) | Gemini 3.5 Flash (Google GenAI SDK) | Cloud-first |
| LLM (v1.1+) | + Anthropic, OpenAI, OpenRouter | Multi-provider |
| LLM (v2.0+) | + Ollama, llama.cpp | Local optional |
| HTTP/Stream | OkHttp 4.12.x + SSE | Agent streaming |
| SSH | SSHJ 0.38.x | Modern, actively maintained |
| Storage | Room 2.8.4 + FTS5 | Command DNA, session history |
| Preferences | DataStore 1.1.x | Settings, shortcuts |
| Security | AndroidX Security Crypto 1.1.x | API keys, SSH Key Vault |
| Serialization | Kotlinx Serialization 1.7.x | Shortcut export/import, themes |
| Background | WorkManager 2.10.x | Natural Language Cron, Agent Watch |
| Biometric | BiometricPrompt 1.2.x | SSH Key Vault unlock |
| Image Loading | Coil Compose 3.x | Theme Store previews |
| Animation | Lottie Compose 6.x | Onboarding, loading |
| Async | Kotlin Coroutines 1.10.x + Flow | Reactive streams |
| Build | Gradle KTS + Version Catalog | Iris Code parity |

---

## 4. Architecture Layers

```
ui/
  terminal/         → Terminal screen, session UI, block renderer
  sessions/         → Session list, preview, navigator
  ssh/              → SSH manager, constellation
  shortcuts/        → Shortcut overlay, keyboard panel
  settings/         → Settings, theme store, API vault
  hud/              → HUD widgets, status panel
  workspace/        → Project workspace, workflow management

domain/
  terminal/         → TerminalSession, Block, SemanticToken
  agent/            → AgentLoop, Tool interfaces, StreamEvent
  session/          → SessionEntity, CommandDNA, Replay
  ssh/              → SshHost, SshKey, SshConnection
  shortcut/         → ShortcutEntity, KeyBinding, CommandShortcut
  workspace/        → Workspace, Project, Workflow

data/
  terminal/         → TerminalManager, ProotRunner, UbuntuBootstrap
  agent/            → MultiStepStreamer, ProviderAdapter, ToolRegistry
  local/            → Room DAOs, FTS5, DataStore
  remote/           → LLM clients, SSH client, Theme Store API
  ssh/              → SshjManager, SshKeyVault

agent/
  tools/            → BashTool, ReadFileTool, WebSearchTool, CronTool
  loop/             → AgentLoop, MultiStepStreamer
  semantic/         → SemanticParser, OutputClassifier

terminal/
  engine/           → PTY bridge, ANSI parser
  renderer/         → BlockRenderer, SemanticHighlighter, RichRenderer
  input/            → GhostTextEngine, InputQueue

di/                 → Hilt modules
util/               → Constants, extensions
```

### Rendering Pipeline

```
PTY output
    ↓
ANSI Parser        → strips escape codes, applies colors
    ↓
Semantic Parser    → detects ERROR/WARNING/SUCCESS/BUILD patterns
    ↓
Rich Renderer      → tables, JSON, markdown (v1.1+)
    ↓
Block Engine       → wraps each command+output as a Block
    ↓
Compose UI         → renders BlockList with animations
```

---

## 5. Visual Identity

| Element | Value |
|---------|-------|
| Background | `#0C0C0C` |
| Surface | `#141414` |
| Surface 2 | `#1A1A1A` |
| Border | `#1E1E1E` |
| Border subtle | `#232323` |
| Primary accent | `#E8C547` (warm gold) |
| Text primary | `#EEEEEE` |
| Text secondary | `#888888` |
| Text muted | `#666666` |
| Text disabled | `#444444` |
| Success | `#27AE60` |
| Error | `#C0392B` |
| Warning | `#C9A84C` |
| Terminal font | JetBrains Mono |
| UI font | Inter / system |
| Corner radius | 14dp cards, 12dp buttons, 8dp chips |
| Theme | Dark only (v1.0) |

### Semantic Highlight Colors
| Token | Color |
|-------|-------|
| ERROR / FATAL | `#C0392B` red |
| WARNING / WARN | `#C9A84C` gold |
| SUCCESS / DONE | `#27AE60` green |
| BUILD / COMPILE | `#4A90E2` blue |
| INFO | `#888888` muted |

### Terminal Themes (v1.1+)
- **Default** — Iris dark, warm gold accents
- **Stealth** — Pure black, minimal color
- **Material You** — Dynamic color from wallpaper
- **Glass** — Subtle blur, translucent surfaces

### OLED Mode
Toggle in Settings. Forces `#000000` background. Saves battery on OLED displays.

---

## 6. Navigation & Screen Inventory

### Navigation Hierarchy
```
Sessions (Home)
  → [session card]     → Terminal Screen
                           tabs: Terminal / Files / Agent
  → [+ new]            → New Session Sheet
  → [workspace]        → Workspace Screen
  → [⚙️]              → Settings

Settings
  → API Vault
  → SSH Manager
  → Theme Store
  → Shortcuts
  → HUD Config
  → Workspace
```

### Screens

**Sessions (Home)**
- Session cards with real terminal snapshot preview
- Session name, last command, uptime, SSH host if remote
- Long press → rename, delete, duplicate, export
- Swipe between sessions → preview card grows into terminal
- `[+]` top right → New Session Sheet
- Search + filter bar

**Terminal Screen**
- Full-screen terminal
- Block-based output (each command = one block)
- HUD strip at top (optional, configurable)
- Keyboard handle at bottom → tap = extra key bar toggle
- Ghost text inline autocomplete
- Tab bar: `[💻 Terminal]` `[📁 Files]` `[🤖 Agent]`
- Overflow `[⋮]`: rename session, export, share, settings

**Session Navigator**
- Triggered by long swipe or dedicated gesture (TBD)
- Full-screen list of all sessions
- Real terminal snapshot per card
- Drag to reorder, swipe to close

**SSH Manager**
- Host list with connection status
- Add host: name, IP/hostname, port, user, auth method
- SSH Constellation view (v1.1)
- Key Vault: stored keys, biometric unlock

**Theme Store**
- Browse community themes
- Preview before applying
- One-tap install
- Upload own theme

**Workspace**
- Project list
- Each project: name, path, linked sessions, workflows
- Workflow builder (v1.1)

**Settings**
- API Vault (per provider)
- Default model
- SSH Manager
- Theme Store
- Shortcuts Manager
- HUD Config
- Shell: zsh/bash toggle
- Auto-install packages toggle
- Auto-optimize rootfs toggle
- OLED mode toggle
- About, license, GitHub

### Session Preview Swipe

```
User swipes right →
  Current session scales down + moves left
  Next session preview scales up from right
  Preview shows real terminal snapshot
  User sees: session name + last command + real output
  Release → transition completes (Shared Element)
  Cancel (swipe back) → return to current
```

Shared Element Transition: session card thumbnail → full terminal screen.

---

## 7. Terminal Core

### Block-Based Output
Every command execution produces a Block:

```
┌──────────────────────────────────────┐
│ $ git status                    [📋] │  ← command line, gold
├──────────────────────────────────────┤
│ On branch main                       │  ← output
│ nothing to commit                    │
│ ✓ exit 0  •  12ms               [↕] │  ← footer: status, duration
└──────────────────────────────────────┘
```

- Short output (≤8 lines): fully expanded
- Long output (>8 lines): collapsed + "Show X more ↓"
- Very long (50+ lines): "Open fullscreen ↑"
- Copy button top right
- Long press block → copy, share, pin, search

### IDE-Style Input
- Multi-line editing
- Cursor movement (arrow keys via keyboard panel)
- Text selection
- Undo / Redo
- Not line-based — real text editor behavior

### Ghost Text Autocomplete
```
git pu
      sh origin main
```
Gray ghost text inline. Confirmed by tapping the suggestion pill that appears above keyboard. Falls back to TAB for shell completion.

```
┌─────────────────────────┐
│  [git push origin main] │  ← suggestion pill
└─────────────────────────┘
   git pu█
```

### Semantic Output Highlighting
Second rendering layer on top of ANSI colors:

| Pattern | Style |
|---------|-------|
| `ERROR`, `FATAL`, `Exception` | Red background tint |
| `WARNING`, `WARN` | Gold left border |
| `BUILD SUCCESSFUL`, `✓`, `done` | Green accent |
| `Task :app:compile*` | Blue, monospace |
| `[1/4]`, `[2/4]` | Progress indicator |

Supported tools: Gradle, npm, pnpm, cargo, git, adb, logcat, docker, pip.

### Rich Terminal Rendering (v1.1+)
PTY → ANSI → Semantic → Rich Renderer → Compose UI

| Content | Rendered As |
|---------|-------------|
| Markdown `# Title` | Large bold heading |
| `\| A \| B \|` table | Compose Table component |
| `- [ ] task` | Interactive checklist |
| `![img](url)` | Inline image thumbnail |
| JSON blob | Collapsible formatted JSON |

---

## 8. Input System

### Keyboard Handle
A thin drag handle sits above the system keyboard.
- **Tap** → toggles extra key bar (Termux-style)
- **Extra key bar visible:** ESC, TAB, CTRL, ALT, |, -, /, arrows, PgUp, PgDn, Home, End
- **Second tap** → hides extra key bar
- Bar is hidden by default — no permanent screen space consumed

### Shortcut Overlay
Triggered by a dedicated gesture or button (TBD — open decision).
When opened:
- Background dims
- **Left side** → Command Shortcuts (user-defined commands)
- **Right side** → Keyboard Shortcuts (key combinations)
- iOS wheel-picker style, scroll to select
- Tap outside → dismiss

**Command Shortcuts:**
User-defined. Each shortcut has:
- Name (e.g. "Deploy")
- Command (e.g. `git add . && git commit -m "update" && git push`)
- Icon
- Color
- Execute mode: run immediately OR paste only

**Keyboard Shortcuts:**
User-defined. Each shortcut has:
- Modifier: CTRL / ALT / SHIFT / CTRL+ALT
- Key: selected from full PC keyboard layout UI
- Name (e.g. "Kill Process")

**Community Shortcut Store:**
- Browse shortcut packs (Python Pack, Docker Pack, Git Flow Pack)
- One-tap install
- Export/import as JSON
- Submit own packs

### Ghost Text Confirmation
Suggestion pill appears above keyboard when ghost text is active.
Tap pill → accepts completion.

### Voice Input
Microphone button in input bar. Hold to record. Whisper API → transcript. Auto-send option in settings.

### External Keyboard
Full key mapping support. Cmd+K → command palette. Escape → dismiss overlays.

---

## 9. Session System

### Session Preview Swipe
See §6 Navigation. Shared Element Transition — card thumbnail morphs into full terminal. Real snapshot shown in preview, not placeholder.

### Session Organization
- **Name** — user-defined (e.g. `prod-server`, `dev-local`, `docker-lab`)
- **Groups** — logical grouping of related sessions
- **Favorites** — pinned to top of session list
- **Recents** — auto-sorted by last used

### Workspace (Project-Based)
Each workspace is a project:
```
Workspace: MyApp
  ├── Path: /home/user/myapp
  ├── Sessions: dev-local, test-env
  ├── Shortcuts: project-specific commands
  └── Workflows: deploy, test, build (v1.1+)
```
- Project-aware shortcuts
- Project-aware agent context
- Workflow automation (v1.1+)
- Lightweight — terminal stays primary

---

## 10. Agent Core

### Architecture
Port of Iris Code's agent system. Same 3-layer architecture:

```
AgentLoop (submission)
  └── Flow<AgentEvent> → UI
  └── Maps StreamEvent → AgentEvent
        ↓
MultiStepStreamer (multi-step engine)
  └── for (step in 1..MAX_STEPS)
  └── ProviderAdapter.stream() → SSE
  └── Tool execution inline
        ↓
ProviderAdapter (interface)
  └── OpenAiProviderAdapter (impl)
  └── OpenAI-compatible /chat/completions
```

### Tool Set (v1.0)
| Tool | Description | Mode |
|------|-------------|------|
| `bash` | Execute shell command via PRoot | BUILD |
| `read_file` | Read file into agent context | PLAN + BUILD |
| `write_file` | Write file, diff+approve flow | BUILD |
| `ask_user` | Ask user a question | PLAN + BUILD |
| `update_todo` | Create/update TodoCard | PLAN + BUILD |
| `web_search` | Tavily API search | PLAN + BUILD |

### Work Mode
| Mode | Behavior |
|------|----------|
| PLAN | Read-only. Agent suggests only. |
| BUILD | Full tool use. Diff/approve active. |
| AUTO | BUILD + autonomy toggles forced ON. |

### Agent in Terminal Context
Agent operates in the same shell environment as the user. Same PRoot Ubuntu session. Agent can:
- Read current directory
- Execute commands
- Observe output
- Chain commands across steps

User terminal and agent share the same filesystem. Agent bash output → Agent tab (not user terminal).

---

## 11. Features

### Detailed Features

#### Command DNA
Every command is automatically indexed:
- Timestamp
- Working directory
- Exit code
- Duration
- Output summary (first 3 lines)
- Session ID
- Tags (auto-detected: git, docker, ssh, python...)

Stored in Room FTS5. Queryable:
- "What did I do on the prod server last week?"
- "Show all failed commands today"
- "Find when I last ran npm install"

#### Session Intelligence
Iris indexes all sessions. Natural language queries across session history. Uses FTS5 full-text search. Agent layer for complex queries.

#### Natural Language Cron
```
"Every morning at 8am run git pull"
"Every Monday clean server logs"
"Every 5 minutes ping health endpoint"
        ↓
Iris parses → generates cron expression
        ↓
WorkManager schedules job
        ↓
Runs in background, reports results
```
No cron syntax required. Managed from Settings → Scheduled Tasks.

#### Agent Watch
Background monitoring with natural language conditions:
```
"Tell me when this build finishes"
"Alert me if CPU goes above 90%"
"Notify me if this endpoint goes down"
```
WorkManager + Foreground Service. Sends Android notification when condition met. Notification actions: View, Dismiss, Repeat.

#### Iris Autopilot
Multi-step task execution:
```
User: "Deploy the app to production"
        ↓
Iris: shows step plan
User: approves
        ↓
Iris executes step by step:
  1. git add . && git commit -m "..."
  2. git push origin main
  3. ssh server "pm2 restart app"
  4. curl https://app.com/health
        ↓
Reports result, notifies on completion
```
Each step shown as a block. User can pause between steps. Agent handles errors and retries.

#### Live Share
Real-time terminal session sharing between Iris Shell instances:

**Initiator:** generates share link / QR code
**Guest:** opens link → joins session

Permission levels:
| Level | Can Do |
|-------|--------|
| Read Only | Watch terminal output |
| Suggest | Submit commands for approval |
| Execute | Run commands directly |
| Full Control | Full terminal access |

SSH-inspired but human-first. No external server required for LAN sharing. Relay server for internet sharing (v1.1+).

#### Terminal Lens
Point camera at any terminal, monitor, document, or paper:
- OCR reads the text
- Iris parses it as a command or output
- Shows: "Run this command?" → user approves
- Eliminates manual retyping

Triggered from input bar camera icon.

#### HUD (Heads-Up Display)
Configurable status strip at top of terminal screen:

Available widgets:
- CPU usage %
- RAM usage %
- Network ↑↓ speed
- Active job count
- SSH connection status
- Clock
- Battery %
- Disk usage

User selects which widgets to show. Drag to reorder. Can be hidden entirely.

#### SSH Constellation (v1.1)
Visual map of all SSH hosts:
- Graph layout — nodes = servers, edges = jump host relationships
- Tap node → connect
- Color by status: green (connected), gray (idle), red (unreachable)
- Shows active sessions per host

#### Theme Store
- Browse community themes
- Preview with real terminal screenshot
- One-tap install
- OLED-optimized themes flagged
- Submit own theme (JSON format)
- Font packs included

#### Alias Manager
GUI for shell aliases:
- Name: `dc`
- Command: `docker-compose`
- Scope: global or per-workspace
- Sync to `.zshrc` / `.bashrc` automatically

#### Dangerous Command Warn
Intercepts and warns before:
- `rm -rf *` or `rm -rf /`
- `chmod 777` on system paths
- `dd if=`
- Fork bomb patterns
- Any command in a **Production-tagged** session

Warning card shows: what will happen, estimated impact, confirm / cancel.

#### Smart Sudo
Before any `sudo` command:
- Iris explains what the command will do
- Shows affected files/paths
- Risk level: LOW / MEDIUM / HIGH
- User confirms → executes
- Even in AUTO mode

#### Production Tag
Mark any SSH host or session as "Production":
- Red banner at top of terminal screen: `⚠️ PRODUCTION`
- Every command shows confirm prompt
- Dangerous Command Warn always active
- Cannot be accidentally dismissed

#### Session Replay
Every session is recorded as a command sequence:
- Replay step by step
- Pause, rewind, fast-forward
- Copy any command from replay
- Export as shell script

#### Output Intelligence
Agent analyzes command output automatically:
```
$ npm install
  ↓
┌──────────────────────────────────┐
│ ⚠️  3 high severity vulns found  │
│ 📦  847 packages installed       │
│ ⏱️  23.4s                        │
│ [Fix Vulns]  [Details]           │
└──────────────────────────────────┘
```
Actionable cards from raw output. Supported: npm, pip, gradle, cargo, apt, docker, git.

#### Error DNA
When a command fails:
1. Iris detects failure (exit code ≠ 0)
2. Agent diagnoses: what failed and why
3. Suggests fix
4. If user applies fix → outcome stored
5. Next time same error occurs → Iris proposes proven fix immediately

Learned fixes stored in Room. Per-user, per-project.

#### Multi-Exec
Send same command to multiple SSH hosts simultaneously:
- Select hosts from SSH manager
- Enter command
- Outputs shown side by side per host
- Aggregate status: X/Y succeeded

#### Process Cinema (v2.0)
Visual process manager:
- Each running process shown as a card
- CPU, RAM, uptime per process
- [Stop] [Restart] [Logs] actions
- Agent: "Why is this process using so much RAM?"

---

### Planned Features (name only)

Input & Completion: Typo Fixer, Command Template, Pipe Suggestions, Sudo Remember, History Dedup

Output: Output Pin, Output Diff, Table Renderer, JSON Renderer, Progress Detector, Output Filter, Line Numbering

Files: Drag & Drop Upload, Quick Edit, File Size Warning, Trash, Recents

SSH: Auto Reconnect, Connection Health, Offline Queue, SSH Config Import

Safety: Dry Run, Command Lock

Discovery: Man Page Viewer, Cheat Sheet, Package Search, Port Scanner Mini, Env Viewer

Productivity: Command Timer, Repeat Command, Parallel Run, Command Chain Builder, Expected Output

Quick Access: Quick Note, Screenshot to Command, Widget, Notification Actions

Accessibility: Large Font Mode, High Contrast Theme, Single Hand Mode

---

## 12. SSH System

### Stack
SSHJ 0.38.x — modern, actively maintained, clean Kotlin-friendly API.

### SshHost Entity
```kotlin
data class SshHost(
    val id: String,
    val name: String,        // "prod-server"
    val hostname: String,
    val port: Int = 22,
    val username: String,
    val authMethod: AuthMethod, // PASSWORD | KEY | KEY_WITH_PASSPHRASE
    val keyId: String?,
    val jumpHostId: String?,
    val isProduction: Boolean,
    val tags: List<String>
)
```

### SSH Key Vault
- Keys stored encrypted via Security Crypto
- BiometricPrompt unlock before key use
- Supports RSA, ED25519, ECDSA
- Built-in key generator
- Import from file or paste

### Features
- Port forwarding (local + remote)
- Jump host (bastion) support
- Mosh support (v1.1)
- SFTP browser (v1.1)
- Known hosts management
- Fingerprint visual verification
- Session timeout + auto-lock
- Keep-alive ping

---

## 13. Security & Privacy

- API keys encrypted via AndroidX Security Crypto
- SSH keys encrypted, biometric unlock
- Smart Sudo — explain before execute
- Production Tag — visual warning, confirm every command
- Dangerous Command Warn — intercept destructive commands
- Secret Redaction — API keys/passwords masked in output
- Incognito Session — no history, no DNA, no logs
- Clipboard auto-clear after configurable timeout
- No telemetry, no analytics, no data sent to Iris servers

---

## 14. Notifications

| Event | Notification | Actions |
|-------|-------------|---------|
| Long command finished | "✓ Command done — exit 0" | View Output |
| SSH connection dropped | "⚠️ prod-server disconnected" | Reconnect |
| Agent task completed | "Iris finished — 3 files changed" | View |
| Cron job finished | "Scheduled task: git pull done" | View Log |
| Agent Watch trigger | "CPU hit 94% on prod-server" | Open Terminal |
| Autopilot paused | "Waiting for your approval" | Approve / Cancel |

---

## 15. Current Status

### From Iris Code — Direct Port
```
✅ ProotRunner.kt
✅ UbuntuBootstrap.kt
✅ TerminalManager.kt
✅ AgentLoop.kt
✅ MultiStepStreamer.kt
✅ OpenAiProviderAdapter.kt
✅ WebSearchTool.kt
✅ BashTool.kt
✅ termux-view (vendored JNI)
✅ libtermux.so
✅ Visual Identity (colors, typography)
✅ Settings / API Vault architecture
✅ Hilt module structure
```

### To Build from Scratch
```
⬜ BlockEngine.kt — block-based output
⬜ SemanticParser.kt — output intelligence
⬜ GhostTextEngine.kt — inline autocomplete
⬜ KeyboardHandle.kt — toggle UI
⬜ ShortcutOverlay.kt — left/right picker
⬜ SessionPreviewSwipe.kt — shared element
⬜ SshjManager.kt — SSH client
⬜ SshKeyVault.kt — encrypted key store
⬜ CommandDnaDao.kt — FTS5 indexing
⬜ SessionReplay.kt — recording/playback
⬜ NaturalLanguageCron.kt — WorkManager cron
⬜ AgentWatch.kt — background conditions
⬜ HudEngine.kt — status widgets
⬜ ThemeStore.kt — theme engine
⬜ AliasManager.kt — shell alias sync
⬜ MultiExec.kt — broadcast SSH commands
⬜ WorkspaceManager.kt — project system
```

---

## 16. Open Decisions

| # | Decision | Options | Notes |
|---|----------|---------|-------|
| 1 | Command Shortcuts trigger | Gesture? Button? Dedicated key? | TBD |
| 2 | Shortcut Overlay trigger | Same as above | Linked to #1 |
| 3 | Session Navigator trigger | Long swipe? Button? Bottom sheet? | TBD |
| 4 | Ghost text confirmation | Pill tap vs sağa swipe | Pill leading candidate |
| 5 | App icon & splash | Eye concept? Shell concept? | TBD |
| 6 | Onboarding | How many steps? What to show? | TBD |
| 7 | Terminal background | Solid / blur / glassmorphism depth | TBD |
| 8 | General UI direction | Material You depth vs minimal | Partially decided: Material You + Unixporn + light glass |
| 9 | Live Share relay | Self-hosted? Third-party? | v1.1 concern |
| 10 | Workspace depth v1.0 | Full workflow builder or just project tagging? | TBD |

---

## 17. Reference Repositories

| System | Reference |
|--------|-----------|
| Terminal engine | `termux/termux-app` |
| Agent loop | `anomalyco/opencode` |
| SSH client | `hierynomus/sshj` |
| Diff | `java-diff-utils` |
| Iris Code | `mmuhofy/IrisCode` — primary reference |