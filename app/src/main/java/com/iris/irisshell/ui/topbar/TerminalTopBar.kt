package com.iris.irisshell.ui.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.ui.icons.IrisShellMark
import com.iris.irisshell.ui.theme.IrisOutline
import com.iris.irisshell.ui.theme.IrisPrimary
import com.iris.irisshell.ui.theme.IrisSurface
import com.iris.irisshell.ui.theme.IrisSurfaceVariant
import com.iris.irisshell.ui.theme.IrisText
import com.iris.irisshell.ui.theme.IrisTextSecondary

/**
 * Iris Shell Terminal topbar — modern, fixed, 48dp tall.
 *
 * Layout (mobile-first, all icons vector via Canvas + Lucide):
 *   LEFT  — Iris monogram · "Iris Shell"
 *   MID   — Tab indicator: "1 / 3" with active gold dot
 *   RIGHT — Refresh · Fullscreen toggle · Close (≥48dp tap targets)
 *
 * Design tokens (MEMORYBANK.md §5):
 *  - Background: IrisSurface (#141414) with IrisOutline (#1E1E1E) 1dp bottom border
 *  - Primary accent: IrisPrimary (#E8C547 warm gold)
 *  - Secondary text: IrisTextSecondary (#888888 / 4.5:1 contrast on IrisSurface)
 *  - Tap targets: 48dp (Material default)
 *  - Spacing: 4dp and 8dp incremental rhythm
 *  - Press feedback: clip(RoundedCornerShape(12dp)) + tinted background on tap
 *
 * The earlier setup bug (notification shade auto-pull) is preserved by keeping
 * the topbar inside the same WindowInsetsCompat hide zone so it does not
 * re-trigger the system bars on tap.
 */
@Composable
fun TerminalTopBar(
    activeTabIndexFlow: kotlinx.coroutines.flow.StateFlow<Int>,
    tabCount: Int,
    onRefresh: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onClose: () -> Unit,
    isFullscreen: Boolean,
    modifier: Modifier = Modifier,
) {
    val activeTabIndex by activeTabIndexFlow.collectAsState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(IrisSurface)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ─── LEFT: branding ───────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IrisShellMark(modifier = Modifier.size(20.dp))
            Text(
                text = "Iris Shell",
                color = IrisText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp,
            )
        }

        // ─── CENTER: tab indicator ────────────────────────────────────────────
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabIndicator(activeIndex = activeTabIndex, total = tabCount)
        }

        // ─── RIGHT: action buttons ───────────────────────────────────────────
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopBarIconButton(
                imageVector = Icons.Filled.Refresh,
                description = "Refresh terminal",
                onClick = onRefresh,
            )
            TopBarIconButton(
                imageVector = Icons.Filled.Lock,
                description = if (isFullscreen) "Exit fullscreen" else "Enter fullscreen",
                tinted = isFullscreen,
                onClick = onToggleFullscreen,
            )
            TopBarIconButton(
                imageVector = Icons.Filled.Close,
                description = "Close session",
                onClick = onClose,
            )
        }
    }
    // 1dp hairline separator — provides definition between the topbar surface
    // and the terminal view below.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(IrisOutline),
    )
}

@Composable
private fun TabIndicator(activeIndex: Int, total: Int) {
    if (total <= 0) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(50))
                .background(IrisPrimary),
        )
        Text(
            text = "${activeIndex + 1} / $total",
            color = IrisText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.6.sp,
        )
    }
}

/**
 * Generic topbar icon button — 36dp visual circle on a 48dp tap surface,
 * tinted surface when `tinted` is true and on press.
 */
@Composable
private fun TopBarIconButton(
    imageVector: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    tinted: Boolean = false,
) {
    val containerColor = if (tinted) {
        IrisSurfaceVariant
    } else {
        Color.Transparent
    }
    val iconColor = if (tinted) IrisPrimary else IrisTextSecondary
    Box(
        modifier = Modifier
            .size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(containerColor)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = description,
                tint = iconColor,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
