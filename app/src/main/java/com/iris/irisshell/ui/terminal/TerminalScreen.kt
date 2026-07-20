package com.iris.irisshell.ui.terminal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleEventObserver
import com.iris.irisshell.terminal.TerminalManager
import com.iris.irisshell.terminal.TerminalViewClientImpl
import com.iris.irisshell.terminal.UbuntuSetupState
import com.termux.view.TerminalView

/**
 * Phase 1 Terminal screen - ported from mmuhofy/IrisCode
 *   app/src/main/kotlin/com/iris/iriscode/ui/terminal/TerminalScreen.kt
 *
 * Adapted for Iris Shell - com.iris.irisshell.
 *
 * Classical termux-style terminal - the bionic PTY is hosted in a flat
 * Compose AndroidView with no extra input bar. The user types via the
 * system soft keyboard or a hardware keyboard; the termux view routes
 * keys through its native IME bridge to the underlying bash.
 *
 * Behaviour preserved:
 *  - Setup-progress card while UbuntuBootstrap is installing / extract / etc.
 *  - AndroidView hosting the termux TerminalView
 *  - attachSession called when a session exists
 *  - Setup failure surfaces a retry button
 *
 * Removed in this commit:
 *  - The earlier Compose-based "input bar" caused crashes because it
 *    bypassed the termux IME bridge. Termux's TerminalView already has
 *    its own keyboard handler - duplicating Compose keys was redundant
 *    and broke the drawable resolution when select-text highlights fired.
 */
@Composable
fun TerminalScreen(
    terminalManager: TerminalManager,
    ubuntuSetupState: UbuntuSetupState,
    onRetry: () -> Unit,
) {
    when (ubuntuSetupState) {
        UbuntuSetupState.Idle,
        UbuntuSetupState.Extracting,
        UbuntuSetupState.Configuring,
        is UbuntuSetupState.InstallingPackages,
        is UbuntuSetupState.InstallingOhMyZsh,
        UbuntuSetupState.Optimizing -> {
            SetupProgress(state = ubuntuSetupState)
        }
        UbuntuSetupState.Ready -> {
            TerminalViewHost(terminalManager = terminalManager)
        }
        is UbuntuSetupState.Failed -> {
            SetupFailure(
                error = ubuntuSetupState.error,
                onRetry = onRetry,
            )
        }
    }
}

@Composable
private fun SetupProgress(state: UbuntuSetupState) {
    val label = when (state) {
        UbuntuSetupState.Idle -> "Preparing…"
        UbuntuSetupState.Extracting -> "Extracting Ubuntu rootfs…"
        UbuntuSetupState.Configuring -> "Configuring system…"
        is UbuntuSetupState.InstallingPackages ->
            if (state.message.isNotEmpty()) state.message
            else "Installing packages…"
        is UbuntuSetupState.InstallingOhMyZsh -> state.message
        UbuntuSetupState.Optimizing -> "Cleaning up…"
        UbuntuSetupState.Ready -> "Ready"
        is UbuntuSetupState.Failed -> state.error
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Text(
                text = "Setting up terminal",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
            )
        }
    }
}

@Composable
private fun SetupFailure(error: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Failed to set up terminal",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp),
            )
            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp),
            ) { Text("Retry") }
        }
    }
}

@Composable
private fun TerminalViewHost(terminalManager: TerminalManager) {
    val terminalViewRef = remember {
        androidx.compose.runtime.mutableStateOf<TerminalView?>(null)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    // The view client is captured once so its strong reference keeps the
    // TerminalViewClientImpl alive for the lifetime of the host composable.
    val viewClient = remember { TerminalViewClientImpl() }

    // Ensure at least one tab when the screen enters the Ready state.
    LaunchedEffect(Unit) {
        if (terminalManager.tabCount == 0) {
            terminalManager.addTab()
        }
    }

    // Hook the activity lifecycle to the termux view. The view does not
    // expose explicit onResume/onPause; this observer is a forward-compatible
    // hook for Phase 2 (animation suspend, bitmap release, etc.).
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ -> }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize(),
        factory = { ctx ->
            TerminalView(ctx, null).apply {
                setTextSize(12)
                isFocusable = true
                isFocusableInTouchMode = true
                setTerminalViewClient(viewClient)
                terminalManager.currentSession?.let { session ->
                    attachSession(session)
                }
                terminalManager.registerTerminalView(this, ctx)
                terminalViewRef.value = this
            }
        },
        update = { view ->
            terminalManager.currentSession?.let { session ->
                view.attachSession(session)
            }
            terminalManager.registerTerminalView(view, view.context)
            terminalViewRef.value = view
            view.requestFocus()
        },
    )
}
