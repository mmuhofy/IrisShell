package com.iris.irisshell

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.iris.irisshell.terminal.TerminalManager
import com.iris.irisshell.terminal.UbuntuBootstrap
import com.iris.irisshell.terminal.UbuntuSetupState
import com.iris.irisshell.ui.terminal.TerminalScreen
import com.iris.irisshell.ui.theme.IrisTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Single-activity entry point.
 *
 * Phase 1 — Terminal Core. We render TerminalScreen directly without any
 * navigation graph or onboarding flow. Once Phase 2 introduces session
 * preview / sessions home, this Activity gets wrapped in NavHost.
 *
 * Behaviour so that the system "notification shade auto-pulled" gesture does
 * not start the activity when the user first unlocks:
 *   - WindowCompat.setDecorFitsSystemWindows(false, window) — full edge-to-edge
 *   - WindowInsetsControllerCompat hides the status bar
 *   - The Activity never allows the system to lift it from recents to the
 *     notification shade because the activity has FLAG_SHOW_WHEN_LOCKED and
 *     uses cutout area exclusively.
 *   - We do NOT pull the notification panel programmatically.
 *
 * Ported from mmuhofy/IrisCode — app/src/main/kotlin/com/iris/iriscode/MainActivity.kt
 * Adapted for Iris Shell — com.iris.irisshell
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var terminalManager: TerminalManager
    @Inject lateinit var ubuntuBootstrap: UbuntuBootstrap

    private val setupState = MutableStateFlow<UbuntuSetupState>(UbuntuSetupState.Idle)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge: app draws under the status / navigation bars.
        enableEdgeToEdge()

        // Hide the system bars during startup so the user does not see a
        // partial-shade opening splash with the notification panel pulled
        // down. Bars will re-appear once the Activity finishes the first
        // composition pass and the user starts interacting.
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // On Android 11+ allow window to extend into the display cutout area
        // when status bar is hidden — keeps the terminal view edge-to-edge.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        // Kick off PRoot / Ubuntu bootstrap install on first launch. The
        // bootstrap runs in IO context inside UbuntuBootstrap; we just
        // forward the resulting state to the StateFlow the Composable reads.
        startSetup()

        setContent {
            IrisTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootScreen(
                        setupState = setupState,
                        terminalManager = terminalManager,
                        onRetry = ::startSetup,
                    )
                }
            }
        }
    }

    private fun startSetup() {
        lifecycleScope.launch {
            ubuntuBootstrap.install(
                installPackages = true,
                optimize = true,
            ) { state ->
                setupState.value = state
            }
        }
    }
}

@Composable
private fun RootScreen(
    setupState: StateFlow<UbuntuSetupState>,
    terminalManager: TerminalManager,
    onRetry: () -> Unit,
) {
    val state by setupState.collectAsState()
    TerminalScreen(
        terminalManager = terminalManager,
        ubuntuSetupState = state,
        onRetry = onRetry,
    )
}
