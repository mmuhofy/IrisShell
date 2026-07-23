package com.iris.irisshell.ui.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Tiny vector icons used by the modern topbar (Phase 2 visual).
 *
 * Replaces `androidx.compose.material.icons.filled.{Close,Lock,Refresh}`
 * which pulls in `compose-material-icons-extended` (≈12MB shipped into the
 * APK for three glyphs we already redraw with Canvas).
 *
 * Each icon accepts a `Color` and a `size`-flavored Modifier - we deliberately
 * do NOT take tint/colour strategically at the Compose modifier layer
 * because that bloats redraw under the LazyColumn; vector primitives are
 * cheap enough to keep this hand-rolled.
 */

@Composable
fun GlyphRefresh(
    modifier: Modifier = Modifier.size(18.dp),
    color: Color = Color(0xFF888888),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.5.dp.toPx()
        // Circular arrow body - we draw a 3/4 arc then a chevron pointing back.
        val path = Path().apply {
            // Arc body from 12 o'clock around to 9 o'clock.
            addArc(
                oval = androidx.compose.ui.geometry.Rect(0f, 0f, w, h),
                startAngleDegrees = 70f,
                sweepAngleDegrees = 280f,
            )
            moveTo(w * 0.20f, h * 0.40f)
            // Tip of the chevron (pointing left-down at 9 o'clock).
            lineTo(w * 0.05f, h * 0.55f)
            lineTo(w * 0.20f, h * 0.70f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = stroke),
        )
    }
}

@Composable
fun GlyphLock(
    modifier: Modifier = Modifier.size(18.dp),
    color: Color = Color(0xFF888888),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.5.dp.toPx()
        // Body
        drawRoundRect(
            color = color,
            topLeft = Offset(w * 0.15f, h * 0.45f),
            size = androidx.compose.ui.geometry.Size(w * 0.70f, h * 0.45f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = stroke),
        )
        // Shackle
        val shackle = Path().apply {
            moveTo(w * 0.27f, h * 0.45f)
            cubicTo(
                w * 0.27f, h * 0.18f,
                w * 0.73f, h * 0.18f,
                w * 0.73f, h * 0.45f,
            )
        }
        drawPath(
            path = shackle,
            color = color,
            style = Stroke(width = stroke),
        )
    }
}

@Composable
fun GlyphClose(
    modifier: Modifier = Modifier.size(18.dp),
    color: Color = Color(0xFF888888),
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = 1.5.dp.toPx()
        // Two diagonal strokes.
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.25f)
            lineTo(w * 0.75f, h * 0.75f)
            moveTo(w * 0.75f, h * 0.25f)
            lineTo(w * 0.25f, h * 0.75f)
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = stroke),
        )
    }
}
