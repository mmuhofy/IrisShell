package com.iris.irisshell.ui.terminal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Right-edge vertical zoom indicator. Auto-hides after [autoHideMillis] of
 * inactivity. Tap or drag anywhere on the track to jump the font.
 *
 * Visual:
 *   ┌──────┐
 *   │ max  │   <- "A" tip label
 *   │ ─┬─  │
 *   │  │   │   <- thumb (IrisPrimary)
 *   │  │   │
 *   │  │   │
 *   │ ─┴─  │
 *   │ sp   │
 *   └──────┘
 *
 * Top = MAX_FONT_SP, bottom = MIN_FONT_SP. Mapping is linear / continuous.
 */
@Composable
fun VerticalZoomSlider(
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
            Text(
                text = "A",
                color = IrisPrimary,
                fontSize = 14.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            ZoomTrack(
                value = value,
                onValueChange = onValueChange,
            )
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
    var size by remember { mutableStateOf(IntSize.Zero) }

    val thumbFraction = ((value - range.first).toFloat() / (range.last - range.first))
        .coerceIn(0f, 1f)

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
                    onDragEnd = { /* launchEffect re-arms the auto-hide timer */ },
                ) { change, _ ->
                    change.consume()
                    commitOffset(change.position, size, minV, maxV, onValueChange)
                }
            },
    ) {
        // Filled portion from the thumb upwards.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = ((1f - thumbFraction) * size.height).coerceAtLeast(0f).toInt().dp)
                .background(IrisPrimary.copy(alpha = 0.65f)),
        )
        // Thumb puck.
        Box(
            modifier = Modifier
                .size(width = 28.dp, height = 8.dp)
                .padding(top = ((1f - thumbFraction) * size.height).coerceAtLeast(0f).toInt().dp)
                .clip(RoundedCornerShape(3.dp))
                .background(IrisPrimary)
                .align(Alignment.TopStart),
        )
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
