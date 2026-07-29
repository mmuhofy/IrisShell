package com.iris.irisshell.ui.terminal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleEventObserver
import com.iris.irisshell.terminal.TerminalManager
import com.iris.irisshell.terminal.TerminalViewClientImpl
import com.iris.irisshell.terminal.UbuntuSetupState
import com.iris.irisshell.ui.session.SessionSwitcherSheet
import com.iris.irisshell.ui.session.SessionSwitcherViewModel
import com.iris.irisshell.ui.topbar.SessionSwitcherTopBar
import com.termux.view.TerminalView

/**
 * Phase 1 Terminal screen.
 *
 * Inspired by mmuhofy/IrisCode — app/src/main/kotlin/.../TerminalScreen.kt
 * Adapted for Iris Shell — com.iris.irisshell.
 *
 * Layout (when [ubuntuSetupState] == Ready):
 *   ┌──────────────┐
 *   │  TopBar      │   ← TerminalTopBar (MoreActionsMenu)
 *   ├──────────────┤
 *   │              │
 *   │  Terminal    │   ← Pinch-to-zoom → font size persists via VM
 *   │              │
 *   │          ║A║ │   ← VerticalZoomSlider (right edge, auto-hide)
 *   └──────────────┘
 *
 * Fullscreen (long-press "Enter fullscreen" in the topbar menu) hides both
 * the topbar and the slider so the termux view gets the whole screen.
 */
@Composable
fun TerminalScreen(
    terminalManager: TerminalManager,
    ubuntuSetupState: UbuntuSetupState,
    onRetry: () -> Unit,
    terminalViewModel: TerminalViewModel = hiltViewModel(),
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
            ReadyScreen(
                terminalManager = terminalManager,
                terminalViewModel = terminalViewModel,
            )
        }
        is UbuntuSetupState.Failed -> {
            SetupFailure(
                error = ubuntuSetupState.error,
                onRetry = onRetry,
            )
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun ReadyScreen(
    terminalManager: TerminalManager,
    terminalViewModel: TerminalViewModel,
    sessionSwitcherViewModel: SessionSwitcherViewModel = hiltViewModel(),
) {
    var fullscreen by remember { mutableStateOf(false) }
    var switcherOpen by remember { mutableStateOf(false) }
    val fontSizeSp by terminalViewModel.fontSizeSp.collectAsState()
    val sliderVisible by terminalViewModel.sliderVisible.collectAsState()

    // Animate font-size transitions so the terminal content scales up/down
    // smoothly instead of snapping. baseline sp = the size the AndroidView
    // is configured at (DEFAULT_FONT_SP). currentSp animates from there.
    val baselineSp = DEFAULT_FONT_SP
    val animatedSp by androidx.compose.animation.core.animateFloatAsState(
        targetValue = fontSizeSp.toFloat(),
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
        ),
        label = "terminal-font-scale",
    )
    val terminalScale = (animatedSp / baselineSp).coerceIn(0.7f, 2.0f)

    Column(modifier = Modifier.fillMaxSize()) {
        if (!fullscreen) {
            SessionSwitcherTopBar(
                viewModel = sessionSwitcherViewModel,
                isFullscreen = false,
                onRefresh = {
                    terminalManager.currentSession?.finishIfRunning()
                    terminalManager.addTab()
                },
                onToggleFullscreen = { fullscreen = true },
                onClose = {
                    terminalManager.currentSession?.finishIfRunning()
                },
                onOpenSwitcher = { switcherOpen = true },
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures(
                        onGestureEnd = {
                            // Lift detected → start the auto-hide timer
                            // for the zoom slider.
                            terminalViewModel.onPinchEnd()
                        },
                    ) { _, _, zoom, _ ->
                        if (zoom != 1f) {
                            terminalViewModel.bumpFontSize(zoom)
                            terminalViewModel.showSlider()
                        }
                    }
                },
        ) {
            // Smooth scale wrap so the AndroidView (which only knows about
            // integer sp sizes) eases between font sizes. Pinch the view to
            // animatedSp; the underlying setTextSize still snaps in steps
            // but the visible scale animates the transition.
            TerminalViewHost(
                terminalManager = terminalManager,
                fontSizeSp = fontSizeSp,
                modifier = androidx.compose.ui.graphics.graphicsLayer {
                    scaleX = terminalScale
                    scaleY = terminalScale
                },
            )

            if (!fullscreen && sliderVisible) {
                VerticalZoomSlider(
                    value = fontSizeSp,
                    onValueChange = { terminalViewModel.setFontSize(it) },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                )
            }

            if (fullscreen) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    contentAlignment = Alignment.TopStart,
                ) {
                    CompactFullscreenExit {
                        fullscreen = false
                    }
                }
            }
        }
    }

    if (switcherOpen) {
        SessionSwitcherSheet(onDismiss = { switcherOpen = false })
    }
}

@Composable
private fun CompactFullscreenExit(onExitFullscreen: () -> Unit) {
    androidx.compose.material3.Surface(
          color = com.iris.irisshell.design.system.IrisSurface.copy(alpha = 0.85f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable(onClick = onExitFullscreen),
        ) {
            Text(
                text = "Tap to exit fullscreen",
                  color = com.iris.irisshell.design.system.IrisTextSecondary,
                style = MaterialTheme.typography.labelMedium,
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

private const val TERMINAL_PINCH_THRESHOLD = 0.04f

@Composable
private fun TerminalViewHost(
    terminalManager: TerminalManager,
    fontSizeSp: Int,
    modifier: Modifier = Modifier,
) {
    val terminalViewRef = remember {
        androidx.compose.runtime.mutableStateOf<TerminalView?>(null)
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewClient = remember { TerminalViewClientImpl() }

    LaunchedEffect(Unit) {
        if (terminalManager.tabCount == 0) {
            terminalManager.addTab()
        }
    }

    LaunchedEffect(fontSizeSp) {
        terminalViewRef.value?.setTextSize(fontSizeSp)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ -> }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            TerminalView(ctx, null).apply {
                setTextSize(fontSizeSp)
                isFocusable = true
                isFocusableInTouchMode = true
                setTerminalViewClient(viewClient)
                terminalManager.currentSession?.let { session -> attachSession(session) }
                terminalManager.registerTerminalView(this, ctx)
                terminalViewRef.value = this
            }
        },
        update = { view ->
            view.setTextSize(fontSizeSp)
            terminalManager.currentSession?.let { session -> view.attachSession(session) }
            terminalManager.registerTerminalView(view, view.context)
            terminalViewRef.value = view
            view.requestFocus()
        },
    )
}
