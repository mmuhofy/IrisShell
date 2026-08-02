package com.iris.irisshell

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.iris.irisshell.domain.terminal.ObserveFirstLaunchUseCase
import com.iris.irisshell.domain.terminal.TriggerBootstrapUseCase
import com.iris.irisshell.terminal.TerminalManager
import com.iris.irisshell.terminal.UbuntuSetupState
import com.iris.irisshell.ui.setup.BootstrapStepperScreen
import com.iris.irisshell.ui.setup.SetupRecoveryScreen
import com.iris.irisshell.ui.setup.onboarding.OnboardingScreen
import com.iris.irisshell.ui.terminal.TerminalScreen
import com.iris.irisshell.ui.theme.IrisTheme
import com.iris.irisshell.ui.settings.SettingsScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-activity entry point.
 *
 * Phase 1 — Terminal Core, now with a full setup UX:
 *
 *  1. Observe [ObserveFirstLaunchUseCase.isCompleted] from DataStore.
 *  2. While loading → render a thin splash.
 *  3. `false` → push [OnboardingScreen]; on completion it flips the flag and
 *     kicks off [TriggerBootstrapUseCase.start].
 *  4. `true` → render [BootstrapStepperScreen]; it routes to
 *     [SetupRecoveryScreen] on failure or to [TerminalScreen] on Ready.
 *
 * Per AGENT.md §125-128 the UI never imports `terminal/UbuntuSetupState`
 * directly. The one remaining direct import is `UbuntuSetupState.Ready`,
 * passed to the Phase 1 [TerminalScreen] whose signature is locked. A
 * follow-up PR will refactor [TerminalScreen] to consume a `:domain` state
 * type and remove this seam.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var terminalManager: TerminalManager
    @Inject lateinit var firstLaunchUseCase: ObserveFirstLaunchUseCase
    @Inject lateinit var triggerBootstrap: TriggerBootstrapUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            IrisTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RootScreen()
                }
            }
        }
    }

    @Composable
    private fun RootScreen() {
        var firstCompleted by remember { mutableStateOf<Boolean?>(null) }
        var bootstrapReady by remember { mutableStateOf(false) }
        var bootstrapFailed by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            firstLaunchUseCase.isCompleted().collect { firstCompleted = it }
        }

        LaunchedEffect(firstCompleted) {
            if (firstCompleted == true) {
                triggerBootstrap.start()
            }
        }

        when {
            firstCompleted == null -> SplashScreen()

            firstCompleted == false -> OnboardingScreen(
                onCompleted = { firstCompleted = true },
            )

            bootstrapFailed -> SetupRecoveryScreen(
                viewModel = androidx.hilt.navigation.compose.hiltViewModel(),
            )

            !bootstrapReady -> {
                BootstrapStepperScreen(
                    onReady = { bootstrapReady = true },
                    onSetupFailed = { bootstrapFailed = true },
                )
            }

            else -> TerminalNavHost(
                terminalManager = terminalManager,
                onRetry = { triggerBootstrap.retry() },
            )
        }
    }

    @Composable
    private fun TerminalNavHost(
        terminalManager: TerminalManager,
        onRetry: () -> Unit,
    ) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "terminal",
        ) {
            composable("terminal") {
                TerminalScreen(
                    terminalManager = terminalManager,
                    ubuntuSetupState = UbuntuSetupState.Ready,
                    onRetry = onRetry,
                    onOpenSettings = { navController.navigate("settings") },
                )
            }
            composable("settings") {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

/** Tiny splash used until DataStore emits its first value. */
@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0C0C0C)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color = Color(0xFFE8C547),
            strokeWidth = 2.dp,
        )
    }
}
