package com.iris.irisshell.ui.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateTopPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisError
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.design.system.OutfitFontFamily
import com.iris.irisshell.ui.R
import com.iris.irisshell.ui.session.SessionSwitcherViewModel

/**
 * Terminal top bar — Obsidian Mobile tarzı.
 *
 *  Sol:   panel-left icon + oturum adı → tıkla → sidebar aç
 *  Sağ:   iki yana dokunuk pill buton (keyboard toggle + more actions)
 *
 * Butonlar iOS-style pill: yana dokunuk, border var ama elevation yok.
 * Etrafında container/elevation yok — doğrudan top bar üzerinde.
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(IrisSurface)
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
            // ── Left: panel icon + session name ───────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onOpenSidebar)
                    .padding(horizontal = 8.dp, vertical = 6.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.lucide_panel_left),
                    contentDescription = "Open session menu",
                    tint = IrisTextSecondary,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        text = activeName ?: "IrisShell",
                        color = IrisText,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                    Text(
                        text = "tap to switch",
                        color = IrisTextMuted,
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                    )
                }
            }

            // ── Right: two pill buttons side by side ──────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                PillButton(
                    iconRes = if (keyboardFocused) R.drawable.lucide_keyboard_off else R.drawable.lucide_keyboard,
                    contentDescription = if (keyboardFocused) "Hide keyboard" else "Show keyboard",
                    onClick = onToggleKeyboard,
                )

                var moreExpanded by remember { mutableStateOf(false) }
                PillButton(
                    iconRes = R.drawable.lucide_ellipsis_vertical,
                    contentDescription = "More actions",
                    onClick = { moreExpanded = true },
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
    val offsetY = with(LocalDensity.current) { (48.dp + statusBarH).roundToPx() }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = IrisSurfaceVariant,
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

@Composable
private fun PillButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(IrisSurfaceVariant)
            .border(width = 1.dp, color = IrisBorderSubtle, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = IrisTextSecondary,
            modifier = Modifier.size(20.dp),
        )
    }
}
