package com.iris.irisshell.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.domain.session.SessionSnapshot
import com.iris.irisshell.ui.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/* -------------------------------------------------------------------------- */
/*                              Data class for undo                           */
/* -------------------------------------------------------------------------- */

private data class DeletedSession(
    val snapshot: SessionSnapshot,
    val wasActive: Boolean,
    val fallbackName: String?,
)

/* -------------------------------------------------------------------------- */
/*                              Composable Helpers                            */
/* -------------------------------------------------------------------------- */

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
        Spacer(Modifier.size(44.dp))
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
                painter = painterResource(R.drawable.lucide_plus),
                contentDescription = "New session",
                modifier = Modifier.size(24.dp),
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
/*                              Main Sheet                                    */
/* -------------------------------------------------------------------------- */

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SessionSwitcherSheet(
    onDismiss: () -> Unit,
    viewModel: SessionSwitcherViewModel = hiltViewModel(),
) {
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val activeId by viewModel.activeId.collectAsStateWithLifecycle()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp,
        ),
        containerColor = IrisSurface,
        tonalElevation = 6.dp,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SwitcherTopBar(
                onClose = onDismiss,
                onCreate = {},
                searchText = "",
                onSearchChange = {},
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
            ) {
                SessionList(
                    sessions = sessions,
                    activeId = activeId,
                    committingId = null,
                    onCommit = {},
                    onRename = {},
                    onDelete = {},
                )
            }
        }
    }
}

/* -------------------------------------------------------------------------- */
/*                              Top Bar                                       */
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
                    painter = painterResource(R.drawable.lucide_x),
                    contentDescription = "Close switcher",
                    tint = IrisText,
                )
            }
            Text(
                text = "Sessions",
                color = IrisText,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.weight(1f),
            )
            FilledIconButton(
                onClick = onCreate,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = IrisPrimary,
                    contentColor = IrisSurface,
                ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.lucide_plus),
                    contentDescription = "New session",
                    modifier = Modifier.size(22.dp),
                )
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
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.lucide_search),
                    contentDescription = null,
                    tint = IrisTextMuted,
                    modifier = Modifier.size(19.dp),
                )
            },
        )
    }
}

/* -------------------------------------------------------------------------- */
/*                              Session List                                  */
/* -------------------------------------------------------------------------- */

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
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        itemsIndexed(sessions, key = { _, snapshot -> snapshot.id }) { _, snapshot ->
            val isActive = snapshot.id == activeId
            val isCommitting = committingId != null && committingId == snapshot.id
            SessionCard(
                snapshot = snapshot,
                isActive = isActive,
                isCommitting = isCommitting,
                onActivate = { onCommit(snapshot.id) },
                onRename = { onRename(snapshot) },
                onDelete = { onDelete(snapshot) },
            )
        }
    }
}
