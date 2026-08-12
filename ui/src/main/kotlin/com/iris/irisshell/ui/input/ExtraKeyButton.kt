package com.iris.irisshell.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.domain.input.ExtraKey
import com.iris.irisshell.ui.R

/**
 * A single on-screen extra-key button sized to mirror Termux's
 * `ExtraKeysView` cell — compact, edge-to-edge with siblings, no gaps.
 * Only the pressed / sticky-armed state paints a soft rounded
 * highlight behind the glyph, matching iOS keyboard key-cap behaviour
 * rather than Material's outlined button look.
 *
 * Arrow keys render as Lucide vector icons (see `ui/.../drawable/`);
 * everything else uses a sans-serif label so the row reads like a
 * normal keyboard legend, not a code listing.
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

    val arrowResId = key.arrowDrawableRes()

    Box(
        modifier = modifier
            .size(width = 44.dp, height = 32.dp)
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
                .padding(horizontal = 3.dp, vertical = 3.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(highlight),
        )
        if (arrowResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(arrowResId),
                contentDescription = key.displayLabel(),
                modifier = Modifier.size(18.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(glyphColor),
            )
        } else {
            Text(
                text = key.displayGlyph(),
                color = glyphColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

private fun ExtraKey.arrowDrawableRes(): Int? = when (this) {
    is ExtraKey.Navigation -> when (this) {
        ExtraKey.Navigation.ARROW_UP -> R.drawable.lucide_arrow_big_up
        ExtraKey.Navigation.ARROW_DOWN -> R.drawable.lucide_arrow_big_down
        ExtraKey.Navigation.ARROW_LEFT -> R.drawable.lucide_arrow_big_left
        ExtraKey.Navigation.ARROW_RIGHT -> R.drawable.lucide_arrow_big_right
        else -> null
    }
    else -> null
}

private fun ExtraKey.displayGlyph(): String = when (this) {
    is ExtraKey.Special -> name
    is ExtraKey.Text -> glyph
    is ExtraKey.Navigation -> when (this) {
        ExtraKey.Navigation.ESC -> "ESC"
        ExtraKey.Navigation.TAB -> "TAB"
        ExtraKey.Navigation.ARROW_LEFT -> ""
        ExtraKey.Navigation.ARROW_RIGHT -> ""
        ExtraKey.Navigation.ARROW_UP -> ""
        ExtraKey.Navigation.ARROW_DOWN -> ""
        ExtraKey.Navigation.HOME -> "HOME"
        ExtraKey.Navigation.END -> "END"
        ExtraKey.Navigation.PAGE_UP -> "PgUp"
        ExtraKey.Navigation.PAGE_DOWN -> "PgDn"
    }
}

private fun ExtraKey.displayLabel(): String = when (this) {
    is ExtraKey.Navigation -> when (this) {
        ExtraKey.Navigation.ARROW_UP -> "Up arrow"
        ExtraKey.Navigation.ARROW_DOWN -> "Down arrow"
        ExtraKey.Navigation.ARROW_LEFT -> "Left arrow"
        ExtraKey.Navigation.ARROW_RIGHT -> "Right arrow"
        else -> displayGlyph()
    }
    else -> displayGlyph()
}
