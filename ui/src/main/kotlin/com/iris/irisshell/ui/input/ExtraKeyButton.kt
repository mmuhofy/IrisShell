package com.iris.irisshell.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.domain.input.ExtraKey

/**
 * A single on-screen extra-key button. Tap fires [onTap]; long-press
 * fires [onLongPress]. While the modifier behind this button is
 * "sticky" (CTRL/ALT armed), [stuckActive] highlights it in the gold
 * accent — this mirrors Termux's `SpecialButtonState.setIsActive`
 * color swap.
 *
 * Long-press detection is hand-rolled with `detectTapGestures` so we
 * can fire on press-down rather than press-up (snappier UX, matches
 * Termux's `DEFAULT_LONG_PRESS_DURATION`). The delay defaults to
 * Termux's `FALLBACK_LONG_PRESS_DURATION = 400ms` so the popup appears
 * in roughly the same window.
 *
 * UNTESTED — verify on device.
 */
@Composable
fun ExtraKeyButton(
    key: ExtraKey,
    stuckActive: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    longPressDelayMs: Long = LONG_PRESS_DELAY_MS,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 44.dp, minHeight = 36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (stuckActive) IrisSurface else IrisBackground)
            .clickable(
                interactionSource = interactionSource,
                role = Role.Button,
                onClick = onTap,
            )
            .pointerInput(key) {
                detectTapGestures(
                    onLongPress = { _ -> onLongPress() }
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = key.displayGlyph(),
            color = if (stuckActive) IrisPrimary else IrisTextMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private fun ExtraKey.displayGlyph(): String = when (this) {
    is ExtraKey.Special -> name
    is ExtraKey.Text -> glyph
    is ExtraKey.Navigation -> when (this) {
        ExtraKey.Navigation.ESC -> "ESC"
        ExtraKey.Navigation.TAB -> "TAB"
        ExtraKey.Navigation.ARROW_LEFT -> "←"
        ExtraKey.Navigation.ARROW_RIGHT -> "→"
        ExtraKey.Navigation.ARROW_UP -> "↑"
        ExtraKey.Navigation.ARROW_DOWN -> "↓"
        ExtraKey.Navigation.HOME -> "HOME"
        ExtraKey.Navigation.END -> "END"
        ExtraKey.Navigation.PAGE_UP -> "PgUp"
        ExtraKey.Navigation.PAGE_DOWN -> "PgDn"
    }
}

private const val LONG_PRESS_DELAY_MS: Long = 400L
private const val LONG_PRESS_POLL_MS: Long = 32L
