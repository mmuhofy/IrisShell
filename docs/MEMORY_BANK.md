# Iris Shell — Memory Bank
_Last updated: 2026-09-08_

## Project
Android terminal emulator with block-based output, PRoot Linux environment, and agent intelligence. Package: `com.iris.irisshell`.

## Stack
Kotlin 2.2.0, Compose BOM 2026.04.01, Hilt 2.57, Room 2.8.4, Kotlinx Serialization 1.7.x, WorkManager 2.11.2, SSHJ 0.38.x, OkHttp 4.12.x.

## Architecture Rules
- `domain/` — pure Kotlin, no Android imports, repository interfaces, use cases, `UrlDetector`, `IrisTool`
- `data/` — implements `domain` interfaces (Room DAOs, DataStore, SSHJ adapters)
- `terminal/` — depends on `domain/` only; termux-view JNI bridge, block engine, semantic parser, `TerminalManager`, `ProotRunner`
- `agent/` — depends on `domain/` only (DI via Hilt)
- `ui/` — depends on `domain/` and `core/` only; never imports `terminal/`, `data/`, `agent/`, or `ssh/` directly
- `ssh/` — isolated module, accessed via `domain/` use cases
- `di/` — Hilt modules only, no logic
- `util/` — constants, extensions

## Completed Features

### Terminal Link Detection
- `UrlDetector.kt` in `domain/` with `findUrls(text)`, `matches(word)`, `normalizeUrlFromWord(word)`
- `BlockBody.kt` — URLs rendered with `IrisPrimary` color + `TextDecoration.Underline`, clickable
- `BlockCard.kt` — `onUrlClick: (String) -> Unit` parameter
- `BlockTerminalView.kt` — `onUrlClick` parameter passed through
- `TerminalViewClientImpl.kt` — `onSingleTapUp` uses `UrlDetector.matches()` + `onUrlClick` instead of `Intent.ACTION_VIEW`
- `TerminalScreen.kt` — `browserUrl` state + `WebViewSheet`
- `WebViewSheet.kt` in `ui/browser/` — `ModalBottomSheet` + `WebView` via `AndroidView`

### PRoot Start Command Setting
- `SettingsRepository` interface + `SettingsRepositoryImpl` — `prootStartCommand: StateFlow<String>` + `setProotStartCommand(command: String)`
- `ProotRunner.build(startCommand: String = "")` — splits start command into argv tokens
- `TerminalManager` — collects `prootStartCommand` Flow via `onEach` + `launchIn`
- `TerminalViewModel` — `prootStartCommand` StateFlow + `setProotStartCommand()`
- `SettingsViewModel` — `prootStartCommand` StateFlow + `setProotStartCommand()`
- `SettingsScreen.kt` — "Gelişmiş" section with `ProotStartCommandRow` (experimental/dangerous warning banner)

## Active
- WebViewSheet sizing — verify `weight(1f)` on `AndroidView` fills remaining space, no overlap with top bar

## Open Decisions
- (none currently)
