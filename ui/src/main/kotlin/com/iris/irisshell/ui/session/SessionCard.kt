package com.iris.irisshell.ui.session

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.domain.session.SessionSnapshot
import com.iris.irisshell.domain.session.SessionState

/**
 * One card in the [SessionSwitcherSheet]'s HorizontalPager.
 *
 * Layout (B-kart style — prompt+output single kart analog):
 *   - 6dp radius
 *   - Surface bg
 *   - 3dp left stroke in [stateColor] (IrisSuccess / IrisPrimary / muted)
 *   - Top: name (large, bold) + state chip
 *   - Bottom: last [previewLineCount] lines of transcript in a sub-surface
 *     with reduced opacity, monospace, truncated to [previewLineCount]
 *
 * Tap → [onActivate].
 */
@Composable
fun SessionCard(
    snapshot: SessionSnapshot,
    isActive: Boolean,
    onActivate: () -> Unit,
    previewLineCount: Int = 6,
    modifier: Modifier = Modifier,
) {
    val strokeColor = stateStroke(snapshot.state)
    val preview = previewLines(snapshot, previewLineCount)

    // Press feedback — separate from clickable so the pager's drag
    // gesture still gets the raw touch stream.
    var pressed by remember { mutableStateOf(false) }
    val targetScale = when {
        pressed  -> 0.96f
        isActive -> 1.02f
        else     -> 1.00f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "session-card-scale",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent()
                        pressed = down.changes.any { it.pressed }
                    }
                }
            }
            .clip(RoundedCornerShape(6.dp))
            .background(IrisSurface)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = strokeColor,
                shape = RoundedCornerShape(6.dp),
            )
            .clickable(onClick = onActivate),
    ) {
        // Left stroke — a separate, non-rounded strip inside the
        // rounded card. Using `padding(start = 4dp)` plus a 4dp-wide
        // box that hugs the left edge.
        Box(
            modifier = Modifier
                .padding(start = 0.dp)
                .width(4.dp)
                .fillMaxSize()
                .background(strokeColor),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
        ) {
            // Top row: name + state chip.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = snapshot.name,
                        color = IrisText,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = stateLabel(snapshot.state),
                        color = stateLabelColor(snapshot.state),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                StateChip(snapshot.state)
            }

            Spacer(Modifier.height(12.dp))

            // Live preview block — sub-surface, monospace.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(IrisSurfaceVariant.copy(alpha = 0.6f))
                    .padding(8.dp),
            ) {
                if (preview.isEmpty()) {
                    Text(
                        text = "no output yet",
                        color = IrisTextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Column {
                        preview.forEach { line ->
                            Text(
                                text = line,
                                color = IrisText,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StateChip(state: SessionState) {
    val (label, color) = when (state) {
        SessionState.Running -> "RUNNING" to IrisPrimary
        SessionState.Idle    -> "IDLE"    to IrisTextMuted
        SessionState.Closed  -> "CLOSED"  to IrisTextMuted
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(3.dp))
            .border(1.dp, color, RoundedCornerShape(3.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

private fun stateStroke(state: SessionState): Color = when (state) {
    SessionState.Running -> IrisPrimary
    SessionState.Idle    -> IrisSurfaceVariant
    SessionState.Closed  -> IrisTextMuted
}

private fun stateLabel(state: SessionState): String = when (state) {
    SessionState.Running -> "pty process alive"
    SessionState.Idle    -> "not yet started"
    SessionState.Closed  -> "process exited"
}

private fun stateLabelColor(state: SessionState): Color = when (state) {
    SessionState.Running -> IrisPrimary
    SessionState.Idle    -> IrisTextMuted
    SessionState.Closed  -> IrisTextMuted
}

private fun previewLines(snapshot: SessionSnapshot, max: Int): List<String> {
    val live = snapshot.liveSnapshotLines
    return when {
        live.isNotEmpty() -> live.takeLast(max)
        else              -> emptyList()
    }
}
