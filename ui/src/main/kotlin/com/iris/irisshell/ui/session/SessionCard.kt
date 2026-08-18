package com.iris.irisshell.ui.session

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.Trash2
import com.iris.irisshell.ui.components.IrisDropdownMenu
import com.iris.irisshell.ui.components.IrisMenuItem
import com.iris.irisshell.ui.components.IrisMenuItemStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisError
import com.iris.irisshell.design.system.IrisOnPrimary
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.domain.session.SessionSnapshot
import com.iris.irisshell.domain.session.SessionState
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val CardShape    = RoundedCornerShape(16.dp)
private val PreviewShape = RoundedCornerShape(10.dp)
private const val DELETE_THRESHOLD_DP = 110f

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
    val density = LocalDensity.current
    val deleteThresholdPx = with(density) { DELETE_THRESHOLD_DP.dp.toPx() }
    val swipeOffset = remember(snapshot.id) { Animatable(0f) }

    // Card scale: tap feedback + commit explode
    val targetScale = when {
        isCommitting -> 1.03f
        isActive     -> 1.005f
        else         -> 1f
    }
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow,
        ),
        label = "card-scale",
    )

    // Gold glow opacity when committing
    val glowAlpha by animateFloatAsState(
        targetValue   = if (isCommitting) 1f else 0f,
        animationSpec = tween(120),
        label         = "card-glow",
    )

    val deleteProgress = (swipeOffset.value / deleteThresholdPx).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        // Delete reveal — red tint + label slides in from right
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(CardShape)
                .background(IrisError.copy(alpha = 0.08f + deleteProgress * 0.22f)),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Text(
                text     = "Delete",
                color    = IrisError.copy(alpha = deleteProgress),
                style    = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier
                    .padding(end = 20.dp)
                    .graphicsLayer { translationX = (1f - deleteProgress) * 24f },
            )
        }

        // Card
        val shadowElevation = when {
            isCommitting -> 20.dp
            isActive     -> 10.dp
            else         -> 4.dp
        }
        val shadowColor = if (isActive || isCommitting)
            IrisPrimary.copy(alpha = 0.25f * glowAlpha + if (isActive) 0.12f else 0f)
        else
            Color.Black.copy(alpha = 0.4f)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .offset { IntOffset(-swipeOffset.value.roundToInt(), 0) }
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .pointerInput(snapshot.id) {
                    coroutineScope {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                val next = (swipeOffset.value - dragAmount.x)
                                    .coerceIn(0f, deleteThresholdPx * 1.6f)
                                if (dragAmount.x < 0f || swipeOffset.value > 0f) {
                                    launch { swipeOffset.snapTo(next) }
                                }
                            },
                            onDragEnd = {
                                val shouldDelete = swipeOffset.value >= deleteThresholdPx
                                launch {
                                    swipeOffset.animateTo(
                                        targetValue   = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness    = Spring.StiffnessMedium,
                                        ),
                                    )
                                    if (shouldDelete) onDelete()
                                }
                            },
                            onDragCancel = {
                                launch {
                                    swipeOffset.animateTo(
                                        targetValue   = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness    = Spring.StiffnessMedium,
                                        ),
                                    )
                                }
                            },
                        )
                    }
                }
                .shadow(
                    elevation    = shadowElevation,
                    shape        = CardShape,
                    ambientColor = shadowColor,
                    spotColor    = shadowColor,
                )
                .clip(CardShape)
                .background(
                    if (isActive)
                        Brush.linearGradient(
                            colors = listOf(
                                IrisPrimary.copy(alpha = 0.07f),
                                IrisSurfaceVariant.copy(alpha = 0.55f),
                            )
                        )
                    else
                        Brush.linearGradient(
                            colors = listOf(
                                IrisSurfaceVariant.copy(alpha = 0.55f),
                                IrisSurfaceVariant.copy(alpha = 0.55f),
                            )
                        )
                )
                .border(
                    width = if (isActive || isCommitting) 1.5.dp else 1.dp,
                    color = when {
                        isCommitting -> IrisPrimary
                        isActive     -> IrisPrimary.copy(alpha = 0.55f)
                        else         -> IrisBorderSubtle.copy(alpha = 0.8f)
                    },
                    shape = CardShape,
                )
                .clickable(enabled = !isCommitting && swipeOffset.value == 0f) { onActivate() },
        ) {
            // Active left pulse bar
            if (isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(3.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(IrisPrimary, IrisPrimary.copy(alpha = 0.5f))
                            )
                        ),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start  = if (isActive) 19.dp else 14.dp,
                        end    = 6.dp,
                        top    = 12.dp,
                        bottom = 12.dp,
                    ),
            ) {
                // Top row: name + ACTIVE badge + overflow
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text     = snapshot.name,
                        color    = IrisText,
                        style    = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight    = FontWeight.SemiBold,
                            letterSpacing = (-0.3).sp,
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )

                    if (isActive) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(IrisPrimary)
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text          = "ACTIVE",
                                color         = IrisOnPrimary,
                                fontSize      = 9.sp,
                                fontWeight    = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                            )
                        }
                    }

                    CardOverflowMenu(
                        onRename = onRename,
                        onDelete = onDelete,
                        enabled  = !isCommitting,
                    )
                }

                // Subtitle: state · freshness
                Text(
                    text     = "${stateLabel(snapshot.state)} · ${relativeTime(snapshot.lastUsedAtMs)}",
                    color    = IrisTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.padding(top = 3.dp),
                )

                // Terminal preview — monospace, last non-blank line
                val preview = snapshot.liveSnapshotLines.lastOrNull { it.isNotBlank() }
                if (preview != null) {
                    Spacer(Modifier.height(9.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(PreviewShape)
                            .background(IrisBackground.copy(alpha = 0.6f))
                            .border(1.dp, IrisBorderSubtle.copy(alpha = 0.5f), PreviewShape)
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                    ) {
                        Text(
                            text       = preview,
                            color      = IrisTextMuted,
                            fontSize   = 11.5.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines   = 1,
                            overflow   = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/*  Overflow menu                                                             */
/* -------------------------------------------------------------------------- */

@Composable
private fun CardOverflowMenu(
    onRename: () -> Unit,
    onDelete: () -> Unit,
    enabled: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick  = { expanded = true },
            enabled  = enabled,
            modifier = Modifier.size(36.dp),
        ) {
            Icon(
                imageVector        = Icons.Filled.MoreVert,
                contentDescription = "Session options",
                tint               = IrisTextSecondary,
                modifier           = Modifier.size(18.dp),
            )
        }
        IrisDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false },
            items = listOf(
                IrisMenuItem("Rename", icon = Lucide.Pencil),
                IrisMenuItem(
                    label         = "Delete",
                    icon          = Lucide.Trash2,
                    style         = IrisMenuItemStyle.Destructive,
                    dividerBefore = true,
                ),
            ),
            onItemClick = { item ->
                when (item.label) {
                    "Rename" -> onRename()
                    "Delete" -> onDelete()
                }
            },
        )
    }
}

/* -------------------------------------------------------------------------- */
/*  Helpers                                                                   */
/* -------------------------------------------------------------------------- */

private fun stateLabel(state: SessionState): String = when (state) {
    SessionState.Running -> "Running"
    SessionState.Idle    -> "Idle"
    SessionState.Closed  -> "Closed"
}

private fun relativeTime(thenMs: Long): String {
    if (thenMs <= 0L) return "—"
    val diff = (System.currentTimeMillis() - thenMs).coerceAtLeast(0L)
    val s    = diff / 1000
    return when {
        s < 60      -> "just now"
        s < 3_600   -> "${s / 60}m ago"
        s < 86_400  -> "${s / 3_600}h ago"
        s < 604_800 -> "${s / 86_400}d ago"
        else        -> "${s / 604_800}w ago"
    }
}
