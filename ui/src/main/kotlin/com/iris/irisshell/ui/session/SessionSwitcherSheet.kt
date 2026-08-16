package com.iris.irisshell.ui.session

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.domain.session.SessionSnapshot
import com.iris.irisshell.ui.util.BlurDialogWindow
import android.view.Window
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.runtime.rememberSwipeToDismissBoxState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Centre-bottom sheet that lists all sessions in a vertical list with search.
 * Features:
 *  - Search filter (name + last command)
 *  - Active session highlighted with gold accent line + glow
 *  - Swipe-to-delete with undo snackbar
 *  - Inline rename via trailing edit icon
 *  - New session button in header
 */
@Composable
fun SessionSwitcherSheet(
    onDismiss: () -> Unit,
    viewModel: SessionSwitcherViewModel = hiltViewModel(),
) {
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val activeId by viewModel.activeId.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var committingId by remember { mutableStateOf<String?>(null) }
    var renamingId by remember { mutableStateOf<String?>(null) }
    var renameNewName by remember { mutableStateOf("") }
    var pendingDelete by remember { mutableStateOf<DeletedSession?>(null) }
    var searchText by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope = rememberCoroutineScope()

    DisableDialogScrim()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        BlurDialogWindow(
            radiusDp = 22f,
            enabled = !showCreateDialog && renamingId == null
        )
    }

    val inCommit = committingId != null

    // Filter sessions locally for instant feedback
    val filteredSessions = remember(sessions, searchText) {
        if (searchText.isBlank()) sessions
        else sessions.filter { s ->
            s.name.contains(searchText, ignoreCase = true) ||
                s.liveSnapshotLines.any { it.contains(searchText, ignoreCase = true) }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(
                initialScale = 0.88f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            ) + fadeIn(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            ),
            exit = scaleOut(
                targetScale = if (inCommit) 0.94f else 0.92f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessHigh,
                ),
            ) + fadeOut(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessHigh,
                ),
            ),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(560.dp) // slightly taller for list + search
                    .systemBarsPadding()
                    .graphicsLayer { shadowElevation = 24f },
                shape = RoundedCornerShape(12.dp),
                color = IrisSurface.copy(alpha = 0.92f),
                tonalElevation = 6.dp,
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    SwitcherTopBar(
                        onClose = onDismiss,
                        onCreate = { showCreateDialog = true },
                        searchText = searchText,
                        onSearchChange = { searchText = it },
                    )

                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        if (filteredSessions.isEmpty()) {
                            EmptyState(
                                searchText = searchText,
                                onCreate = { showCreateDialog = true },
                            )
                        } else {
                            SessionList(
                                sessions = filteredSessions,
                                activeId = activeId,
                                committingId = committingId,
                                onCommit = { id ->
                                    if (committingId == null) {
                                        committingId = id
                                        viewModel.activate(id)
                                    }
                                },
                                onRename = { snapshot ->
                                    renameNewName = snapshot.name
                                    renamingId = snapshot.id
                                },
                                onDelete = { snapshot ->
                                    // Swipe-to-delete will call this; we keep for compatibility
                                    pendingDelete = DeletedSession(
                                        snapshot = snapshot,
                                        wasActive = snapshot.id == activeId,
                                        fallbackName = null,
                                    )
                                    viewModel.delete(snapshot.id)
                                },
                            )
                        }

                        SnackbarHost(
                            hostState = snackbarHostState,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp),
                        )
                    }
                }
            }
        }
    }

    // Commit animation then dismiss
    LaunchedEffect(committingId) {
        val id = committingId ?: return@LaunchedEffect
        delay(220)
        onDismiss()
        committingId = null
    }

    // Prepare undo snackbar after deletion
    LaunchedEffect(pendingDelete, sessions, activeId) {
        val pd = pendingDelete ?: return@LaunchedEffect
        if (pd.fallbackName == null && !sessions.any { it.id == pd.snapshot.id }) {
            val fallback = sessions.firstOrNull { it.id == activeId }?.name
            pendingDelete = pd.copy(
                wasActive = pd.wasActive || pd.snapshot.id == activeId,
                fallbackName = fallback,
            )
        }
    }

    LaunchedEffect(pendingDelete) {
        val pd = pendingDelete ?: return@LaunchedEffect
        val msg = if (pd.wasActive && pd.fallbackName != null) {
            "Deleted '${pd.snapshot.name}' • Switched to '${pd.fallbackName}'"
        } else {
            "Deleted '${pd.snapshot.name}'"
        }
        snackbarScope.launch {
            val result = try {
                snackbarHostState.showSnackbar(
                    message = msg,
                    actionLabel = "Undo",
                    withDismissAction = true,
                    duration = androidx.compose.material3.SnackbarDuration.Short,
                )
            } catch (e: kotlinx.coroutines.CancellationException) {
                SnackbarResult.Dismissed
            }
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    viewModel.restoreSession(pd.snapshot)
                }
                SnackbarResult.Dismissed -> {
                    pendingDelete = null
                }
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

    renamingId?.let { id ->
        RenameSessionDialog(
            currentName = renameNewName,
            onConfirm = { newName ->
                viewModel.rename(id, newName)
                renamingId = null
            },
            onDismiss = { renamingId = null },
        )
    }
}

