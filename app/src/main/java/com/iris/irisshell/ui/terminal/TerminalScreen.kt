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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.iris.irisshell.terminal.TerminalManager
import com.iris.irisshell.terminal.UbuntuSetupState
import com.termux.view.TerminalView

/**
 * Phase 1 Terminal screen — ported from mmuhofy/IrisCode
 *   app/src/main/kotlin/com/iris/iriscode/ui/terminal/TerminalScreen.kt
 *
 * Adapted for Iris Shell — com.iris.irisshell.
 *
 * The full IrisCode surface (tab bar + search overlay + fullscreen pill + font
 * scale slider, etc.) is intentionally simplified for the first commit: the
 * goal of Phase 1 is to confirm that the PRoot/Ubuntu bootstrap, Race-Loss
 * resolution, and PTY session all render output in Compose. Cosmetic UI lands
 * in Phase 2 (UI & Session System).
 *
 * Behaviour preserved:
 *  - Setup-progress card while UbuntuBootstrap is installing / extract / etc.
 *  - AndroidView hosting the termux TerminalView
 *  - attachSession called when a session exists
 *  - Setup failure surfaces a retry button
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

    // Ensure at least one tab when the screen enters the Ready state.
    LaunchedEffect(Unit) {
        if (terminalManager.tabCount == 0) {
            terminalManager.addTab()
        }
    }

    // Mirror Activity onResume/onPause to the termux TerminalView.
    DisposableEffect(lifecycleOwner, terminalViewRef.value) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> terminalViewRef.value?.onResume()
                Lifecycle.Event.ON_PAUSE -> terminalViewRef.value?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .then(Modifier),
        factory = { ctx ->
            TerminalView(ctx, null).apply {
                setTextSize(12)
                isFocusable = true
                isFocusableInTouchMode = true
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
