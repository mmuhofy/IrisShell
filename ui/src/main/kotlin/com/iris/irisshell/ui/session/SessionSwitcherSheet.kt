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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.ui.util.BlurDialogWindow
import android.view.Window

/**
 * Centred popup dialog over the terminal screen — iOS App Switcher style.
 *
 * Visual:
 *   - **No scrim/dim** — the dialog's window dim amount is forced to 0,
 *     so the terminal underneath stays at full brightness.
 *   - **Blur background on Android 12+** — terminal still composited
 *     underneath, just blurred. API <31 has no RenderEffect so it
 *     simply renders unblurred (and undimmed).
 *   - **Spring enter/leave** — scale 0.88 → 1.0 + alpha 0 → 1 over
 *     ~280ms with a medium-bouncy spring.
 *
 * Interaction (handled by [LongPressSessionGrid]):
 *   - **Tap** a card → activate + dismiss.
 *   - **Long-press 150ms** → drag mode (cards shrink, finger-following
 *     tooltip, gold ring under finger, haptic ticks).
 *   - **Release on a card** during drag → activate that card.
 *   - **Release outside any card** during drag → dismiss without
 *     changing session.
 */
@Composable
fun SessionSwitcherSheet(
    onDismiss: () -> Unit,
    viewModel: SessionSwitcherViewModel = hiltViewModel(),
) {
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val activeId by viewModel.activeId.collectAsStateWithLifecycle()

    var showCreateDialog by remember { mutableStateOf(false) }

    // Disable the default scrim (dim) on the dialog window. We want the
    // terminal to stay bright behind the popup; the blur is sufficient.
    DisableDialogScrim()

    // On Android 12+ the dialog's parent window is blurred while the
    // popup is on screen. On older devices this is a no-op.
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        BlurDialogWindow(radiusDp = 22f, enabled = !showCreateDialog)
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
                targetScale = 0.92f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
            ) + fadeOut(
                animationSpec = spring(stiffness = Spring.StiffnessMedium),
            ),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(540.dp)
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
                    )

                    Box(modifier = Modifier.fillMaxSize()) {
                        if (sessions.isEmpty()) {
                            EmptyState(onCreate = { showCreateDialog = true })
                        } else {
                            LongPressSessionGrid(
                                sessions = sessions,
                                activeId = activeId,
                                onActivate = { id ->
                                    viewModel.activate(id)
                                    onDismiss()
                                },
                                onDismiss = onDismiss,
                            )
                        }
                    }
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
}

/**
 * Side-effect-only composable that walks up from [LocalView] to find
 * the host dialog window and forces its dim amount to zero. This makes
 * the popup's parent window transparent behind the surface, so the
 * terminal stays fully visible (no black scrim).
 *
 * Calling `setDimAmount(0f)` is safe across all supported API levels
 * (it's been in [Window] since API 1).
 */
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

@Composable
private fun SwitcherTopBar(
    onClose: () -> Unit,
    onCreate: () -> Unit,
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
private fun EmptyState(onCreate: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
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
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New session",
                color = IrisText,
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text("Name") },
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name.ifBlank { "shell" }) },
            ) {
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