/* -------------------------------------------------------------------------- */
/*                                    UI                                      */
/* -------------------------------------------------------------------------- */

@Composable
private fun SwitcherTopBar(
    onClose: () -> Unit,
    onCreate: () -> Unit,
    searchText: String,
    onSearchChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    .padding(start = 8.dp),
            )
            FilledIconButton(
                onClick = onCreate,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = IrisPrimary,
                    contentColor = IrisSurface,
                ),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New session")
            }
        }

        // Search field
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            singleLine = true,
            placeholder = { Text("Search sessions…", color = IrisTextMuted) },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = IrisTextMuted) },
            colors = androidx.compose.material3.TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = IrisPrimary,
                unfocusedBorderColor = IrisOutline,
                backgroundColor = IrisSurfaceVariant.copy(alpha = 0.6f),
            ),
        )
    }
}

@Composable
private fun SessionList(
    sessions: List<SessionSnapshot>,
    activeId: String?,
    committingId: String?,
    onCommit: (String) -> Unit,
    onRename: (SessionSnapshot) -> Unit,
    onDelete: (SessionSnapshot) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 16.dp),
    ) {
        items(sessions) { snapshot ->
            val isActive = snapshot.id == activeId
            val isCommitting = committingId != null && committingId == snapshot.id
            val swipeState = rememberSwipeToDismissBoxState()
            val swipeDirection = if (snapshot.id == activeId) SwipeToDismissBoxDefaults.DismissDirection.EndToStart else SwipeToDismissBoxDefaults.DismissDirection.EndToStart

            SwipeToDismissBox(
                state = swipeState,
                directions = setOf(swipeDirection),
                onDismissed = { onDelete(snapshot) },
                modifier = Modifier.fillMaxWidth(),
                background = { progress ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Red),
                        contentAlignment = Alignment.CenterEnd,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = IrisSurface,
                            modifier = Modifier.padding(end = 20.dp)
                        )
                    }
                },
                dismissThresholds = { fraction -> fraction > 0.5f },
            ) {
                SessionCard(
                    snapshot = snapshot,
                    isActive = isActive,
                    isCommitting = isCommitting,
                    onActivate = { onCommit(snapshot.id) },
                    onRename = { onRename(snapshot) },
                    onDelete = { }, // delete handled by swipe-to-dismiss
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun EmptyState(
    searchText: String,
    onCreate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = if (searchText.isNotBlank()) "No matching session" else "No sessions yet",
            color = IrisText,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = if (searchText.isNotBlank())
                "Try a different search term"
            else
                "Tap + to spawn your first terminal",
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

/* -------------------------------------------------------------------------- */
/*                              Dialogs (unchanged)                           */
/* -------------------------------------------------------------------------- */

@Composable
private fun CreateSessionDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("shell") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New session", color = IrisText) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Name") },
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name.ifBlank { "shell" }) }) {
                Text("Create", color = IrisPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = IrisTextMuted)
            }
        },
        containerColor = IrisSurface,
    )
}

@Composable
private fun RenameSessionDialog(
    currentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(currentName) { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename session", color = IrisText) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Name") },
                isError = name.isBlank(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.trim()) },
                enabled = name.isNotBlank(),
            ) {
                Text("Save", color = IrisPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = IrisTextMuted)
            }
        },
        containerColor = IrisSurface,
    )
}

@Composable
private fun DeleteConfirmDialog(
    snapshot: SessionSnapshot,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete this session?", color = IrisText) },
        text = {
            Text(
                text = "Session '${snapshot.name}' will be removed from the list. " +
                    "Files in the home directory won't be touched.",
                color = IrisTextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = IrisPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = IrisTextMuted)
            }
        },
        containerColor = IrisSurface,
    )
}

/* -------------------------------------------------------------------------- */
/*                                 Helpers                                    */
/* -------------------------------------------------------------------------- */

@Composable
private fun DisableDialogScrim() {
    val view = LocalView.current
    SideEffect {
        val w = findHostWindow(view.context)
        w?.setDimAmount(0f)
    }
}

private fun findHostWindow(ctx: android.content.Context): Window? {
    var c: android.content.Context? = ctx
    while (c is android.content.ContextWrapper) {
        if (c is android.app.Activity) return c.window
        c = c.baseContext
    }
    return null
}

/* -------------------------------------------------------------------------- */
/*                              Data class for undo                           */
/* -------------------------------------------------------------------------- */

private data class DeletedSession(
    val snapshot: SessionSnapshot,
    val wasActive: Boolean,
    val fallbackName: String?,
)