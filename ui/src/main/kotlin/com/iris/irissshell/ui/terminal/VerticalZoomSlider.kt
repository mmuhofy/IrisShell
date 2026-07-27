package com.iris.irissshell.ui.terminal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irissshell.design.system.IrisPrimary
import com.iris.irissshell.design.system.IrisSurface
import com.iris.irissshell.design.system.IrisSurfaceVariant
import com.iris.irissshell.design.system.IrisTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Right-edge vertical zoom indicator. Auto-hides after [autoHideMillis] of
 * inactivity. Tap or drag anywhere on the track to jump the font.
 *
 * Layout (rooted top-down):
 *
 *   ▲
 *   ┌─┐
 *   │ │  <- track + gold-filled above the thumb
 *   │█│  <- thumb (IrisPrimary dot)
 *   ├─┤
 *   │ │
 *   │ │
 *   └─┘
 *   sp
 *
 * Top of track = MAX_FONT_SP, bottom = MIN_FONT_SP (linear, continuous).
 */
@Composable
internal fun VerticalZoomSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    autoHideMillis: Long = 1500L,
) {
    var visible by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(value) {
        visible = true
        scope.launch {
            delay(autoHideMillis)
            visible = false
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .width(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(IrisSurface.copy(alpha = 0.94f))
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "▲", color = IrisPrimary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(8.dp))
            ZoomTrack(value = value, onValueChange = onValueChange)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "sp",
                color = IrisTextSecondary,
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
private fun ZoomTrack(
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    val range = TerminalViewModel.MIN_FONT_SP..TerminalViewModel.MAX_FONT_SP
    val minV = range.first.toFloat()
    val maxV = range.last.toFloat()
    val frac = ((value - range.first).toFloat() / (range.last - range.first))
        .coerceIn(0f, 1f)

    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .width(8.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(IrisSurfaceVariant)
            .onGloballyPositioned { size = it.size }
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    commitOffset(offset, size, minV, maxV, onValueChange)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        commitOffset(offset, size, minV, maxV, onValueChange)
                    },
                    onDragEnd = {},
                    onDragCancel = {},
                ) { change, _ ->
                    change.consume()
                    commitOffset(change.position, size, minV, maxV, onValueChange)
                }
            },
    ) {
        Canvas(modifier = Modifier) {
            val trackWidth = this.size.width
            val trackHeight = this.size.height
            val filledFraction = frac
            drawRect(
                color = IrisPrimary.copy(alpha = 0.65f),
                topLeft = Offset(x = 0f, y = trackHeight * (1f - filledFraction)),
                size = Size(width = trackWidth, height = trackHeight * filledFraction),
            )
            val thumbH = 6.dp.toPx()
            val thumbY = (trackHeight * (1f - filledFraction) - thumbH / 2f)
                .coerceIn(0f, (trackHeight - thumbH).coerceAtLeast(0f))
            drawRect(
                color = IrisPrimary,
                topLeft = Offset(x = 0f, y = thumbY),
                size = Size(width = trackWidth, height = thumbH),
            )
        }
    }
}

private fun commitOffset(
    offset: Offset,
    size: IntSize,
    minV: Float,
    maxV: Float,
    onValueChange: (Int) -> Unit,
) {
    if (size.height <= 0) return
    val fraction = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
    val target = minV + fraction * (maxV - minV)
    onValueChange(target.roundToInt())
}
