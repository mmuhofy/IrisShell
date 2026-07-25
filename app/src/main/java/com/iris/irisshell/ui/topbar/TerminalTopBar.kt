package com.iris.irisshell.ui.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.ui.icons.MoreVertical
import com.iris.irisshell.ui.theme.IrisOutline
import com.iris.irisshell.ui.theme.IrisPrimary
import com.iris.irisshell.ui.theme.IrisSurface
import com.iris.irisshell.ui.theme.IrisSurfaceVariant
import com.iris.irisshell.ui.theme.IrisText
import com.iris.irisshell.ui.theme.IrisTextSecondary

/**
 * Iris Shell Terminal topbar — modern, fixed, 56dp tall.
 *
 * Inspired by: github.com/RohitKushvaha01/ReTerminal — core/components/.../TopBar.kt
 * Adapted for Iris Shell — com.iris.irisshell.
 *
 * Layout (mobile-first):
 *   LEFT  — large "IrisShell" title (session name placeholder)
 *   RIGHT — single "more vertical" affordance; tapping it opens a
 *           [DropdownMenu] of terminal actions (Refresh · Fullscreen · Close).
 *
 * Design tokens (MEMORYBANK.md §5):
 *  - Background: IrisSurface (#141414) with IrisOutline (#1E1E1E) 1dp bottom border
 *  - Primary accent: IrisPrimary (#E8C547 warm gold)
 *  - Secondary text: IrisTextSecondary
 *  - Tap target: 48dp
 *
 * ICON STRATEGY
 * --------------
 * All glyphs are real Lucide vectors translated to Compose [ImageVector];
 * see [com.iris.irisshell.ui.icons.MoreVertical] (placeholders for future
 * Refresh / Fullscreen / Close are routed through the same Lucide file in
 * later PRs).
 */
@Composable
fun TerminalTopBar(
    sessionTitle: String,
    onRefresh: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onClose: () -> Unit,
    isFullscreen: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(IrisSurface)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = sessionTitle,
            color = IrisText,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.2).sp,
            modifier = Modifier.weight(1f),
        )

        MoreActionsMenu(
            isFullscreen = isFullscreen,
            onRefresh = onRefresh,
            onToggleFullscreen = onToggleFullscreen,
            onClose = onClose,
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(IrisOutline),
    )
}

@Composable
private fun MoreActionsMenu(
    isFullscreen: Boolean,
    onRefresh: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onClose: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = true },
            contentAlignment = Alignment.Center,
        ) {
            MoreVertical(
                modifier = Modifier.size(22.dp),
                tint = IrisTextSecondary,
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(IrisSurfaceVariant),
        ) {
            DropdownMenuItem(
                text = { MenuLabel("Refresh terminal") },
                onClick = {
                    expanded = false
                    onRefresh()
                },
            )
            DropdownMenuItem(
                text = {
                    MenuLabel(
                        if (isFullscreen) "Exit fullscreen" else "Enter fullscreen",
                    )
                },
                onClick = {
                    expanded = false
                    onToggleFullscreen()
                },
            )
            HorizontalDivider(color = IrisOutline)
            DropdownMenuItem(
                text = { MenuLabel("Close session", destructive = true) },
                onClick = {
                    expanded = false
                    onClose()
                },
            )
        }
    }
}

@Composable
private fun MenuLabel(text: String, destructive: Boolean = false) {
    Text(
        text = text,
        color = if (destructive) IrisPrimary else IrisText,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
    )
}
