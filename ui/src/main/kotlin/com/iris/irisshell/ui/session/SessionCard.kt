package com.iris.irisshell.ui.session

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.domain.session.SessionSnapshot
import com.iris.irisshell.domain.session.SessionState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val CardShape = RoundedCornerShape(16.dp)
private val PreviewShape = RoundedCornerShape(10.dp)
private const val DELETE_THRESHOLD_DP = 120f

@Composable
fun SessionCard(
    snapshot: SessionSnapshot,
    isActive: Boolean,
    isCommitting: Boolean,
    onActivate: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val swipeOffset = remember(snapshot.id) { Animatable(0f) }
    val deleteThresholdPx = with(androidx.compose.ui.platform.LocalDensity.current) {
        DELETE_THRESHOLD_DP.dp.toPx()
    }

    // Scale feedback
    val targetScale = when {
        isCommitting -> 1.04f
        isActive     -> 1.01f
        else         -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "card-scale",
    )

    // Delete reveal alpha — appears as card slides left
    val deleteAlpha = (swipeOffset.value / deleteThresholdPx).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        // Delete background revealed on swipe
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CardShape)
                .background(
                    androidx.compose.ui.graphics.Color(0xFFEF4444).copy(alpha = 0.12f + deleteAlpha * 0.18f)
                ),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Text(
                text = "Delete",
                color = androidx.compose.ui.graphics.Color(0xFFEF4444).copy(alpha = deleteAlpha),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(end = 20.dp),
            )
        }

        // Card surface
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .offset { IntOffset(-swipeOffset.value.roundToInt(), 0) }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .pointerInput(snapshot.id) {
                    coroutineScope {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                // dragAmount.x < 0 = sola sürükleme (delete yönü)
                                val next = (swipeOffset.value - dragAmount.x)
                                    .coerceIn(0f, deleteThresholdPx * 1.5f)
                                if (dragAmount.x < 0f || swipeOffset.value > 0f) {
                                    launch { swipeOffset.snapTo(next) }
                                }
                            },
                            onDragEnd = {
                                val shouldDelete = swipeOffset.value >= deleteThresholdPx
                                launch {
                                    swipeOffset.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium,
                                        ),
                                    )
                                    if (shouldDelete) onDelete()
                                }
                            },
                            onDragCancel = {
                                launch {
                                    swipeOffset.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMedium,
                                        ),
                                    )
                                }
                            },
                        )
                    }
                }
                .clip(CardShape)
                .background(
                    if (isActive)
                        IrisPrimary.copy(alpha = 0.06f)
                    else
                        IrisSurfaceVariant.copy(alpha = 0.55f)
                )
                .then(
                    if (isActive || isCommitting) {
                        Modifier.border(
                            width = if (isCommitting) 2.dp else 1.5.dp,
                            color = IrisPrimary.copy(alpha = if (isCommitting) 1f else 0.85f),
                            shape = CardShape,
                        )
                    } else {
                        Modifier.border(
                            width = 1.dp,
                            color = IrisText.copy(alpha = 0.05f),
                            shape = CardShape,
                        )
                    }
                )
                .clickable(enabled = !isCommitting && swipeOffset.value == 0f) { onActivate() },
        ) {
            // Active left accent bar
            if (isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(3.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                        .background(IrisPrimary),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (isActive) 19.dp else 16.dp,
                        end = 8.dp,
                        top = 12.dp,
                        bottom = 12.dp,
                    ),
            ) {
                // Top row: name + ACTIVE badge + overflow menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = snapshot.name,
                        color = IrisText,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    if (isActive) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(IrisPrimary)
                                .padding(horizontal = 5.dp, vertical = 1.dp),
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = IrisSurface,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.6.sp,
                            )
                        }
                    }

                    CardOverflowMenu(
                        onRename = onRename,
                        onDelete = onDelete,
                        enabled = !isCommitting,
                    )
                }

                // Subtitle: state + freshness
                Text(
                    text = "${stateLabel(snapshot.state)} · ${relativeTime(snapshot.lastUsedAtMs)}",
                    color = IrisTextMuted,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Normal,
                    ),
                    modifier = Modifier.padding(top = 2.dp),
                )

                // Terminal preview
                val preview = snapshot.liveSnapshotLines
                    .filter { it.isNotBlank() }
                    .takeLast(1)
                    .firstOrNull()

                if (preview != null) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(PreviewShape)
                            .background(IrisSurface.copy(alpha = 0.38f))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = preview,
                            color = IrisTextMuted,
                            fontSize = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardOverflowMenu(
    onRename: () -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = { expanded = true },
            enabled = enabled,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "Session options",
                tint = IrisTextMuted,
                modifier = Modifier.size(18.dp),
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(IrisSurfaceVariant),
        ) {
            DropdownMenuItem(
                text = { Text("Rename", color = IrisText, fontSize = 14.sp) },
                onClick = {
                    expanded = false
                    onRename()
                },
            )
            DropdownMenuItem(
                text = {
                    Text(
                        "Delete",
                        color = androidx.compose.ui.graphics.Color(0xFFEF4444),
                        fontSize = 14.sp,
                    )
                },
                onClick = {
                    expanded = false
                    onDelete()
                },
            )
        }
    }
}

private fun stateLabel(state: SessionState): String = when (state) {
    SessionState.Running -> "Running"
    SessionState.Idle    -> "Idle"
    SessionState.Closed  -> "Closed"
}

private fun relativeTime(thenMs: Long): String {
    if (thenMs <= 0L) return "—"
    val diff = (System.currentTimeMillis() - thenMs).coerceAtLeast(0L)
    val s = diff / 1000
    return when {
        s < 60      -> "just now"
        s < 3_600   -> "${s / 60}m ago"
        s < 86_400  -> "${s / 3_600}h ago"
        s < 604_800 -> "${s / 86_400}d ago"
        else        -> "${s / 604_800}w ago"
    }
}
