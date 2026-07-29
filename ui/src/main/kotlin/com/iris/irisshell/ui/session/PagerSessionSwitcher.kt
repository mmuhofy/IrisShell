package com.iris.irisshell.ui.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.domain.session.SessionSnapshot
import kotlinx.coroutines.launch

/**
 * HorizontalPager-based session switcher with a long-press drag overlay.
 *
 * Interaction model (single gesture — matches docs/mockups/session-switcher.html):
 *
 *  - **Press & hold ≥150ms** → armed. Cards shrink, finger card pops with a
 *    gold ring, haptic tick fires.
 *  - **Drag while armed** → pages swipe via the pager's built-in gesture.
 *    Each new page = haptic tick.
 *  - **Release while armed** → activate that page + dismiss.
 *  - **Short tap (<150ms)** on a card → activate that card + dismiss.
 *  - Anything else (swipe before 150ms, drag past armed threshold on empty
 *    area) is ignored.
 *
 * The pager's own drag gesture does the page-swiping animation. The
 * long-press overlay is a transparent pointerInput layer on top that
 * listens for press-down events, flips `armed` after the threshold, and
 * fires `onActivate(currentPage)` on release.
 */
@Composable
fun PagerSessionSwitcher(
    sessions: List<SessionSnapshot>,
    activeId: String?,
    onActivate: (String) -> Unit,
    longPressThresholdMs: Long = 150L,
    modifier: Modifier = Modifier,
) {
    val haptic: HapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    val initialPage = sessions.indexOfFirst { it.id == activeId }
        .coerceAtLeast(0)
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { sessions.size },
    )

    var armed by remember { mutableStateOf(false) }
    var downPage by remember { mutableStateOf(initialPage) }

    Box(modifier = modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 0.dp,
        ) { pageIndex ->
            val snapshot = sessions[pageIndex]
            val hovered = armed && pagerState.currentPage == pageIndex
            SessionCard(
                snapshot = snapshot,
                isActive = snapshot.id == activeId,
                isHovered = hovered,
                isArmed = armed,
                onActivate = { onActivate(snapshot.id) },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Floating tooltip — only visible while armed, top-center.
        if (armed) {
            val idx = pagerState.currentPage.coerceIn(0, sessions.lastIndex)
            Box(
                modifier = Modifier
                    .offset(x = 0.dp, y = 32.dp),
            ) {
                FloatingTooltip(text = sessions[idx].name)
            }
        }
    }

    // Per-page haptic tick while armed — fires when settled page changes.
    LaunchedEffect(armed) {
        if (!armed) return@LaunchedEffect
        snapshotFlow { pagerState.currentPage }
            .collect {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
    }

    // Long-press detector — listens at the pager container level. After the
    // threshold, sets `armed = true`. The pager's own drag handles the
    // page swiping; we just gate which releases count.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(sessions.size, longPressThresholdMs) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitPointerEvent(PointerEventPass.Main)
                        val firstDown = down.changes.firstOrNull { it.pressed }
                            ?: continue
                        val start = System.currentTimeMillis()
                        downPage = pagerState.currentPage

                        var armedNow = false
                        while (true) {
                            val ev = awaitPointerEvent(PointerEventPass.Main)
                            val change = ev.changes.firstOrNull { it.id == firstDown.id }
                            if (change == null || !change.pressed) break
                            val elapsed = System.currentTimeMillis() - start
                            if (!armedNow && elapsed >= longPressThresholdMs) {
                                armedNow = true
                                armed = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                // Snap pager back to the press start so the
                                // user's drag distance reads cleanly.
                                scope.launch {
                                    pagerState.scrollToPage(downPage)
                                }
                            }
                        }
                        // Pointer released.
                        if (armedNow || armed) {
                            val finalPage = pagerState.currentPage
                                .coerceIn(0, sessions.lastIndex)
                            onActivate(sessions[finalPage].id)
                        }
                        armed = false
                    }
                }
            },
    )
}

@Composable
private fun FloatingTooltip(text: String) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(initialScale = 0.7f),
        exit = fadeOut() + scaleOut(targetScale = 0.7f),
    ) {
        Box(
            modifier = Modifier
                .background(IrisPrimary)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = text,
                color = IrisSurface,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
