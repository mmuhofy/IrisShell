package com.iris.irisshell.ui.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iris.irisshell.ui.session.SessionSwitcherViewModel
import com.iris.irisshell.ui.theme.IrisOutline
import com.iris.irisshell.ui.theme.IrisPrimary
import com.iris.irisshell.ui.theme.IrisSurface
import com.iris.irisshell.ui.theme.IrisSurfaceVariant
import com.iris.irisshell.ui.theme.IrisText
import com.iris.irisshell.ui.theme.IrisTextSecondary

/**
 * Phase 2 — TopBar with a session-switcher chip in the title slot.
 *
 * The title is now a clickable chip displaying the active session's
 * display name. Tapping it opens the [com.iris.irisshell.ui.session.SessionSwitcherSheet]
 * (full-screen modal). The dropdown menu on the right still owns the
 * terminal-level actions (refresh, fullscreen, close).
 *
 * Subtitle line shows "Session N of M" so the user has positional
 * context alongside the named chip.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionSwitcherTopBar(
    viewModel: SessionSwitcherViewModel,
    isFullscreen: Boolean,
    keyboardFocused: Boolean = true,
    onToggleKeyboard: () -> Unit = {},
    onRefresh: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onClose: () -> Unit,
    onOpenSwitcher: () -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val activeName by viewModel.activeName.collectAsStateWithLifecycle()
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val totalCount = sessions.size
    val subtitle = if (totalCount > 0) {
        "$totalCount session${if (totalCount == 1) "" else "s"} • ${activeName ?: "—"}"
    } else {
        null
    }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onOpenSwitcher)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activeName ?: "IrisShell",
                            color = IrisText,
                            fontSize = 20.sp,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Icon(
                            imageVector = Icons.Filled.ExpandMore,
                            contentDescription = "Open session switcher",
                            tint = IrisTextSecondary,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(18.dp),
                        )
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = IrisTextSecondary,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        },
        actions = {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = rememberRipple(),
                        onClick = onToggleKeyboard
                    )
            ) {
                Icon(
                    painter = androidx.compose.ui.res.painterResource(
                        if (keyboardFocused) com.iris.irisshell.ui.R.drawable.lucide_keyboard_off
                        else com.iris.irisshell.ui.R.drawable.lucide_keyboard
                    ),
                    contentDescription =
                        if (keyboardFocused) "Hide keyboard" else "Show keyboard",
                    tint = IrisTextSecondary,
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.Center),
                )
            }
            MoreActionsMenu(
                isFullscreen = isFullscreen,
                onRefresh = onRefresh,
                onToggleFullscreen = onToggleFullscreen,
                onClose = onClose,
                onOpenSettings = onOpenSettings,
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
    onOpenSettings: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More actions",
                modifier = Modifier.size(24.dp),
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
                text = { MenuLabel("Settings") },
                onClick = {
                    expanded = false
                    onOpenSettings()
                },
            )
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
