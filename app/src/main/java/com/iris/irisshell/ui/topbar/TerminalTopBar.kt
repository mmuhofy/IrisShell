package com.iris.irisshell.ui.topbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Maximize2
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Settings
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.design.system.IrisDropdownMenu
import com.iris.irisshell.design.system.IrisMenuItem

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
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val activeTabIndex by activeTabIndexFlow.collectAsState()

    TopAppBar(
        modifier = modifier.fillMaxWidth(),
        colors   = TopAppBarDefaults.topAppBarColors(
            containerColor         = IrisSurface,
            scrolledContainerColor = IrisSurface,
            titleContentColor      = IrisText,
            actionIconContentColor = IrisTextSecondary,
        ),
        navigationIcon = {},
        title = {
            Column {
                Text(
                    text  = sessionTitle,
                    color = IrisText,
                    fontSize = 20.sp,
                    style = MaterialTheme.typography.titleLarge,
                )
                if (tabCount > 0) {
                    Text(
                        text  = "Session ${activeTabIndex + 1} of $tabCount",
                        color = IrisTextSecondary,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        actions = {
            MoreActionsMenu(
                isFullscreen      = isFullscreen,
                onToggleFullscreen = onToggleFullscreen,
                onOpenSettings    = onOpenSettings,
                onNewSession      = onRefresh, // caller maps this to new session
            )
        },
    )
}

@Composable
private fun MoreActionsMenu(
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onOpenSettings: () -> Unit,
    onNewSession: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier         = Modifier.size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector        = Icons.Filled.MoreVert,
                contentDescription = "More actions",
                modifier           = Modifier.size(24.dp),
                tint               = IrisTextSecondary,
            )
        }

        IrisDropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false },
            items = listOf(
                IrisMenuItem(
                    label = "New Session",
                    icon  = Lucide.Plus,
                ),
                IrisMenuItem(
                    label = if (isFullscreen) "Exit Fullscreen" else "Enter Fullscreen",
                    icon  = Lucide.Maximize2,
                ),
                IrisMenuItem(
                    label         = "Settings",
                    icon          = Lucide.Settings,
                    dividerBefore = true,
                ),
            ),
            onItemClick = { item ->
                when (item.label) {
                    "New Session"                       -> onNewSession()
                    "Enter Fullscreen", "Exit Fullscreen" -> onToggleFullscreen()
                    "Settings"                          -> onOpenSettings()
                }
            },
        )
    }
}
