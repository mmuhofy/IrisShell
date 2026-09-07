package com.iris.irisshell.ui.session

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.iris.irisshell.domain.session.SessionSnapshot
import com.iris.irisshell.ui.R

/**
 * Slide-in sol sidebar — Obsidian Mobile tarzı.
 *
 *  ModalBottomSheet yerine doğrudan overlay: 0 scrim, slide-in animasyonu.
 *  Session listesi, yeni oturum, rename, delete — hepsi burada.
 */
@Composable
fun SessionSidebar(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    if (!isOpen) return

    val viewModel: SessionSwitcherViewModel = hiltViewModel()
    val config = LocalConfiguration.current
    val sidebarW = remember(config) {
        val sw = config.screenWidthDp
        if (sw > 0) {
            (sw * 0.75f).coerceAtMost(280f).dp
        } else {
            280.dp
        }
    }

    // Scrim overlay + slide-in panel
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(onClick = onDismiss)
            .zIndex(1f),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(sidebarW)
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(0.dp, 16.dp, 16.dp, 0.dp))
                .background(IrisSurfaceVariant)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            SidebarContent(
                viewModel = viewModel,
                onClose = onDismiss,
                onOpenSettings = onOpenSettings,
            )
        }
    }
}

@Composable
private fun SidebarContent(
    viewModel: SessionSwitcherViewModel,
    onClose: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val activeId by viewModel.activeId.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Sessions",
                color = IrisText,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            IconButton(onClick = onClose) {
                Icon(
                    painter = painterResource(R.drawable.lucide_x),
                    contentDescription = "Close",
                    tint = IrisTextSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Text(
            text = "${sessions.size} session(s)",
            color = IrisTextMuted,
            fontFamily = OutfitFontFamily,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
        )

        // New session button
        NewSessionButton(
            onClick = { viewModel.createNew("shell") },
        )
        Spacer(Modifier.height(12.dp))

        // Session list
        if (sessions.isEmpty()) {
            Text(
                text = "No active sessions",
                color = IrisTextMuted,
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
            )
        } else {
            sessions.forEach { snapshot ->
                SessionItem(
                    snapshot = snapshot,
                    isActive = snapshot.id == activeId,
                    onClick = { viewModel.activate(snapshot.id) },
                    onRename = { newName -> viewModel.rename(snapshot.id, newName) },
                    onDelete = { viewModel.delete(snapshot.id) },
                )
                Spacer(Modifier.height(4.dp))
            }
        }

        // Settings & close
        Spacer(Modifier.height(16.dp))
        Divider(color = IrisBorderSubtle, thickness = 1.dp)
        Spacer(Modifier.height(8.dp))
        SidebarActionItem(
            text = "Settings",
            iconRes = R.drawable.lucide_settings,
            onClick = onOpenSettings,
        )
        Spacer(Modifier.height(4.dp))
        SidebarActionItem(
            text = "Close session",
            iconRes = R.drawable.lucide_x_circle,
            onClick = { /* handled by top bar dropdown */ },
            isDestructive = true,
        )
    }
}

@Composable
private fun NewSessionButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(IrisPrimary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.lucide_plus),
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp),
            )
            Text(
                text = "New session",
                color = Color.White,
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun SessionItem(
    snapshot: SessionSnapshot,
    isActive: Boolean,
    onClick: () -> Unit,
    onRename: (String) -> Unit,
    onDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    val nameColor = if (isActive) IrisPrimary else IrisText
    val badgeColor = if (isActive) IrisPrimary else IrisTextMuted
    val statusBarH = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(if (isActive) IrisSurfaceVariant else IrisSurface)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = snapshot.name,
                    color = nameColor,
                    fontFamily = OutfitFontFamily,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 14.sp,
                )
                Text(
                    text = snapshot.state.name,
                    color = IrisTextMuted,
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                )
            }
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                painter = painterResource(R.drawable.lucide_ellipsis_vertical),
                contentDescription = "More",
                tint = IrisTextSecondary,
                modifier = Modifier.size(16.dp),
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            containerColor = IrisSurfaceVariant,
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(12.dp),
            offset = DpOffset(x = 0.dp, y = statusBarH + 48.dp),
        ) {
            DropdownMenuItem(
                onClick = { onClick(); showMenu = false },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.lucide_play),
                            contentDescription = null,
                            tint = IrisTextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Activate",
                            color = IrisText,
                            fontFamily = OutfitFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                },
            )
            DropdownMenuItem(
                onClick = { onRename(snapshot.name.trim()); showMenu = false },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.lucide_pencil),
                            contentDescription = null,
                            tint = IrisTextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Rename",
                            color = IrisText,
                            fontFamily = OutfitFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                },
            )
            DropdownMenuItem(
                onClick = { onDelete(); showMenu = false },
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.lucide_trash_2),
                            contentDescription = null,
                            tint = IrisError,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "Delete",
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
}

@Composable
private fun SidebarActionItem(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = if (isDestructive) IrisError else IrisTextSecondary,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = text,
            color = if (isDestructive) IrisError else IrisText,
            fontFamily = OutfitFontFamily,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}
