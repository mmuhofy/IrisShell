package com.iris.irisshell.ui.terminal

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleEventObserver
import com.iris.irisshell.terminal.TerminalManager
import com.iris.irisshell.terminal.TerminalViewClientImpl
import com.iris.irisshell.terminal.UbuntuSetupState
import com.iris.irisshell.ui.input.InputBarHost
import com.iris.irisshell.ui.input.InputBarViewModel
import com.iris.irisshell.ui.session.SessionSwitcherSheet
import com.iris.irisshell.ui.session.SessionSwitcherViewModel
import com.iris.irisshell.ui.topbar.SessionSwitcherTopBar
import com.iris.irisshell.ui.block.BlockEngineViewModel
import com.iris.irisshell.ui.block.BlockTerminalView
import com.termux.view.TerminalView
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.util.Log
import android.content.Context
import android.view.inputmethod.InputMethodManager

@Composable
fun TerminalScreen(
    terminalManager: TerminalManager,
    ubuntuSetupState: UbuntuSetupState,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit = {},
    terminalViewModel: TerminalViewModel = hiltViewModel(),
    extraKeyState: com.iris.irisshell.terminal.ExtraKeyState? = null,
) {
    var showProgress by remember { mutableStateOf(false) }
    LaunchedEffect(ubuntuSetupState) {
        if (ubuntuSetupState is UbuntuSetupState.Ready) {
            showProgress = false
        } else {
            delay(300)
            showProgress = true
        }
    }

    when (ubuntuSetupState) {
        UbuntuSetupState.Idle,
        UbuntuSetupState.Extracting,
        UbuntuSetupState.Configuring,
        is UbuntuSetupState.InstallingPackages,
        is UbuntuSetupState.InstallingOhMyZsh,
        UbuntuSetupState.Optimizing -> {
            if (showProgress) {
                SetupProgress(state = ubuntuSetupState)
            }
        }
        UbuntuSetupState.Ready -> {
            ReadyScreen(
                terminalManager = terminalManager,
                terminalViewModel = terminalViewModel,
                onOpenSettings = onOpenSettings,
                extraKeyState = extraKeyState,
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
    onOpenSettings: () -> Unit,
    sessionSwitcherViewModel: SessionSwitcherViewModel = hiltViewModel(),
    blockEngineViewModel: BlockEngineViewModel = hiltViewModel(),
    inputBarViewModel: InputBarViewModel = hiltViewModel(),
    extraKeyState: com.iris.irisshell.terminal.ExtraKeyState? = null,
) {
    var fullscreen by remember { mutableStateOf(false) }
    var switcherOpen by remember { mutableStateOf(false) }
    val fontSizeSp by terminalViewModel.fontSizeSp.collectAsState()
    val sliderVisible by terminalViewModel.sliderVisible.collectAsState()
    val activeId by sessionSwitcherViewModel.activeId.collectAsState()
    val useBlockEngine by terminalViewModel.useBlockEngine.collectAsState()

    var keyboardFocused by remember { mutableStateOf(true) }
    val terminalViewRef = remember { mutableStateOf<TerminalView?>(null) }

    fun showKeyboard() {
        try {
            terminalViewRef.value?.let { view ->
                view.requestFocusFromTouch()
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
                keyboardFocused = true
            }
        } catch (e: Exception) {
            Log.e("TerminalScreen", "showKeyboard failed", e)
        }
    }

    fun hideKeyboard() {
        try {
            terminalViewRef.value?.let { view ->
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                val token = view.windowToken
                if (token != null) {
                    imm.hideSoftInputFromWindow(token, 0)
                    keyboardFocused = false
                }
            }
        } catch (e: Exception) {
            Log.e("TerminalScreen", "hideKeyboard failed", e)
        }
    }

    fun toggleKeyboard() {
        if (keyboardFocused) hideKeyboard() else showKeyboard()
    }

    // Session-switch entry animation
    val appearScale = remember { androidx.compose.animation.core.Animatable(1f) }
    val appearAlpha = remember { androidx.compose.animation.core.Animatable(1f) }
    LaunchedEffect(activeId) {
        appearScale.snapTo(0.92f)
        appearAlpha.snapTo(0f)
        coroutineScope {
            launch {
                appearScale.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                )
            }
            launch {
                appearAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium,
                    ),
                )
            }
        }
    }

    LaunchedEffect(sliderVisible) {
        if (sliderVisible) {
            delay(2500L)
            terminalViewModel.hideSlider()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (!fullscreen) {
            SessionSwitcherTopBar(
                viewModel = sessionSwitcherViewModel,
                isFullscreen = false,
                keyboardFocused = keyboardFocused,
                onToggleKeyboard = ::toggleKeyboard,
                onRefresh = {
                    terminalManager.currentSession?.finishIfRunning()
                    terminalManager.addTab()
                },
                onToggleFullscreen = { fullscreen = true },
                onClose = {
                    terminalManager.currentSession?.finishIfRunning()
                },
                onOpenSwitcher = { switcherOpen = true },
                onOpenSettings = onOpenSettings,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding(),
        ) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                if (useBlockEngine) {
                    val clipboard = androidx.compose.ui.platform.LocalClipboardManager.current
                    val hiddenIds by blockEngineViewModel.hiddenIds.collectAsState()
                    val exportRequest by blockEngineViewModel.exportRequest.collectAsState()
                    val clipboardEvent by blockEngineViewModel.clipboardRequest.collectAsState()
                    val pendingEdit by blockEngineViewModel.pendingEdit.collectAsState()

                    LaunchedEffect(exportRequest) {
                        if (exportRequest != null) {
                            blockEngineViewModel.consumeExportRequest()
                        }
                    }
                    LaunchedEffect(clipboardEvent) {
                        val event = clipboardEvent ?: return@LaunchedEffect
                        val text = when (event) {
                            is BlockEngineViewModel.ClipboardEvent.Command ->
                                "${event.prompt} ${event.command}"
                            is BlockEngineViewModel.ClipboardEvent.Output -> event.text
                        }
                        clipboard.setText(androidx.compose.ui.text.AnnotatedString(text))
                        blockEngineViewModel.consumeClipboardRequest()
                    }

                    val visibleBlocks = remember {
                        kotlinx.coroutines.flow.MutableStateFlow(
                            emptyList<com.iris.irisshell.domain.block.Block>()
                        )
                    }
                    val blocksRaw by blockEngineViewModel.blocks.collectAsState()
                    LaunchedEffect(blocksRaw, hiddenIds) {
                        visibleBlocks.value = blocksRaw.filterNot { it.id in hiddenIds }
                    }

                    val lastDir by blockEngineViewModel.lastDir.collectAsState()

                    BlockTerminalView(
                        blocks = visibleBlocks,
                        onToggleCollapsed = blockEngineViewModel::onToggleCollapsed,
                        onCommandSubmitted = blockEngineViewModel::onCommandSubmitted,
                        onCopyCommand = blockEngineViewModel::onCopyCommand,
                        onCopyOutput = blockEngineViewModel::onCopyOutput,
                        onRerunCommand = blockEngineViewModel::onRerunCommand,
                        onEditCommand = { cmd ->
                            blockEngineViewModel.onEditCommand(cmd)
                            blockEngineViewModel.consumePendingEdit()
                        },
                        onExportOutput = blockEngineViewModel::onExportOutput,
                        onDeleteBlock = blockEngineViewModel::onDeleteBlock,
                        promptLabel = lastDir,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = appearScale.value
                                scaleY = appearScale.value
                                alpha = appearAlpha.value
                            },
                    )
                } else {
                    TerminalViewHost(
                        terminalManager = terminalManager,
                        fontSizeSp = fontSizeSp,
                        terminalViewModel = terminalViewModel,
                        terminalViewRef = terminalViewRef,
                        extraKeyState = extraKeyState,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = appearScale.value
                                scaleY = appearScale.value
                                alpha = appearAlpha.value
                            },
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

            if (!fullscreen) {
                val inputBarState by inputBarViewModel.uiState.collectAsState()
                InputBarHost(
                    uiState = inputBarState,
                    onToggle = inputBarViewModel::toggleBarVisible,
                    onIntent = inputBarViewModel::onIntent,
                )
            }
        }

        if (!fullscreen && sliderVisible) {
            VerticalZoomSlider(
                value = fontSizeSp,
                onValueChange = { terminalViewModel.setFontSize(it) },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(end = 16.dp),
            )
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
    terminalViewModel: TerminalViewModel,
    terminalViewRef: MutableState<TerminalView?>,
    modifier: Modifier = Modifier,
    extraKeyState: com.iris.irisshell.terminal.ExtraKeyState? = null,
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val viewClient = remember(terminalViewModel, extraKeyState) {
        TerminalViewClientImpl(
            onScaleChange = { factor ->
                terminalViewModel.bumpFontSize(factor)
                terminalViewModel.showSlider()
                factor
            },
            extraKeyState = extraKeyState,
        )
    }

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