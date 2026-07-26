package com.iris.irisshell.ui.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
 * Iris Shell Terminal topbar — shapes and container color mirror ReTerminal's
 * [com.rk.terminal.ui.screens.terminal.TerminalTopBar], but with Iris Shell's
 * bolder left-aligned title and the "more vertical" affordance routing
 * terminal actions through a dropdown menu.
 *
 * Inspired by:
 *  - https://github.com/RohitKushvaha01/ReTerminal/blob/main/core/main/.../TerminalTopBar.kt
 *  - https://github.com/RohitKushvaha01/ReTerminal/blob/main/core/components/.../appbars/TopBar.kt
 *
 * Shape strategy:
 *  - Material 3 [TopAppBar] (same primitive ReTerminal uses) with a filled
 *    container color so the bar blends directly into the page surface — no
 *    bottom divider, no extra padding wrappers. The reference uses
 *    `Color.Transparent`; we use [IrisSurface] so the bar reads as one solid
 *    band against [IrisBackground].
 *  - Title slot: a 2-line column — large session title + small subtitle —
 *    replicating ReTerminal's "ReTerminal" / "$id ($mode)" idiom with
 *    Iris Shell's branding.
 *  - actions slot: a single [IconButton] -> [DropdownMenu] for session
 *    operations. No back arrow on entry so the bar stays single-purpose.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalTopBar(
    sessionTitle: String,
    activeTabIndexFlow: kotlinx.coroutines.flow.StateFlow<Int>,
    tabCount: Int,
    isFullscreen: Boolean,
    onRefresh: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeTabIndex by activeTabIndexFlow.collectAsState()
    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = IrisSurface,
            scrolledContainerColor = IrisSurface,
            titleContentColor = IrisText,
            actionIconContentColor = IrisTextSecondary,
        ),
        navigationIcon = {},
        title = {
            Column {
                Text(
                    text = sessionTitle,
                    color = IrisText,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge,
                )
                if (tabCount > 0) {
                    Text(
                        text = "Session ${activeTabIndex + 1} of $tabCount",
                        color = IrisTextSecondary,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        actions = {
            MoreActionsMenu(
                isFullscreen = isFullscreen,
                onRefresh = onRefresh,
                onToggleFullscreen = onToggleFullscreen,
                onClose = onClose,
            )
        },
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
        IconButton(onClick = { expanded = true }) {
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
    )
}
