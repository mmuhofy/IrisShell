package com.iris.irisshell.ui.setup.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.iris.irisshell.ui.setup.onboarding.scenes.ArchitectureScene
import com.iris.irisshell.ui.setup.onboarding.scenes.ReadyScene
import com.iris.irisshell.ui.setup.onboarding.scenes.WelcomeScene
import com.iris.irisshell.ui.setup.theme.SetupPalette
import kotlinx.coroutines.launch

/**
 * Three-scene onboarding flow with no HorizontalPager.
 *
 * Scene transitions are full-screen fade (220ms). On the last scene, Continue
 * finishes onboarding via [viewModel]. Skip is available everywhere and
 * always routes to the bootstrap stepper (i.e. skips the wizard but still
 * triggers bootstrap).
 *
 * The terminal backdrop is shared across scenes (per-scene content is
 * rendered on top of it). Onboarding does not mount a real TermuxView —
 * see `TerminalBackdrop` for the rationale.
 */
@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit,
    viewModel: com.iris.irisshell.ui.setup.OnboardingViewModel =
        androidx.hilt.navigation.compose.hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    var scene by remember { mutableStateOf(OnboardingSceneKind.Welcome) }
    val coroutineScope = rememberCoroutineScope()

    val advance: () -> Unit = {
        val next = scene.next()
        if (next != null) {
            scene = next
        } else {
            coroutineScope.launch {
                viewModel.finishOnboarding()
                onCompleted()
            }
        }
    }

    val skip: () -> Unit = {
        coroutineScope.launch {
            viewModel.finishOnboarding()
            onCompleted()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SetupPalette.Background),
    ) {
        AnimatedContent(
            targetState = scene,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220)) togetherWith
                    fadeOut(animationSpec = tween(180)))
            },
            label = "onboarding-scene",
        ) { current ->
            when (current) {
                OnboardingSceneKind.Welcome ->
                    WelcomeScene(onContinue = advance, onSkip = skip)
                OnboardingSceneKind.Architecture ->
                    ArchitectureScene(onContinue = advance, onSkip = skip)
                OnboardingSceneKind.Ready ->
                    ReadyScene(onContinue = advance, onSkip = skip)
            }
        }
    }
}
