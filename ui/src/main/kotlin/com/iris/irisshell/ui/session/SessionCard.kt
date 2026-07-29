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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
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
 * One card in the [LongPressSessionGrid].
 *
 * iOS-App-Switcher inspired layout:
 *
 *   ┌─────────────────────────────────────┐
 *   │  ● shell                          ⓘ │   ← dot + name + info badge
 *   │  Running · 2m ago                   │   ← subtitle: state + freshness
 *   │                                     │
 *   │  ┌───────────────────────────────┐  │
 *   │  │  $ ls -la                     │  │   ← transcript preview
 *   │  │  total 12                     │  │     (mono, 3 lines)
 *   │  └───────────────────────────────┘  │
 *   └─────────────────────────────────────┘
 *
 * Visual states:
 *   - **Default**: 14dp radius, soft shadow, no border.
 *   - **Active**: gold corner badge + 1.5dp gold ring + slightly larger.
 *   - **Hovered** (drag-mode): 1.04× scale + 2dp gold border + gold glow.
 *   - **Armed** (grid-wide drag mode): slight alpha dip when not hovered.
 */
@Composable
fun SessionCard(
    snapshot: SessionSnapshot,
    isActive: Boolean,
    onActivate: () -> Unit,
    isHovered: Boolean = false,
    isArmed: Boolean = false,
    previewLineCount: Int = 3,
    modifier: Modifier = Modifier,
) {
    val dotColor = stateDotColor(snapshot.state)
    val preview = previewLines(snapshot, previewLineCount)
    val freshness = remember(snapshot.lastUsedAtMs) {
        relativeTime(snapshot.lastUsedAtMs)
    }

    // Press feedback on the card itself — independent from the
    // long-press grid overlay.
    var pressed by remember { mutableStateOf(false) }
    val targetScale = when {
        isHovered -> 1.04f
        pressed   -> 0.96f
        isActive  -> 1.02f
        else      -> 1f
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
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val ev = awaitPointerEvent()
                        pressed = ev.changes.any { it.pressed }
                    }
                }
            }
            .then(
                if (isHovered) {
                    Modifier.shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(14.dp),
                        ambientColor = IrisPrimary.copy(alpha = 0.4f),
                        spotColor = IrisPrimary.copy(alpha = 0.4f),
                    )
                } else {
                    Modifier.shadow(
                        elevation = if (isActive) 10.dp else 6.dp,
                        shape = RoundedCornerShape(14.dp),
                    )
                },
            )
            .clip(RoundedCornerShape(14.dp))
            .background(IrisSurfaceVariant.copy(alpha = 0.85f))
            .then(
                if (isActive || isHovered) {
                    Modifier.border(
                        width = if (isHovered) 2.dp else 1.5.dp,
                        color = IrisPrimary,
                        shape = RoundedCornerShape(14.dp),
                    )
                } else {
                    Modifier
                },
            )
            .clickable(enabled = !isArmed, onClick = onActivate),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            // ---- Top row: dot + name + corner badge ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = snapshot.name,
                    color = IrisText,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (isActive) {
                    ActiveCornerBadge()
                }
            }

            // ---- Subtitle: state label · freshness ----
            Text(
                text = "${stateLabel(snapshot.state)} · $freshness",
                color = IrisTextMuted,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Normal,
                ),
                modifier = Modifier.padding(top = 2.dp, start = 18.dp),
            )

            Spacer(Modifier.height(12.dp))

            // ---- Live preview block ----
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IrisSurface.copy(alpha = 0.5f))
                    .padding(10.dp),
            ) {
                if (preview.isEmpty()) {
                    Text(
                        text = "no output yet",
                        color = IrisTextMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        preview.forEach { line ->
                            Text(
                                text = line,
                                color = IrisText.copy(alpha = 0.85f),
                                fontSize = 11.sp,
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
private fun ActiveCornerBadge() {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(IrisPrimary)
            .border(1.dp, IrisSurface, CircleShape),
    )
}

private fun stateDotColor(state: SessionState): Color = when (state) {
    SessionState.Running -> IrisPrimary
    SessionState.Idle    -> IrisTextMuted
    SessionState.Closed  -> IrisTextMuted.copy(alpha = 0.4f)
}

private fun stateLabel(state: SessionState): String = when (state) {
    SessionState.Running -> "Running"
    SessionState.Idle    -> "Idle"
    SessionState.Closed  -> "Closed"
}

private fun previewLines(snapshot: SessionSnapshot, max: Int): List<String> {
    val live = snapshot.liveSnapshotLines
    return when {
        live.isNotEmpty() -> live.takeLast(max)
        else              -> emptyList()
    }
}

private fun relativeTime(thenMs: Long): String {
    if (thenMs <= 0) return "—"
    val now = System.currentTimeMillis()
    val diff = (now - thenMs).coerceAtLeast(0)
    val s = diff / 1000
    return when {
        s < 60       -> "just now"
        s < 3600     -> "${s / 60}m ago"
        s < 86_400   -> "${s / 3600}h ago"
        s < 604_800  -> "${s / 86_400}d ago"
        else         -> "${s / 604_800}w ago"
    }
}
