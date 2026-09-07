package com.iris.irisshell.ui.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisError
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.design.system.OutfitFontFamily
import com.iris.irisshell.ui.R
import com.iris.irisshell.ui.session.SessionSwitcherViewModel

/**
 * Modern minimalist top bar — pills float directly on terminal background.
 *
 *  Left:   [pill] session-name-surface        (session name is NOT clickable)
 *  Right:  [kbd] [more]                      (floating pills, no surface)
 *
 * No container background — pills and text sit directly on terminal.
 */
@Composable
fun TerminalTopBar(
    viewModel: SessionSwitcherViewModel,
    isFullscreen: Boolean,
    keyboardFocused: Boolean,
    onToggleKeyboard: () -> Unit,
    onOpenSidebar: () -> Unit,
    onRefresh: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeName by viewModel.activeName.collectAsStateWithLifecycle()

    val statusBarH = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Transparent container — pills float directly on terminal background
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp + statusBarH)
            .padding(top = statusBarH),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // ── Left: floating pill button + session name surface ───────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                 FloatingPillButton(
                    iconRes = R.drawable.lucide_panel_left,
                    contentDescription = "Open sessions",
                    onClick = onOpenSidebar,
                    size = 40.dp,
                )

                // Session name — has its own surface, NOT clickable
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(IrisSurfaceVariant)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = activeName ?: "IrisShell",
                        color = IrisText,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                    )
                }
            }

            // ── Right: two floating pills side by side ─────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                FloatingPillButton(
                    iconRes = if (keyboardFocused) R.drawable.lucide_keyboard_off else R.drawable.lucide_keyboard,
                    contentDescription = if (keyboardFocused) "Hide keyboard" else "Show keyboard",
                    onClick = onToggleKeyboard,
                    size = 40.dp,
                )

                var moreExpanded by remember { mutableStateOf(false) }
                FloatingPillButton(
                    iconRes = R.drawable.lucide_ellipsis_vertical,
                    contentDescription = "More actions",
                    onClick = { moreExpanded = true },
                    size = 40.dp,
                )

                MoreActionsDropdown(
                    expanded = moreExpanded,
                    onDismiss = { moreExpanded = false },
                    isFullscreen = isFullscreen,
                    onRefresh = { onRefresh(); moreExpanded = false },
                    onToggleFullscreen = { onToggleFullscreen(); moreExpanded = false },
                    onOpenSettings = { onOpenSettings(); moreExpanded = false },
                    onClose = { onClose(); moreExpanded = false },
                )
            }
        }
    }
}

@Composable
private fun MoreActionsDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    isFullscreen: Boolean,
    onRefresh: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit,
) {
    val statusBarH = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = IrisSurface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(12.dp),
        offset = DpOffset(x = 0.dp, y = statusBarH + 48.dp),
    ) {
        DropdownMenuItem(
            onClick = { onRefresh() },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.lucide_rotate_cw),
                        contentDescription = null,
                        tint = IrisTextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Refresh terminal",
                        color = IrisText,
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
        )
        DropdownMenuItem(
            onClick = { onToggleFullscreen() },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        painter = painterResource(
                            if (isFullscreen) R.drawable.lucide_minimize else R.drawable.lucide_maximize
                        ),
                        contentDescription = null,
                        tint = IrisTextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = if (isFullscreen) "Exit fullscreen" else "Enter fullscreen",
                        color = IrisText,
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
        )
        Divider(
            color = IrisBorderSubtle,
            thickness = 1.dp,
            modifier = Modifier.padding(vertical = 4.dp),
        )
        DropdownMenuItem(
            onClick = { onOpenSettings() },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.lucide_settings),
                        contentDescription = null,
                        tint = IrisTextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Settings",
                        color = IrisText,
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
        )
        DropdownMenuItem(
            onClick = { onClose() },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.lucide_x_circle),
                        contentDescription = null,
                        tint = IrisError,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Close session",
                        color = IrisError,
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
        )
    }
}

/**
 * Floating pill button — no surface/background, sits directly on terminal.
 * Uses a very subtle hover alpha on the icon background for affordance.
 */
@Composable
private fun FloatingPillButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 36.dp,
) {
    var pressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (pressed) IrisTextSecondary.copy(alpha = 0.08f) else Color.Transparent)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                        onClick()
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = IrisTextSecondary,
            modifier = Modifier.size(18.dp),
        )
    }
}
