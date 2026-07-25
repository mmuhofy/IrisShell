package com.iris.irisshell.ui.setup.onboarding.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.ui.setup.theme.SetupPalette

/**
 * Fullscreen fake terminal used as the visual backdrop of every onboarding
 * scene.
 *
 * Why fake (not a real Termux-mount) — at onboarding time the terminal is not
 * yet attached to a session, and the proot'd Ubuntu rootfs hasn't been
 * bootstrapped. A Compose-canvas placeholder lets us sell the "show, don't
 * tell" experience without risking a half-mounted TermuxView.
 *
 * Optional lines are drawn over the same backdrop via [lines]; their y-offset
 * increases per line. The cursor blinks at the bottom.
 */
@Composable
fun TerminalBackdrop(
    lines: List<String>,
    cursorPosition: CursorPos,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SetupPalette.Background)
            .padding(horizontal = 28.dp, vertical = 80.dp),
    ) {
        if (lines.isNotEmpty()) {
            lines.forEachIndexed { index, line ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = (index * 28).dp),
                ) {
                    androidx.compose.material3.Text(
                        text = line,
                        color = SetupPalette.Text,
                        style = terminalTextStyle(fontSize = 14.sp),
                        modifier = Modifier,
                    )
                }
            }
        }
        CursorBlink(
            line = cursorPosition.line,
            column = cursorPosition.column,
        )
    }
}

data class CursorPos(val line: Int, val column: Int)

@Composable
private fun CursorBlink(line: Int, column: Int) {
    val infinite = rememberInfiniteTransition(label = "cursor")
    val alpha by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cursor-alpha",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = (line * 28).dp, start = (column * 9).dp),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(10.dp)
                .background(SetupPalette.Text.copy(alpha = alpha)),
        )
    }
}

internal fun terminalTextStyle(fontSize: androidx.compose.ui.unit.TextUnit): TextStyle =
    TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontSize = fontSize,
        letterSpacing = 0.sp,
    )
