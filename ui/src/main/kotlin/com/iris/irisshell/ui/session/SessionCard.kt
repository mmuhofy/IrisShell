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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource
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
import com.iris.irisshell.ui.R

private val SwipeDeleteSurface = Color(0xFF8F4650)
private val CardShape = RoundedCornerShape(14.dp)

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
    val swipeOffset = remember(snapshot.id) { Animatable(0f) }
    var pressed by remember(snapshot.id) { mutableStateOf(false) }

    val deleteThreshold = 120f
    val swipeProgress = (swipeOffset.value / deleteThreshold).coerceIn(0f, 1f)
    val deleteAlpha = ((swipeProgress - 0.18f) / 0.82f).coerceIn(0f, 1f)
    val deleteIconScale = 0.82f + (0.18f * deleteAlpha)

    val cardScaleTarget = when {
        isCommitting -> 1.025f
        pressed -> 0.985f
        else -> 1f
    }
    val cardScale by animateFloatAsState(
        targetValue = cardScaleTarget,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "session-card-scale",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                translationX = -swipeOffset.value
                scaleX = cardScale
                scaleY = cardScale
            }
            .pointerInput(snapshot.id) {
                coroutineScope {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val next = (swipeOffset.value + dragAmount.x)
                                .coerceIn(0f, deleteThreshold * 1.5f)
                            if (dragAmount.x > 0f || swipeOffset.value > 0f) {
                                launch { swipeOffset.snapTo(next) }
                            }
                        },
                        onDragEnd = {
                            val shouldDelete = swipeOffset.value >= deleteThreshold
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
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CardShape)
                .background(SwipeDeleteSurface.copy(alpha = deleteAlpha * 0.92f)),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Icon(
                painter = painterResource(R.drawable.lucide_trash_2),
                contentDescription = "Delete",
                tint = IrisSurface,
                modifier = Modifier
                    .padding(end = 18.dp)
                    .graphicsLayer {
                        alpha = deleteAlpha
                        scaleX = deleteIconScale
                        scaleY = deleteIconScale
                    }
                    .size(21.dp),
            )
        }

        val background = if (isActive) {
            IrisPrimary.copy(alpha = 0.075f)
        } else {
            IrisSurfaceVariant.copy(alpha = 0.78f)
        }

        val borderColor = if (isActive) {
            IrisPrimary.copy(alpha = 0.55f)
        } else {
            IrisTextMuted.copy(alpha = 0.08f)
        }

        val elevation = if (isActive) 7.dp else 3.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = (1f - swipeProgress * 0.22f).coerceIn(0.78f, 1f)
                }
                .shadow(
                    elevation = elevation,
                    shape = CardShape,
                    ambientColor = if (isActive) IrisPrimary.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.10f),
                    spotColor = if (isActive) IrisPrimary.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.08f),
                )
                .clip(CardShape)
                .background(background)
                .border(1.dp, borderColor, CardShape)
                .clickable(enabled = !isCommitting && swipeOffset.value == 0f) {
                    onActivate()
                }
                .pointerInput(snapshot.id + ":press") {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            pressed = event.changes.any { it.pressed }
                        }
                    }
                },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.lucide_square_terminal),
                        contentDescription = null,
                        tint = if (isActive) IrisPrimary else IrisTextMuted,
                        modifier = Modifier
                            .padding(end = 10.dp)
                            .size(19.dp),
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = snapshot.name,
                            color = IrisText,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = relativeTime(snapshot.lastUsedAtMs),
                            color = IrisTextMuted,
                            style = MaterialTheme.typography.labelMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    var showActions by remember(snapshot.id) { mutableStateOf(false) }

                    Box {
                        IconButton(
                            onClick = { showActions = true },
                            enabled = !isCommitting,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.lucide_ellipsis_vertical),
                                contentDescription = "Session actions",
                                tint = if (isActive) IrisPrimary else IrisTextMuted,
                                modifier = Modifier.size(20.dp),
                            )
                        }

                        DropdownMenu(
                            expanded = showActions,
                            onDismissRequest = { showActions = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Rename") },
                                onClick = {
                                    showActions = false
                                    onRename()
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Delete") },
                                onClick = {
                                    showActions = false
                                    onDelete()
                                },
                            )
                        }
                    }
                }

                val preview = snapshot.liveSnapshotLines.lastOrNull { it.isNotBlank() }
                if (!preview.isNullOrBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = preview,
                        color = IrisText.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

private fun relativeTime(thenMs: Long): String {
    if (thenMs <= 0L) return "—"
    val diffSeconds = ((System.currentTimeMillis() - thenMs).coerceAtLeast(0L)) / 1000
    return when {
        diffSeconds < 60 -> "just now"
        diffSeconds < 3_600 -> "${diffSeconds / 60}m ago"
        diffSeconds < 86_400 -> "${diffSeconds / 3_600}h ago"
        diffSeconds < 604_800 -> "${diffSeconds / 86_400}d ago"
        else -> "${diffSeconds / 604_800}w ago"
    }
}
