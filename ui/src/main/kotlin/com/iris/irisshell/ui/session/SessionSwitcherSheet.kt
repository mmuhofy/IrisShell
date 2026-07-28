package com.iris.irisshell.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.domain.session.SessionSnapshot
import kotlinx.coroutines.launch

/**
 * Modal full-screen session switcher.
 *
 * Inspired by iOS App Switcher (the multi-app preview you get on swipe-up).
 * The user lands here by tapping the session name in the TopBar. From here:
 *   - **swipe** horizontally → peek at adjacent session cards
 *   - **tap** a card → activate that session, close the sheet
 *   - **[+]** top-right → create a new session (name prompt) and activate it
 *   - **×** top-left → close without switching
 *
 * Empty-state shows a centred `[+] New session` button.
 */
@Composable
fun SessionSwitcherSheet(
    onDismiss: () -> Unit,
    viewModel: SessionSwitcherViewModel = hiltViewModel(),
) {
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val activeId by viewModel.activeId.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IrisSurface)
            .systemBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top action bar — ×, title, +.
            SwitcherTopBar(
                onClose = onDismiss,
                onCreate = { showCreateDialog = true },
                canCreate = true,
            )

            if (sessions.isEmpty()) {
                EmptyState(onCreate = { showCreateDialog = true })
            } else {
                PagerContent(
                    sessions = sessions,
                    activeId = activeId,
                    onActivate = { id ->
                        viewModel.activate(id)
                        onDismiss()
                    },
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateSessionDialog(
            onConfirm = { name ->
                viewModel.createNew(name)
                showCreateDialog = false
            },
            onDismiss = { showCreateDialog = false },
        )
    }
}

@Composable
private fun SwitcherTopBar(
    onClose: () -> Unit,
    onCreate: () -> Unit,
    canCreate: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close switcher",
                tint = IrisText,
            )
        }
        Text(
            text = "Sessions",
            color = IrisText,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
        )
        FilledIconButton(
            onClick = onCreate,
            enabled = canCreate,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = IrisPrimary,
                contentColor = IrisSurface,
            ),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "New session")
        }
    }
}

@Composable
private fun PagerContent(
    sessions: List<SessionSnapshot>,
    activeId: String?,
    onActivate: (String) -> Unit,
) {
    val activeIndex = sessions.indexOfFirst { it.id == activeId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = activeIndex) {
        sessions.size
    }
    val scope = rememberCoroutineScope()

    // Pager positioning with peek.
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 32.dp),
        pageSpacing = 12.dp,
    ) { pageIndex ->
        val snapshot = sessions[pageIndex]
        SessionCard(
            snapshot = snapshot,
            isActive = snapshot.id == activeId,
            onActivate = { onActivate(snapshot.id) },
            modifier = Modifier.fillMaxSize(),
        )
    }

    // Page indicator dots.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        sessions.forEachIndexed { i, _ ->
            val active = pagerState.currentPage == i
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(if (active) 7.dp else 5.dp)
                    .clip(CircleShape)
                    .background(if (active) IrisPrimary else IrisTextMuted.copy(alpha = 0.5f)),
            )
        }
    }

    // Dismiss on scroll-to-edge gesture — debounced via LaunchedEffect so
    // we only react to fully-anchored state changes.
    LaunchedEffect(pagerState.currentPage, sessions.size) {
        // No-op: kept here so a future "swipe past last → dismiss" can
        // hook into this without re-plumbing the pager state.
        @Suppress("UNUSED_EXPRESSION") scope
    }
}

@Composable
private fun EmptyState(onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "No sessions yet",
            color = IrisText,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = "Tap + to spawn your first terminal",
            color = IrisTextMuted,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp),
        )
        FilledIconButton(
            onClick = onCreate,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = IrisPrimary,
                contentColor = IrisSurface,
            ),
            modifier = Modifier.padding(top = 20.dp),
        ) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "New session",
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun CreateSessionDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("shell") }
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New session",
                color = IrisText,
            )
        },
        text = {
            androidx.compose.material3.OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Name") },
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    onConfirm(name.ifBlank { "shell" })
                },
            ) {
                Text("Create", color = IrisPrimary)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel", color = IrisTextMuted)
            }
        },
        containerColor = IrisSurface,
    )
}
