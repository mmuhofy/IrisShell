package com.iris.irisshell.ui.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.ui.icons.GlyphClose
import com.iris.irisshell.ui.icons.GlyphLock
import com.iris.irisshell.ui.icons.GlyphRefresh
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
 * Layout (mobile-first, all icons are Canvas-native vectors):
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
 * Earlier this topbar used `androidx.compose.material.icons.filled.{Close,Lock,Refresh}`,
 * which forces the whole `compose-material-icons-extended` icon table
 * (~12MB) into the APK for three glyphs. We replaced each with a
 * hand-built Canvas primitive (TopBarIcons.kt).
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

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabIndicator(activeIndex = activeTabIndex, total = tabCount)
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TopBarIconButton(
                contentDescription = "Refresh terminal",
                onClick = onRefresh,
            ) { tint ->
                GlyphRefresh(color = tint)
            }
            TopBarIconButton(
                contentDescription = if (isFullscreen) "Exit fullscreen" else "Enter fullscreen",
                tinted = isFullscreen,
                onClick = onToggleFullscreen,
            ) { tint ->
                GlyphLock(color = tint)
            }
            TopBarIconButton(
                contentDescription = "Close session",
                onClick = onClose,
            ) { tint ->
                GlyphClose(color = tint)
            }
        }
    }
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
 * Generic topbar icon button — 48dp tap surface wrapping a 36dp visual
 * target. Draws a 12dp-clipped tinted ellipse when [tinted] is true so the
 * active fullscreen toggle reads at a glance.
 *
 * The icon slot is a normal @Composable lambda so callers can supply a
 * Canvas-drawn element without dragging in a heavyweight ImageVector API.
 */
@Composable
private fun TopBarIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    tinted: Boolean = false,
    icon: @Composable (Color) -> Unit,
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
            icon(iconColor)
        }
    }
}
