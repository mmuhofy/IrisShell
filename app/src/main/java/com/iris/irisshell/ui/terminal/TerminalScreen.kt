package com.iris.irisshell.ui.terminal

import android.content.Context
import android.util.Log
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleEventObserver
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.terminal.SearchHighlightOverlay
import com.iris.irisshell.terminal.TerminalManager
import com.iris.irisshell.terminal.TerminalViewClientImpl
import com.iris.irisshell.terminal.UbuntuSetupState
import com.iris.irisshell.ui.block.BlockEngineViewModel
import com.iris.irisshell.ui.block.BlockTerminalView
import com.iris.irisshell.ui.browser.WebViewSheet
import com.iris.irisshell.ui.input.InputBarHost
import com.iris.irisshell.ui.input.InputBarViewModel
import com.iris.irisshell.ui.search.DraggableSearchBar
import com.iris.irisshell.ui.search.SearchScope
import com.iris.irisshell.ui.session.SessionSidebar
import com.iris.irisshell.ui.session.SessionSwitcherViewModel
import com.iris.irisshell.ui.topbar.TerminalTopBar
import com.termux.view.TerminalView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

@Composable
fun TerminalScreen(
    terminalManager: TerminalManager,
    ubuntuSetupState: UbuntuSetupState,
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit = {},
    terminalViewModel: TerminalViewModel = hiltViewModel(),
    extraKeyState: com.iris.irisshell.terminal.ExtraKeyState? = null,
    onExit: () -> Unit = {},
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
                SetupProgress(
                    state = ubuntuSetupState,
                )
            }
        }

        UbuntuSetupState.Ready -> {
            ReadyScreen(
                terminalManager = terminalManager,
                terminalViewModel = terminalViewModel,
                onOpenSettings = onOpenSettings,
                extraKeyState = extraKeyState,
                onExit = onExit,
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
    onExit: () -> Unit,
    sessionSwitcherViewModel: SessionSwitcherViewModel = hiltViewModel(),
    blockEngineViewModel: BlockEngineViewModel = hiltViewModel(),
    inputBarViewModel: InputBarViewModel = hiltViewModel(),
    extraKeyState: com.iris.irisshell.terminal.ExtraKeyState? = null,
) {
    var fullscreen by remember { mutableStateOf(false) }
    var sidebarOpen by remember { mutableStateOf(false) }
    var browserUrl by remember { mutableStateOf<String?>(null) }

    var searchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var currentMatch by remember { mutableStateOf(1) }
    var searchScope by remember { mutableStateOf(SearchScope.GLOBAL) }
    var terminalLines by remember { mutableStateOf<List<Pair<String, String?>>>(emptyList()) }

    val scope = rememberCoroutineScope()
    val fontSizeSp by terminalViewModel.fontSizeSp.collectAsState()
    val sliderVisible by terminalViewModel.sliderVisible.collectAsState()
    val activeId by sessionSwitcherViewModel.activeId.collectAsState()
    val useBlockEngine by terminalViewModel.useBlockEngine.collectAsState()
    val shouldExit by sessionSwitcherViewModel.shouldExit.collectAsState()

    LaunchedEffect(shouldExit) {
        if (shouldExit) {
            // Defer by one yield to give SessionManagerAdapter.reconcile()
            // a chance to create a default session if Room is empty.
            // This prevents exit when the app process is reused after
            // the previous session was deleted.
            yield()

            if (sessionSwitcherViewModel.allSessions.value.isNotEmpty()) {
                return@LaunchedEffect
            }

            onExit()
        }
    }

    LaunchedEffect(searchActive, searchScope) {
        if (searchActive) {
            terminalLines = if (useBlockEngine) {
                val allBlocks = blockEngineViewModel.blocks.value
                if (searchScope == SearchScope.BLOCK) {
                    val currentBlock = blockEngineViewModel.runningBlock.value
                        ?: allBlocks.lastOrNull()
                    if (currentBlock != null) {
                        buildList {
                            if (currentBlock.prompt.isNotBlank()) add(currentBlock.prompt to currentBlock.id)
                            if (currentBlock.command.isNotBlank()) add(currentBlock.command to currentBlock.id)
                            currentBlock.outputLines.forEach { line ->
                                add(line to currentBlock.id)
                            }
                        }
                    } else {
                        emptyList()
                    }
                } else {
                    buildList {
                        for (block in allBlocks) {
                            if (block.prompt.isNotBlank()) add(block.prompt to block.id)
                            if (block.command.isNotBlank()) add(block.command to block.id)
                            block.outputLines.forEach { line ->
                                add(line to block.id)
                            }
                        }
                    }
                }
            } else {
                val text = terminalManager.currentSession?.emulator?.getScreen()
                    ?.getTranscriptText() ?: ""
                text.lines().map { it to null }
            }
        }
    }

    val matchIndices = remember(searchQuery, terminalLines) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            terminalLines.mapIndexedNotNull { index, (lineText, _) ->
                if (lineText.contains(searchQuery, ignoreCase = true)) index else null
            }
        }
    }
    val matchCount = matchIndices.size

    val currentMatchBlockId = if (matchIndices.isNotEmpty() && currentMatch <= matchIndices.size) {
        terminalLines[matchIndices[currentMatch - 1]].second
    } else {
        null
    }

    var keyboardFocused by remember { mutableStateOf(true) }

    /**
     * This reference is also used by the Liquid Glass extra-key surface.
     *
     * It is populated only after TerminalView has a valid attached size.
     * Therefore the backdrop implementation never receives a zero-sized
     * TerminalView during its normal initialization path.
     */
    val terminalViewRef = remember {
        mutableStateOf<TerminalView?>(null)
    }

    val searchOverlayRef = remember {
        mutableStateOf<SearchHighlightOverlay?>(null)
    }

    fun showKeyboard() {
        try {
            val view = terminalViewRef.value ?: run {
                Log.w(
                    "TerminalScreen",
                    "TerminalView is not ready, cannot show keyboard",
                )
                return
            }

            if (
                !view.isAttachedToWindow ||
                view.width <= 0 ||
                view.height <= 0
            ) {
                Log.w(
                    "TerminalScreen",
                    "TerminalView is not attached or has zero size, cannot show keyboard",
                )
                return
            }

            view.requestFocusFromTouch()

            val imm = view.context.getSystemService(
                Context.INPUT_METHOD_SERVICE,
            ) as InputMethodManager

            imm.showSoftInput(
                view,
                InputMethodManager.SHOW_IMPLICIT,
            )

            keyboardFocused = true
        } catch (e: Exception) {
            Log.e(
                "TerminalScreen",
                "showKeyboard failed",
                e,
            )
        }
    }

    fun hideKeyboard() {
        try {
            val view = terminalViewRef.value ?: run {
                Log.w(
                    "TerminalScreen",
                    "TerminalView is not ready, cannot hide keyboard",
                )
                return
            }

            if (
                !view.isAttachedToWindow ||
                view.width <= 0 ||
                view.height <= 0
            ) {
                Log.w(
                    "TerminalScreen",
                    "TerminalView is not attached or has zero size, cannot hide keyboard",
                )
                return
            }

            val imm = view.context.getSystemService(
                Context.INPUT_METHOD_SERVICE,
            ) as InputMethodManager

            val token = view.windowToken

            if (token != null) {
                imm.hideSoftInputFromWindow(
                    token,
                    0,
                )

                keyboardFocused = false
            }
        } catch (e: Exception) {
            Log.e(
                "TerminalScreen",
                "hideKeyboard failed",
                e,
            )
        }
    }

    fun toggleKeyboard() {
        val view = terminalViewRef.value ?: run {
            Log.w(
                "TerminalScreen",
                "TerminalView is not ready, cannot toggle keyboard",
            )
            return
        }

        if (
            !view.isAttachedToWindow ||
            view.width <= 0 ||
            view.height <= 0
        ) {
            Log.w(
                "TerminalScreen",
                "TerminalView is not attached or has zero size, cannot toggle keyboard",
            )
            return
        }

        if (keyboardFocused) {
            hideKeyboard()
        } else {
            showKeyboard()
        }
    }

    // Terminal content is always fully visible.
    val appearScale = 1f
    val appearAlpha = 1f

    LaunchedEffect(sliderVisible) {
        if (sliderVisible) {
            delay(2500L)
            terminalViewModel.hideSlider()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        /*
         * Terminal content fills all available space.
         *
         * The extra-key bar remains below the terminal in the normal layout,
         * while its Liquid Glass layer samples the classic TerminalView that
         * sits behind it.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(IrisBackground)
                .imePadding(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                if (useBlockEngine) {
                    val clipboard =
                        androidx.compose.ui.platform.LocalClipboardManager.current

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

                            is BlockEngineViewModel.ClipboardEvent.Output ->
                                event.text
                        }

                        clipboard.setText(
                            androidx.compose.ui.text.AnnotatedString(text),
                        )

                        blockEngineViewModel.consumeClipboardRequest()
                    }

                    val visibleBlocks = remember {
                        MutableStateFlow(
                            emptyList<com.iris.irisshell.domain.block.Block>(),
                        )
                    }

                    val blocksRaw by blockEngineViewModel.blocks.collectAsState()

                    LaunchedEffect(
                        blocksRaw,
                        hiddenIds,
                    ) {
                        visibleBlocks.value =
                            blocksRaw.filterNot {
                                it.id in hiddenIds
                            }
                    }

                    val lastDir by blockEngineViewModel.lastDir.collectAsState()

                BlockTerminalView(
                    blocks = visibleBlocks,
                        onToggleCollapsed =
                            blockEngineViewModel::onToggleCollapsed,
                        onCommandSubmitted =
                            blockEngineViewModel::onCommandSubmitted,
                        onCopyCommand =
                            blockEngineViewModel::onCopyCommand,
                        onCopyOutput =
                            blockEngineViewModel::onCopyOutput,
                        onRerunCommand =
                            blockEngineViewModel::onRerunCommand,
                        onEditCommand = { cmd ->
                            blockEngineViewModel.onEditCommand(cmd)
                            blockEngineViewModel.consumePendingEdit()
                        },
                        onExportOutput =
                            blockEngineViewModel::onExportOutput,
                        onDeleteBlock =
                            blockEngineViewModel::onDeleteBlock,
                        promptLabel = lastDir,
                         onUrlClick = { browserUrl = it },
                         searchQuery = if (searchActive && searchQuery.isNotBlank()) searchQuery else null,
                         currentMatchBlockId = currentMatchBlockId,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = appearScale
                                scaleY = appearScale
                                alpha = appearAlpha
                            },
                    )
                } else {
                    /*
                     * CLASSIC TERMINAL PATH
                     *
                     * terminalViewRef is shared with InputBarHost so the
                     * Liquid Glass surface can sample this exact TerminalView.
                     */
                      TerminalViewHost(
                         terminalManager = terminalManager,
                         fontSizeSp = fontSizeSp,
                         terminalViewModel = terminalViewModel,
                         terminalViewRef = terminalViewRef,
                         extraKeyState = extraKeyState,
                         onUrlClick = { browserUrl = it },
                         searchQuery = if (searchActive && searchQuery.isNotBlank()) searchQuery else null,
                         searchOverlayRef = searchOverlayRef,
                         modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = appearScale
                                scaleY = appearScale
                                alpha = appearAlpha
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

                    // NEW:
                    // Give the Liquid Glass renderer the classic TerminalView.
                    terminalView = terminalViewRef.value,
                )
            }
        }

        // Top bar overlay — floats on terminal, takes no layout space.
        if (!fullscreen) {
            TerminalTopBar(
                viewModel = sessionSwitcherViewModel,
                isFullscreen = fullscreen,
                keyboardFocused = keyboardFocused,
                onToggleKeyboard = ::toggleKeyboard,
                onOpenSidebar = {
                    hideKeyboard()
                    scope.launch {
                        delay(100)
                        sidebarOpen = true
                    }
                },
                onFindInOutput = {
                    hideKeyboard()
                    searchActive = true
                },
                onRefresh = {
                    terminalManager.currentSession?.finishIfRunning()
                    terminalManager.addTab()
                },
                onToggleFullscreen = {
                    fullscreen = true
                },
                onClose = {
                    terminalManager.currentSession?.finishIfRunning()
                },
                onOpenSettings = onOpenSettings,
            )
        }

        // Slider overlay.
        if (!fullscreen && sliderVisible) {
            VerticalZoomSlider(
                value = fontSizeSp,
                onValueChange = {
                    terminalViewModel.setFontSize(it)
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
            )
        }

        // Sidebar overlay.
        if (sidebarOpen) {
            BackHandler {
                sidebarOpen = false
            }

            SessionSidebar(
                isOpen = sidebarOpen,
                onDismiss = {
                    sidebarOpen = false
                },
                onOpenSettings = onOpenSettings,
            )
        }

        if (browserUrl != null) {
            BackHandler {
                browserUrl = null
            }
            WebViewSheet(
                url = browserUrl!!,
                onDismiss = { browserUrl = null },
            )
        }

        // Search overlay — draggable, top-center.
        if (searchActive) {
            BackHandler {
                searchActive = false
                searchQuery = ""
                currentMatch = 1
            }

            DraggableSearchBar(
                searchText = searchQuery,
                onSearchTextChange = { searchQuery = it },
                matchCount = matchCount,
                currentMatch = currentMatch,
                onNext = {
                    if (matchCount > 1) {
                        currentMatch = if (currentMatch < matchCount) currentMatch + 1 else 1
                    }
                },
                onPrev = {
                    if (matchCount > 1) {
                        currentMatch = if (currentMatch > 1) currentMatch - 1 else matchCount
                    }
                },
                onClose = {
                    searchActive = false
                    searchQuery = ""
                    currentMatch = 1
                    searchScope = SearchScope.GLOBAL
                },
                searchScope = searchScope,
                onToggleScope = {
                    searchScope = if (searchScope == SearchScope.GLOBAL) SearchScope.BLOCK else SearchScope.GLOBAL
                    currentMatch = 1
                },
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 64.dp),
            )
        }
    }
}

