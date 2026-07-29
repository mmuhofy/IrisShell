package com.iris.irisshell.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.iris.irisshell.domain.session.SessionSnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * The long-press + drag-to-select grid that powers the session switcher.
 *
 * Two interaction modes coexist, separated by a 150 ms long-press threshold:
 *
 *  - **Tap mode (default)**: a single tap on a card activates it and closes
 *    the dialog — the existing `clickable` flow. No drag, no haptic.
 *  - **Drag mode**: after pressing a card for ≥150 ms the gesture enters
 *    drag mode. All cards shrink to 85% scale, the card under the pointer
 *    pops to 1.04× with a gold ring, and a small tooltip showing the
 *    card name floats above the pointer. Each time the highlighted card
 *    changes, a light haptic tick fires. Releasing the finger activates
 *    the highlighted card; releasing outside any card cancels (closes
 *    the dialog without changing session).
 *
 * Cards still own their own `clickable` for tap mode. The drag overlay
 * is a transparent layer on top — it listens for the down event, waits
 * the threshold, then starts emitting drag updates.
 *
 * @param sessions   the persistent session list.
 * @param activeId   currently active session id (for the "active" ring).
 * @param onActivate invoked with the chosen session id when the user
 *                   confirms a tap or finishes a drag.
 * @param onDismiss  invoked when the gesture is cancelled (e.g. release
 *                   outside any card during drag mode).
 */
@Composable
fun LongPressSessionGrid(
    sessions: List<SessionSnapshot>,
    activeId: String?,
    onActivate: (String) -> Unit,
    onDismiss: () -> Unit,
    longPressThresholdMs: Long = 150L,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    columns: Int = 2,
    cellHeightDp: Int = 220,
) {
    val haptic: HapticFeedback = LocalHapticFeedback.current

    // Drag-mode state.
    var armed by remember { mutableStateOf(false) }
    var pointer by remember { mutableStateOf<Offset?>(null) }
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    val cellHeight = cellHeightDp.dp

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            state = rememberLazyGridState(),
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .graphicsLayer {
                    val s = if (armed) 0.85f else 1f
                    scaleX = s
                    scaleY = s
                    alpha = if (armed) 0.88f else 1f
                },
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            items(sessions, key = { it.id }) { snapshot ->
                val index = sessions.indexOf(snapshot)
                val hovered = armed && hoveredIndex == index
                SessionCard(
                    snapshot = snapshot,
                    isActive = snapshot.id == activeId,
                    isHovered = hovered,
                    isArmed = armed,
                    onActivate = { onActivate(snapshot.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellHeight),
                )
            }
        }

        // ---- Transparent drag overlay ----
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .pointerInput(sessions, columns, longPressThresholdMs) {
                    val gridWidth = size.width.toFloat()
                    val gridHeight = size.height.toFloat()
                    val cellW = gridWidth / columns
                    val cellH = with(this@pointerInput) {
                        // Approximate cell height = (gridHeight - spacing) / row_count
                        // We don't know row count; use cellHeightDp via density.
                        cellHeight.toPx()
                    }
                    val spacingPx = 10.dp.toPx()

                    awaitPointerEventScope {
                        while (true) {
                            // Wait for the first finger down.
                            val down = awaitPointerEvent(PointerEventPass.Main)
                            val firstDown = down.changes.firstOrNull { it.pressed }
                                ?: continue
                            if (firstDown == null) continue

                            // Wait threshold or release.
                            val timer = launch {
                                delay(longPressThresholdMs)
                            }
                            var elapsed = 0L
                            var armedNow = false
                            val startTime = System.currentTimeMillis()

                            while (true) {
                                val ev = awaitPointerEvent(PointerEventPass.Main)
                                val change = ev.changes.firstOrNull { it.id == firstDown.id }
                                if (change == null || !change.pressed) {
                                    timer.cancel()
                                    break
                                }
                                elapsed = System.currentTimeMillis() - startTime

                                if (!armedNow && elapsed >= longPressThresholdMs) {
                                    armedNow = true
                                    armed = true
                                    pointer = change.position
                                    hoveredIndex = indexAt(
                                        cellW, cellH, spacingPx, columns, change.position,
                                    )
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }

                                if (armedNow) {
                                    pointer = change.position
                                    val newIndex = indexAt(
                                        cellW, cellH, spacingPx, columns, change.position,
                                    )
                                    if (newIndex != hoveredIndex) {
                                        hoveredIndex = newIndex
                                        if (newIndex != null) {
                                            haptic.performHapticFeedback(
                                                HapticFeedbackType.TextHandleMove,
                                            )
                                        }
                                    }
                                }
                            }
                            timer.cancel()

                            // Handle release.
                            if (armed || armedNow) {
                                val idx = hoveredIndex
                                if (idx != null && idx in sessions.indices) {
                                    onActivate(sessions[idx].id)
                                } else {
                                    onDismiss()
                                }
                                armed = false
                                hoveredIndex = null
                                pointer = null
                            }
                            // If never armed, the existing `clickable`
                            // on the card underneath consumed the tap.
                        }
                    }
                },
        )

        // ---- Floating tooltip (above the pointer) ----
        val idx = hoveredIndex
        val p = pointer
        if (armed && p != null && idx != null && idx in sessions.indices) {
            FloatingTooltip(
                text = sessions[idx].name,
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = p.x.toInt(),
                            y = (p.y - 48.dp.toPx()).toInt(),
                        )
                    }
                    .align(Alignment.TopStart),
            )
        }
    }
}

/**
 * Convert a pointer position to a grid index using fixed column width
 * and known row height. Returns null if the pointer is outside the grid.
 */
private fun indexAt(
    cellWidth: Float,
    cellHeight: Float,
    spacing: Float,
    columns: Int,
    pos: Offset,
): Int? {
    if (pos.x < 0f || pos.y < 0f) return null
    val col = (pos.x / (cellWidth + spacing)).toInt().coerceAtLeast(0)
    if (col >= columns) return null
    val row = (pos.y / (cellHeight + spacing)).toInt().coerceAtLeast(0)
    return row * columns + col
}
