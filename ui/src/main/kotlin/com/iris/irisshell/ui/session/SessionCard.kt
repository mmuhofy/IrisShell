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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import com.iris.irisshell.design.system.IrisDropdownMenu
import com.iris.irisshell.design.system.IrisMenuItem
import com.iris.irisshell.design.system.IrisMenuItemStyle
import com.iris.irisshell.ui.R
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
import com.iris.irisshell.design.system.IrisPrimary
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
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness    = Spring.StiffnessMedium,
        ),
        label = "card-scale",
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
                .clip(CardShape)
                .border(
                    width = 0.5.dp,
                    color = if (isActive)
                        IrisPrimary.copy(alpha = 0.25f)
                    else
                        IrisBorderSubtle.copy(alpha = 0.1f),
                    shape = CardShape,
                )
                .clickable(enabled = !isCommitting && swipeOffset.value == 0f) { onActivate() },
        ) {
            // Active left accent bar — solid blue, ultra-thin
            if (isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(2.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                        .background(IrisPrimary),
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
                                .clip(RoundedCornerShape(20.dp))
                                .background(IrisPrimary.copy(alpha = 0.1f))
                                .border(0.5.dp, IrisPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                        ) {
                            Text(
                                text          = "ACTIVE",
                                color         = IrisPrimary,
                                fontSize      = 9.sp,
                                fontWeight    = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp,
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
                            .background(IrisBackground.copy(alpha = 0.3f))
                            .border(0.5.dp, IrisBorderSubtle.copy(alpha = 0.15f), PreviewShape)
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
                IrisMenuItem(
                    label = "Rename",
                    icon  = painterResource(R.drawable.lucide_pencil),
                ),
                IrisMenuItem(
                    label         = "Delete",
                    icon          = painterResource(R.drawable.lucide_trash_2),
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
