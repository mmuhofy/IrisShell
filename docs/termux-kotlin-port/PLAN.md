# Termux Java → Kotlin Port Plan

## Source

- **Inspiration repo**: `github.com/reapercanuk39/termux-kotlin-app`
  - A full Java→Kotlin conversion of `termux/termux-app`, kept under the
    `com.termux.*` package names for upstream package compatibility.
  - Cloned locally to `/tmp/opencode/termux-kotlin-app` for reference.

## Motivation

- We're vendoring `com.termux.terminal.*` and `com.termux.view.*` from the
  official Java Termux app. The codebase is otherwise 100% Kotlin.
- Kotlin versions exist, drop-in compatible, identical package names.
- ~30% line-count reduction (1838 vs 2617 for `TerminalEmulator`) just
  from `val`/`var` + property syntax — no logic differences.

## Mapping (1:1, exact package names preserved)

| Current (Java) | Kotlin fork | Notes |
|---|---|---|
| `com.termux.terminal.ByteQueue` | `ByteQueue.kt` | Drop-in |
| `com.termux.terminal.JNI` | `JNI.kt` | Same native calls |
| `com.termux.terminal.KeyHandler` | `KeyHandler.kt` | |
| `com.termux.terminal.Logger` | `Logger.kt` | |
| `com.termux.terminal.TerminalBuffer` | `TerminalBuffer.kt` | |
| `com.termux.terminal.TerminalColorScheme` | `TerminalColorScheme.kt` | |
| `com.termux.terminal.TerminalColors` | `TerminalColors.kt` | |
| `com.termux.terminal.TerminalEmulator` | `TerminalEmulator.kt` | 2617 → 1838 lines |
| `com.termux.terminal.TerminalOutput` | `TerminalOutput.kt` | |
| `com.termux.terminal.TerminalRow` | `TerminalRow.kt` | |
| `com.termux.terminal.TerminalSession` | `TerminalSession.kt` | |
| `com.termux.terminal.TerminalSessionClient` | `TerminalSessionClient.kt` | |
| `com.termux.terminal.TextStyle` | `TextStyle.kt` | |
| `com.termux.terminal.WcWidth` | `WcWidth.kt` | |
| `com.termux.view.GestureAndScaleRecognizer` | `GestureAndScaleRecognizer.kt` | |
| `com.termux.view.TerminalRenderer` | `TerminalRenderer.kt` | |
| `com.termux.view.TerminalView` | `TerminalView.kt` | |
| `com.termux.view.TerminalViewClient` | `TerminalViewClient.kt` | |
| `com.termux.view.support.PopupWindowCompatGingerbread` | `PopupWindowCompatGingerbread.kt` | |
| `com.termux.view.textselection.CursorController` | `CursorController.kt` | |
| `com.termux.view.textselection.TextSelectionCursorController` | `TextSelectionCursorController.kt` | |
| `com.termux.view.textselection.TextSelectionHandleView` | `TextSelectionHandleView.kt` | |

**22 files** to convert. None of our own Kotlin files in `:terminal` are
touched (`TerminalManager`, `ProotRunner`, `UbuntuBootstrap`, etc.).

## What changes for `:terminal` and the rest of the app

- **Nothing.** Package names preserved → every import statement in
  `:app`, `:ui`, `:data` keeps working unchanged.
- JNI / native code (`termux.c`, `Android.mk`, `jniLibs/`) is identical
  in both repos — no rebuild needed for native libs.
- Behaviour is byte-for-byte equivalent — same upstream version the Java
  sources were vendored from.

## Plan

### Step 1: Replace Java files in-place
- For each `.java` file, copy the corresponding `.kt` from
  `/tmp/opencode/termux-kotlin-app/terminal-{emulator,view}/src/main/kotlin/...`
  into `terminal/src/main/kotlin/com/termux/...` (NOT `java/`).
- Delete the original `.java` file from `terminal/src/main/java/com/termux/...`.

### Step 2: Verify the build
- `./gradlew :terminal:assembleDebug` — `:terminal` should compile.
- Then `:app:assembleDebug` — full app builds.
- CI matrix still green.

### Step 3: Block output engine (Phase 6 deferred)
Once Termux is Kotlin and the build is green, the block output work in
`docs/block-engine/PLAN.md` becomes approachable — easier to extend the
buffer model in Kotlin than Java.

## Risks

- **Low**: identical package names + identical upstream means behaviour
  should match. The conversion is mechanical (`val` instead of getters,
  `companion object` instead of static, `when` instead of switch).
- **Compile-time**: Kotlin nullable types are stricter than Java. If we
  were calling these classes from anywhere passing `null` where the Java
  API didn't annotate it, Kotlin will refuse. We can grep for that, but
  our usage is limited to `TerminalView`, `TerminalViewClient`,
  `TerminalSession`, `TerminalEmulator` — all already nullable-safe in our
  existing Kotlin wrapper classes.
- **Tests**: `:terminal` has no unit tests today. The fork ships a few
  tests under `terminal-emulator/src/test/java/com/termux/terminal/ApcTest.java`.
  We don't have to port those — they're not part of our build. If we
  want them, that's a separate step.

## Verification

After each batch:

- `./gradlew :terminal:compileDebugKotlin` succeeds.
- No new warnings vs. baseline.
- Manual smoke: open the app, switcher still opens, tap activates a
  session, terminal renders, pinch zooms, delete works.

## Scope boundary

This port is **just Java→Kotlin conversion of vendored Termux code**.
No new features. No JNI changes. No API changes. After it lands,
Phase 6 (block output engine) becomes a Kotlin-native effort.
