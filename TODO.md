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

### Completed (2026-09-06 — closeTab + app exit + relaunch fix)
- [x] Fix closeTab: remove `if (irisSessions.size <= 1) return` guard
- [x] Add shouldExit StateFlow to SessionRepository (domain + data)
- [x] SessionManagerAdapter.reconcile: create default when Room empty (survives relaunch)
- [x] onLastSessionExited: signal app exit instead of creating default
- [x] SessionSwitcherViewModel: expose shouldExit
- [x] TerminalScreen/ReadyScreen: onExit callback → Activity.finish()
- [x] MainActivity: pass onExit via LocalContext
- [x] Fix relaunch crash: yield() guard + start() shouldExit reset

### Completed (Onboarding Wizard — 2026-09-06/07)
- [x] Design system components: DroshLogo, SetupButton, DeviceCheckItem, ShellSelector, PackageProfileSelector
- [x] WelcomeScene: DroshLogo + "Başla →" + SkipAnchor (replaced TerminalBackdrop)
- [x] DeviceCheckScene: auto-scan arch/Android/storage/RAM/battery with status dots
- [x] PreferencesScene: name field (hoisted state) + ShellSelector + PackageProfileSelector
- [x] ShellSetupScene: Oh My Zsh progress bar + step indicators (conditional — Zsh only)
- [x] OnboardingSceneKind: 4 scenes (Welcome → DeviceCheck → Preferences → ShellSetup)
- [x] OnboardingScreen: state hoisting for userName/shellChoice/packageProfile/customPackages; conditional ShellSetup skip for Bash
- [x] GitHub Actions build passing (fix Kotlin if-else syntax, drawArc params, Surface lambda brace, KeyboardOptions API)
- [x] Runtime crash fix: `weight(0f)` in ShellSelector Canvas Box → removed
- [x] Runtime crash fix: `lucide_square_terminal.xml` missing width/height → added 24dp
- [x] Preferences connected to real bootstrap: OnboardingViewModel.start(preferences) → TriggerBootstrap → BootstrapStatePort → UbuntuBootstrap.install(preferences)
- [x] set-default-shell.sh only runs for Zsh (Bash users got proot `/bin/zsh not found` error)
- [x] isInstalled check no longer requires bin/zsh (needed for Bash users)
- [x] bashrc-write.sh created for Bash path
- [x] packages-install.sh reads IRIS_CUSTOM_PACKAGES env var for Custom profile
- [x] ShellChoice + PackageProfile enums moved from ui/ to domain/
- [x] SetupPreferences data class created in domain/

### PTY / Terminal
- [ ] Verify PTY session creation with PRoot + Ubuntu rootfs
- [ ] Handle PTY session resize on orientation change
- [ ] Terminal font loading from custom .ttf files

### Terminal Link Detection + In-App Browser (2026-09-08)
- [x] UrlDetector — shared URL detection utility (domain/UrlDetector.kt)
- [x] Block engine: colored + underlined + clickable URLs in BlockBody.kt
- [x] Classic terminal: tap detection callback in TerminalViewClientImpl.kt (replaces external Intent)
- [x] WebViewSheet — minimal bottom sheet browser (WebView + top bar + close)
- [x] TerminalScreen.kt: browserUrl state + BackHandler + wiring to both rendering paths

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
