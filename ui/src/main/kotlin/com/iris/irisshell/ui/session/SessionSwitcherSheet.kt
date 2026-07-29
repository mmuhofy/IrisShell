package com.iris.irisshell.ui.session

import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.delay

/**
 * Centred popup dialog over the terminal screen — swipeable pager.
 *
 * Visual:
 *   - **No scrim/dim** — the dialog's window dim amount is forced to 0.
 *   - **Blur background on Android 12+** — terminal still composited, blurred.
 *   - **Spring enter/leave** — scale 0.88 → 1.0 + alpha 0 → 1 over 280ms.
 *
 * Interaction:
 *   - **Swipe** pages left/right via HorizontalPager.
 *   - **Tap a card** → activate + dismiss.
 *   - **× button** → dismiss without activating.
 *
 * Selection animation:
 *   When the user taps a card, we play a brief "commit" flash:
 *     - The tapped card scales up to 1.06× and glows gold for ~120ms.
 *     - The whole dialog then collapses (scale 0.94 + fade) over 200ms.
 *     - The result feels like the chosen card "explodes forward" into the
 *       terminal view beneath.
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

    DisableDialogScrim()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        BlurDialogWindow(radiusDp = 22f, enabled = !showCreateDialog)
    }

    // When the sheet is mid-commit we want a snappier exit so the
    // transition into the terminal feels instant.
    val inCommit = committingId != null

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
                            PagerContent(
                                sessions = sessions,
                                activeId = activeId,
                                committingId = committingId,
                                onCommit = { id ->
                                    if (committingId == null) {
                                        committingId = id
                                        viewModel.activate(id)
                                    }
                                },
                                onDismiss = onDismiss,
                            )
                        }
                    }
                }
            }
        }
    }

    // Fire dismiss once the commit animation has played.
    LaunchedEffect(committingId) {
        val id = committingId ?: return@LaunchedEffect
        delay(220)
        onDismiss()
        // Reset for next time the sheet opens.
        committingId = null
        // Suppress unused warning.
        @Suppress("UNUSED_EXPRESSION") id
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
private fun PagerContent(
    sessions: List<com.iris.irisshell.domain.session.SessionSnapshot>,
    activeId: String?,
    committingId: String?,
    onCommit: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val activeIndex = sessions.indexOfFirst { it.id == activeId }.coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = activeIndex) {
        sessions.size
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 12.dp,
        ) { pageIndex ->
            val snapshot = sessions[pageIndex]
            val isCommitting = committingId != null && committingId == snapshot.id
            SessionCard(
                snapshot = snapshot,
                isActive = snapshot.id == activeId,
                isCommitting = isCommitting,
                onActivate = { onCommit(snapshot.id) },
                modifier = Modifier.fillMaxSize(),
            )
        }

        // Page-indicator dots.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
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
    }
}

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
