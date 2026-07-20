package com.iris.irisshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.iris.irisshell.terminal.TerminalManager
import com.iris.irisshell.terminal.UbuntuBootstrap
import com.iris.irisshell.terminal.UbuntuSetupState
import com.iris.irisshell.ui.terminal.TerminalScreen
import com.iris.irisshell.ui.theme.IrisTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Single-activity entry point.
 *
 * Phase 1 — Terminal Core. We render TerminalScreen directly without any
 * navigation graph or onboarding flow. Once Phase 2 introduces session
 * preview / sessions home, this Activity gets wrapped in NavHost.
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
        enableEdgeToEdge()

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
