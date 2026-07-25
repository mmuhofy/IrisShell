package com.iris.irisshell.ui.setup.onboarding.scenes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.iris.irisshell.ui.setup.onboarding.components.CursorPos
import com.iris.irisshell.ui.setup.onboarding.components.OnboardingOverlay
import com.iris.irisshell.ui.setup.onboarding.components.SkipAnchor
import com.iris.irisshell.ui.setup.onboarding.components.TerminalBackdrop
import com.iris.irisshell.ui.setup.onboarding.components.terminalTextStyle
import com.iris.irisshell.ui.setup.theme.SetupPalette
import kotlinx.coroutines.delay

/**
 * Scene 1 — Welcome.
 *
 * Fake terminal backdrop shows a blinking prompt. After 1.4s the line
 * "whoami" simulates being typed, and 700ms later "root" appears as the
 * response. The caption invites the user to run that themselves once the
 * bootstrap is over.
 */
@Composable
fun WelcomeScene(
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var lines by remember { mutableStateOf(listOf<String>()) }
    var cursorLine by remember { mutableStateOf(0) }
    var cursorCol by remember { mutableStateOf(1) }

    LaunchedEffect(Unit) {
        lines = lines + "$"
        cursorLine = 0
        cursorCol = 2
        delay(900L)
        // Type "whoami" one char at a time using a synthetic typing step.
        val typing = "whoami"
        var typed = ""
        for (ch in typing) {
            typed += ch
            lines = lines.dropLast(1) + (lines.last() + ch)
            cursorCol += 1
            delay(85L)
        }
        delay(400L)
        lines = lines + "root"
        cursorLine = lines.size - 1
        cursorCol = 0
        delay(700L)
        lines = lines + "$"
        cursorLine = lines.size - 1
        cursorCol = 1
    }

    Box(modifier = modifier.fillMaxSize()) {
        TerminalBackdrop(
            lines = lines,
            cursorPosition = CursorPos(cursorLine, cursorCol),
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            // Centered hero text — appears above the terminal feed as
            // ambient framing. Respects Iris aesthetic: monospace, muted.
            Text(
                text = "iris shell",
                color = SetupPalette.Text,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    letterSpacing = 2.sp,
                ),
            )
        }
        SkipAnchor(onSkip = onSkip)
        OnboardingOverlay(
            caption = "Run your first command. Try `whoami`. The terminal\n" +
                "wakes up after setup finishes.",
            continueLabel = "Continue",
            onContinue = onContinue,
        )
    }
}
