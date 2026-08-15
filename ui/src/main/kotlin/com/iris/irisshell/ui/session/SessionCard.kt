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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontFamily
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
 * One card in the session switcher list.
 *
 * Layout:
 *   ┌─────────────────────────────────────────────────────┐
 *   │ ● shell-1          Running · 2h 14m        [Active] │
 *   │ ~/projects/iris $ git status                       │
 *   │                              ✏️  🗑️               │
 *   └─────────────────────────────────────────────────────┘
 *
 * Active card: gold left bar + gold border + subtle glow.
 * Inline rename/delete icons on the right.
 */
@Composable
fun SessionCard(
    snapshot: SessionSnapshot,
    isActive: Boolean,
    onActivate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    isCommitting: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val dotColor = stateDotColor(snapshot.state)
    val runtime = sessionRuntime(snapshot)
    val preview = snapshot.liveSnapshotLines.lastOrNull { it.isNotBlank() } ?: ""
    val freshness = remember(snapshot.lastUsedAtMs) {
        relativeTime(snapshot.lastUsedAtMs)
    }

    // Press feedback on the card itself.
    var pressed by remember { mutableStateOf(false) }
    val targetScale = when {
        isCommitting -> 1.06f
        pressed      -> 0.96f
        isActive     -> 1.02f
        else         -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "session-card-scale",
    )

    // Glow alpha ramp — jumps to 1 when committing, fades back if reset.
    val glowAlpha by animateFloatAsState(
        targetValue = if (isCommitting) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "session-card-glow",
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
                if (isCommitting || isActive) {
                    val elev = if (isCommitting) 24.dp else 10.dp
                    val ambient = if (isCommitting) IrisPrimary.copy(alpha = 0.5f * glowAlpha + 0.1f)
                                  else IrisPrimary.copy(alpha = 0f)
                    Modifier.shadow(
                        elevation = elev,
                        shape = RoundedCornerShape(14.dp),
                        ambientColor = ambient,
                        spotColor = ambient,
                    )
                } else {
                    Modifier.shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(14.dp),
                    )
                },
            )
            .clip(RoundedCornerShape(14.dp))
            .background(IrisSurfaceVariant.copy(alpha = 0.85f))
            .then(
                if (isCommitting || isActive) {
                    Modifier.border(
                        width = if (isCommitting) 2.5.dp else 1.5.dp,
                        color = IrisPrimary.copy(alpha = if (isCommitting) glowAlpha.coerceAtLeast(0.6f) else 1f),
                        shape = RoundedCornerShape(14.dp),
                    )
                } else {
                    Modifier
                },
            )
            .clickable(enabled = !isCommitting, onClick = onActivate)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // ---- Top row: dot + name + state+runtime + active badge + actions ----
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
                    // Subtitle: state + runtime
                    Text(
                        text = "${stateLabel(snapshot.state)} · ${sessionRuntime(snapshot)}",
                        color = IrisTextMuted,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Normal,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (isActive) {
                    ActiveBadge()
                }
                // Inline actions
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onRename) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Rename",
                            tint = IrisTextMuted,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = IrisTextMuted,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            // ---- Last command preview ----
            if (snapshot.liveSnapshotLines.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                val lastLine = snapshot.liveSnapshotLines.lastOrNull { it.isNotBlank() } ?: ""
                Text(
                    text = lastLine,
                    color = IrisText.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun ActiveBadge() {
    Box(
        modifier = Modifier
            .size(6.dp, 20.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(IrisPrimary)
            .padding(start = 4.dp),
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

private fun sessionRuntime(snapshot: SessionSnapshot): String {
    val now = System.currentTimeMillis()
    val diff = when (snapshot.state) {
        SessionState.Running -> now - snapshot.createdAtMs
        else -> (snapshot.lastUsedAtMs - snapshot.createdAtMs).coerceAtLeast(0)
    }
    val s = (diff / 1000).coerceAtLeast(0)
    return when {
        s < 60       -> "just now"
        s < 3600     -> "${s / 60}m"
        s < 86_400   -> "${s / 3600}h ${(s % 3600) / 60}m"
        s < 604_800  -> "${s / 86_400}d ${(s % 86_400) / 3600}h"
        else         -> "${s / 604_800}w"
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