package com.iris.irisshell.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.domain.input.ExtraKey

/**
 * A single on-screen extra-key button. Keys are edge-to-edge inside
 * the bar (no spacing between siblings) — only the pressed/armed
 * state paints a soft rounded highlight behind the glyph, matching
 * iOS keyboard key-cap behaviour rather than Material's outlined
 * button look.
 *
 * `stuckActive` mirrors the shared [StickyModifierState] armed flag
 * so CTRL/ALT render gold-tinted while armed.
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
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val highlight: Color = when {
        stuckActive -> IrisPrimary.copy(alpha = 0.18f)
        pressed -> IrisSurface.copy(alpha = 0.95f)
        else -> Color.Transparent
    }

    val glyphColor = when {
        stuckActive -> IrisPrimary
        pressed -> IrisText
        else -> IrisTextMuted
    }

    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 44.dp, minHeight = 40.dp)
            .pointerInput(interactionSource) {
                detectTapGestures(
                    onLongPress = { _ -> onLongPress() },
                    onTap = { _ -> onTap() },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 4.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(highlight),
        )
        Text(
            text = key.displayGlyph(),
            color = glyphColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
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