@Composable
private fun CompactFullscreenExit(
    onExitFullscreen: () -> Unit,
) {
    androidx.compose.material3.Surface(
        color = com.iris.irisshell.design.system.IrisSurface.copy(
            alpha = 0.85f,
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(
                    horizontal = 12.dp,
                    vertical = 6.dp,
                )
                .clickable(
                    onClick = onExitFullscreen,
                ),
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
private fun SetupProgress(
    state: UbuntuSetupState,
) {
    val label = when (state) {
        UbuntuSetupState.Idle ->
            "Preparing…"

        UbuntuSetupState.Extracting ->
            "Extracting Ubuntu rootfs…"

        UbuntuSetupState.Configuring ->
            "Configuring system…"

        is UbuntuSetupState.InstallingPackages ->
            if (state.message.isNotEmpty()) {
                state.message
            } else {
                "Installing packages…"
            }

        is UbuntuSetupState.InstallingOhMyZsh ->
            state.message

        UbuntuSetupState.Optimizing ->
            "Cleaning up…"

        UbuntuSetupState.Ready ->
            "Ready"

        is UbuntuSetupState.Failed ->
            state.error
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
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
                modifier = Modifier.padding(
                    top = 8.dp,
                    start = 32.dp,
                    end = 32.dp,
                ),
            )
        }
    }
}

@Composable
private fun SetupFailure(
    error: String,
    onRetry: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Failed to set up terminal",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
            )

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(
                    top = 8.dp,
                    start = 32.dp,
                    end = 32.dp,
                ),
            )

            Button(
                onClick = onRetry,
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Text("Retry")
            }
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
    onUrlClick: (String) -> Unit,
    searchQuery: String?,
    searchOverlayRef: MutableState<SearchHighlightOverlay?>,
    modifier: Modifier = Modifier,
    extraKeyState: com.iris.irisshell.terminal.ExtraKeyState? = null,
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val viewClient = remember(
        terminalViewModel,
        extraKeyState,
        onUrlClick,
    ) {
        TerminalViewClientImpl(
            onScaleChange = { factor ->
                terminalViewModel.bumpFontSize(factor)
                terminalViewModel.showSlider()
                factor
            },
            extraKeyState = extraKeyState,
            context = context,
            onUrlClick = onUrlClick,
        )
    }

    LaunchedEffect(fontSizeSp) {
        terminalViewRef.value?.setTextSize(fontSizeSp)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            // Lifecycle hook intentionally kept here.
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),

        factory = { ctx ->
            val frameLayout = android.widget.FrameLayout(ctx).apply {
                layoutParams = android.widget.FrameLayout.LayoutParams(
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                    android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                )
            }

            val tv = TerminalView(ctx, null).apply {
                setTextSize(fontSizeSp)
                isFocusable = true
                isFocusableInTouchMode = true
                setTerminalViewClient(viewClient)
                terminalManager.currentSession?.let { session ->
                    attachSession(session)
                }
                terminalManager.registerTerminalView(this, ctx)
                val listener =
                    object : ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            if (width > 0 && height > 0 && isAttachedToWindow) {
                                viewTreeObserver.removeOnGlobalLayoutListener(this)
                                terminalViewRef.value = this@apply
                            }
                        }
                    }
                viewTreeObserver.addOnGlobalLayoutListener(listener)
            }

            val overlay = SearchHighlightOverlay(ctx).apply {
                terminalView = tv
                updateQuery(searchQuery)
            }

            frameLayout.addView(tv)
            frameLayout.addView(overlay)
            searchOverlayRef.value = overlay

            frameLayout
        },

        update = { _ ->
            val tv = terminalViewRef.value
            val overlay = searchOverlayRef.value

            tv?.setTextSize(fontSizeSp)
            terminalManager.currentSession?.let { session ->
                tv?.attachSession(session)
            }
            tv?.let { terminalManager.registerTerminalView(it, it.context) }
            if (tv != null && tv.isAttachedToWindow && tv.width > 0 && tv.height > 0) {
                tv.requestFocus()
            }

            overlay?.updateQuery(searchQuery)
        },
    )
}