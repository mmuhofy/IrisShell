package com.iris.irisshell.ui.topbar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iris.irisshell.design.system.IrisDropdownMenu
import com.iris.irisshell.design.system.IrisMenuItem
import com.iris.irisshell.design.system.IrisMenuItemStyle
import com.iris.irisshell.ui.R
import com.iris.irisshell.ui.session.SessionSwitcherViewModel
import com.iris.irisshell.ui.theme.IrisSurface
import com.iris.irisshell.ui.theme.IrisText
import com.iris.irisshell.ui.theme.IrisTextSecondary

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
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick = onToggleKeyboard,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(
                            if (keyboardFocused) R.drawable.lucide_keyboard_off
                            else R.drawable.lucide_keyboard
                        ),
                        contentDescription =
                            if (keyboardFocused) "Hide keyboard" else "Show keyboard",
                        tint = IrisTextSecondary,
                        modifier = Modifier.size(24.dp),
                    )
                }
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
        IconButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "More actions",
                tint = IrisTextSecondary,
                modifier = Modifier.size(24.dp),
            )
        }

        IrisDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            items = listOf(
                IrisMenuItem(
                    label = "Refresh terminal",
                    icon = painterResource(R.drawable.lucide_rotate_cw),
                ),
                IrisMenuItem(
                    label = if (isFullscreen) "Exit fullscreen" else "Enter fullscreen",
                    icon = painterResource(if (isFullscreen) R.drawable.lucide_minimize else R.drawable.lucide_maximize),
                ),
                IrisMenuItem(
                    label = "Settings",
                    icon = painterResource(R.drawable.lucide_settings),
                    dividerBefore = true,
                ),
                IrisMenuItem(
                    label = "Close session",
                    icon = painterResource(R.drawable.lucide_x_circle),
                    style = IrisMenuItemStyle.Destructive,
                ),
            ),
            onItemClick = { item ->
                when (item.label) {
                    "Refresh terminal" -> onRefresh()
                    "Exit fullscreen", "Enter fullscreen" -> onToggleFullscreen()
                    "Settings" -> onOpenSettings()
                    "Close session" -> onClose()
                }
            },
        )
    }
}