package com.iris.irisshell.ui.setup.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.domain.terminal.StepState
import com.iris.irisshell.ui.setup.theme.SetupPalette

/**
 * Visual marker for a bootstrap step's current state.
 *
 * - Pending:  hollow circle in [SetupPalette.TextDisabled], dashed border
 * - Active:   gold-filled circle with a Gaussian halo that pulses (300ms)
 * - Done:     gold check mark on gold-tinted surface
 * - Failed:   red X with a crimson halo
 *
 * Drawn entirely with Canvas primitives — keeps APK small (no icons-extended dep).
 */
@Composable
fun StepStateIcon(
    state: StepState,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    val infinite = rememberInfiniteTransition(label = "step-icon")
    val pulse by infinite.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-alpha",
    )

    Box(modifier = modifier.size(size + 8.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size + 8.dp)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val ringRadius = (size.toPx() / 2f) + 4f

            when (state) {
                StepState.Pending -> {
                    drawCircle(
                        color = SetupPalette.TextDisabled,
                        radius = size.toPx() / 2.4f,
                        center = center,
                        style = Stroke(
                            width = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f)),
                        ),
                        alpha = 0.6f,
                    )
                }
                StepState.Active -> {
                    drawCircle(
                        color = SetupPalette.PulseHalo,
                        radius = ringRadius * pulse,
                        center = center,
                        alpha = 0.25f * pulse,
                    )
                    drawCircle(
                        color = SetupPalette.Primary,
                        radius = size.toPx() / 2.2f,
                        center = center,
                    )
                }
                StepState.Done -> {
                    drawCircle(
                        color = SetupPalette.Primary.copy(alpha = 0.18f),
                        radius = size.toPx() / 1.9f,
                        center = center,
                    )
                    val cx = center.x
                    val cy = center.y
                    val r = size.toPx() / 4.4f
                    val path = Path().apply {
                        moveTo(cx - r, cy)
                        lineTo(cx - r / 2.5f, cy + r / 1.8f)
                        lineTo(cx + r, cy - r / 1.8f)
                    }
                    drawPath(
                        path = path,
                        color = SetupPalette.Primary,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                        ),
                    )
                }
                StepState.Failed -> {
                    drawCircle(
                        color = SetupPalette.Error.copy(alpha = 0.18f),
                        radius = size.toPx() / 1.9f,
                        center = center,
                    )
                    val cx = center.x
                    val cy = center.y
                    val r = size.toPx() / 3f
                    val path = Path().apply {
                        moveTo(cx - r, cy - r)
                        lineTo(cx + r, cy + r)
                        moveTo(cx + r, cy - r)
                        lineTo(cx - r, cy + r)
                    }
                    drawPath(
                        path = path,
                        color = SetupPalette.Error,
                        style = Stroke(
                            width = 2.dp.toPx(),
                            cap = StrokeCap.Round,
                        ),
                    )
                }
            }
        }
        // Glyph overlays for Done/Failed — minimal redundancy touch for legibility.
        if (state == StepState.Done) {
            Text(
                text = "✓",
                color = SetupPalette.Primary,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.55f).sp,
                ),
            )
        } else if (state == StepState.Failed) {
            Text(
                text = "!",
                color = Color.White,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.55f).sp,
                ),
            )
        }
    }
}

/** Convenience for callers that have positions + width: centers text within bounds. */
@Suppress("unused")
internal val GlyphSizeForStepIcon: Size
    get() = Size.Zero
