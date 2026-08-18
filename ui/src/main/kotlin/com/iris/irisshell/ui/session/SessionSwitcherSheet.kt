package com.iris.irisshell.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.domain.session.SessionSnapshot
import com.iris.irisshell.ui.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SessionSwitcherSheet(
    onDismiss: () -> Unit,
    viewModel: SessionSwitcherViewModel = hiltViewModel(),
) {
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val activeId by viewModel.activeId.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }
    var committingId     by remember { mutableStateOf<String?>(null) }
    var renamingId       by remember { mutableStateOf<String?>(null) }
    var renameNewName    by remember { mutableStateOf("") }
    var pendingDelete    by remember { mutableStateOf<DeletedSession?>(null) }
    var searchText       by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarScope     = rememberCoroutineScope()
    val sheetState        = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val filteredSessions = remember(sessions, searchText) {
        if (searchText.isBlank()) sessions
        else sessions.filter { s ->
            s.name.contains(searchText, ignoreCase = true) ||
                s.liveSnapshotLines.any { it.contains(searchText, ignoreCase = true) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState       = sheetState,
        shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor   = IrisSurface,
        tonalElevation   = 6.dp,
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Main scrollable content — verticalScroll on Column (not LazyColumn)
            // to avoid height=0 measurement crash inside ModalBottomSheet.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
            ) {
                SheetTopBar(
                    onClose   = onDismiss,
                    onCreate  = { showCreateDialog = true },
                    searchText      = searchText,
                    onSearchChange  = { searchText = it },
                )

                if (filteredSessions.isEmpty()) {
                    SheetEmptyState(
                        searchText = searchText,
                        onCreate   = { showCreateDialog = true },
                    )
                } else {
                    Column(
                        modifier  = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        filteredSessions.forEach { snapshot ->
                            SessionCard(
                                snapshot     = snapshot,
                                isActive     = snapshot.id == activeId,
                                isCommitting = committingId == snapshot.id,
                                onActivate   = {
                                    if (committingId == null) {
                                        committingId = snapshot.id
                                        viewModel.activate(snapshot.id)
                                    }
                                },
                                onRename = {
                                    renameNewName = snapshot.name
                                    renamingId    = snapshot.id
                                },
                                onDelete = {
                                    pendingDelete = DeletedSession(
                                        snapshot     = snapshot,
                                        wasActive    = snapshot.id == activeId,
                                        fallbackName = null,
                                    )
                                    viewModel.delete(snapshot.id)
                                },
                            )
                        }
                    }
                }
            }

            // Snackbar anchored to the bottom of the sheet
            SnackbarHost(
                hostState = snackbarHostState,
                modifier  = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp),
            )
        }
    }

    // Dismiss after commit animation
    LaunchedEffect(committingId) {
        val id = committingId ?: return@LaunchedEffect
        delay(220)
        onDismiss()
        committingId = null
    }

    // Resolve fallback name after deletion propagates
    LaunchedEffect(pendingDelete, sessions, activeId) {
        val pd = pendingDelete ?: return@LaunchedEffect
        if (pd.fallbackName == null && !sessions.any { it.id == pd.snapshot.id }) {
            val fallback = sessions.firstOrNull { it.id == activeId }?.name
            pendingDelete = pd.copy(
                wasActive    = pd.wasActive || pd.snapshot.id == activeId,
                fallbackName = fallback,
            )
        }
    }

    // Show undo snackbar
    LaunchedEffect(pendingDelete) {
        val pd = pendingDelete ?: return@LaunchedEffect
        val msg = if (pd.wasActive && pd.fallbackName != null)
            "${pd.snapshot.name} removed · now viewing ${pd.fallbackName}"
        else
            "${pd.snapshot.name} removed"

        snackbarScope.launch {
            val result = try {
                snackbarHostState.showSnackbar(
                    message         = msg,
                    actionLabel     = "Undo",
                    withDismissAction = true,
                    duration        = androidx.compose.material3.SnackbarDuration.Short,
                )
            } catch (e: kotlinx.coroutines.CancellationException) {
                SnackbarResult.Dismissed
            }
            when (result) {
                SnackbarResult.ActionPerformed -> viewModel.restoreSession(pd.snapshot)
                SnackbarResult.Dismissed       -> pendingDelete = null
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
            onConfirm   = { newName ->
                viewModel.rename(id, newName)
                renamingId = null
            },
            onDismiss   = { renamingId = null },
        )
    }
}

/* -------------------------------------------------------------------------- */
/*  Top bar                                                                   */
/* -------------------------------------------------------------------------- */

@Composable
private fun SheetTopBar(
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
                    painter           = painterResource(R.drawable.lucide_x),
                    contentDescription = "Close",
                    tint              = IrisText,
                )
            }
            Text(
                text     = "Sessions",
                color    = IrisText,
                style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f),
            )
            FilledIconButton(
                onClick = onCreate,
                colors  = IconButtonDefaults.filledIconButtonColors(
                    containerColor = IrisPrimary,
                    contentColor   = IrisSurface,
                ),
            ) {
                Icon(
                    painter           = painterResource(R.drawable.lucide_plus),
                    contentDescription = "New session",
                    modifier          = Modifier.size(22.dp),
                )
            }
        }

        OutlinedTextField(
            value         = searchText,
            onValueChange = onSearchChange,
            modifier      = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            singleLine    = true,
            placeholder   = { Text("Search sessions…", color = IrisTextMuted) },
            leadingIcon   = {
                Icon(
                    painter           = painterResource(R.drawable.lucide_search),
                    contentDescription = null,
                    tint              = IrisTextMuted,
                    modifier          = Modifier.size(18.dp),
                )
            },
        )
    }
}

/* -------------------------------------------------------------------------- */
/*  Empty state                                                                */
/* -------------------------------------------------------------------------- */

@Composable
private fun SheetEmptyState(
    searchText: String,
    onCreate: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text  = if (searchText.isNotBlank()) "No matching session" else "No sessions yet",
            color = IrisText,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text     = if (searchText.isNotBlank()) "Try a different search term"
                       else "Tap + to spawn your first terminal",
            color    = IrisTextMuted,
            style    = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 6.dp),
        )
        if (searchText.isBlank()) {
            FilledIconButton(
                onClick = onCreate,
                colors  = IconButtonDefaults.filledIconButtonColors(
                    containerColor = IrisPrimary,
                    contentColor   = IrisSurface,
                ),
                modifier = Modifier.padding(top = 20.dp),
            ) {
                Icon(
                    painter           = painterResource(R.drawable.lucide_plus),
                    contentDescription = "New session",
                    modifier          = Modifier.size(24.dp),
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/*  Dialogs                                                                   */
/* -------------------------------------------------------------------------- */

@Composable
private fun CreateSessionDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf("shell") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title            = { Text("New session", color = IrisText) },
        text             = {
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                singleLine    = true,
                label         = { Text("Name") },
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
        title            = { Text("Rename session", color = IrisText) },
        text             = {
            OutlinedTextField(
                value         = name,
                onValueChange = { name = it },
                singleLine    = true,
                label         = { Text("Name") },
                isError       = name.isBlank(),
            )
        },
        confirmButton = {
            TextButton(
                onClick  = { onConfirm(name.trim()) },
                enabled  = name.isNotBlank(),
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
