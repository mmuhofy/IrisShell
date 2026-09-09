package com.iris.irisshell.ui.topbar

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisError
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.design.system.OutfitFontFamily
import com.iris.irisshell.ui.R
import com.iris.irisshell.ui.session.SessionSwitcherViewModel

/**
 * Modern minimalist top bar — iOS/Obsidian-style floating pills.
 *
 * Değişiklik notları (önceki versiyona göre):
 *  - Pill butonlar artık gerçek bir surface'a sahip (önceden tamamen
 *    şeffaftı, sadece basılınca hafif alpha görünüyordu).
 *  - Boyutlar büyütüldü: buton 36/40dp → 44dp, ikon 18dp → 22dp (iOS ölçeği).
 *  - Session-name kutusu stadium (tam yuvarlak) pill'e çevrildi.
 *  - Basma anında hafif scale-down animasyonu (spring, bounce yok) —
 *    iOS tarzı dokunma geri bildirimi.
 *  - "Vibrancy" simülasyonu: gerçek backdrop blur DEĞİL (Compose'da bunun
 *    native karşılığı yok, bkz. sohbet notu). Bunun yerine yarı saydam
 *    surface + ince kenarlık + üstte hafif highlight gradyanı ile
 *    "buzlu cam" hissi veriliyor. Gerçek blur için Haze kütüphanesi
 *    gerekir — ayrı bir adım olarak ele alınmalı.
 *  - MoreActionsDropdown: hardcoded offset kaldırıldı (anchor'a göre
 *    otomatik konumlanıyor), Divider → HorizontalDivider.
 *
 * Public API değişmedi: TerminalTopBar(...) imzası aynı.
 */
@Composable
fun TerminalTopBar(
    viewModel: SessionSwitcherViewModel,
    isFullscreen: Boolean,
    keyboardFocused: Boolean,
    onToggleKeyboard: () -> Unit,
    onOpenSidebar: () -> Unit,
    onFindInOutput: () -> Unit,
    onRefresh: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onNewSession: () -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeName by viewModel.activeName.collectAsStateWithLifecycle()

    val statusBarH = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp + statusBarH)
            .padding(top = statusBarH),
    ) {
        var moreExpanded by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            // ── Left: pill icon button + session name pill ──────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GlassPillButton(
                    iconRes = R.drawable.lucide_panel_left,
                    contentDescription = "Open sessions",
                    onClick = onOpenSidebar,
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(percent = 50))
                        .background(IrisSurfaceVariant.copy(alpha = 0.72f))
                        .border(
                            width = 1.dp,
                            color = IrisBorderSubtle.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(percent = 50),
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    Text(
                        text = activeName ?: "IrisShell",
                        color = IrisText,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                    )
                }
            }
        }

        // ── Right: two pill buttons ────────────────────────────────────────
        Box(
            Modifier
                .wrapContentSize()
                .align(Alignment.CenterEnd)
                .padding(horizontal = 12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                GlassPillButton(
                    iconRes = if (keyboardFocused) R.drawable.lucide_keyboard_off else R.drawable.lucide_keyboard,
                    contentDescription = if (keyboardFocused) "Hide keyboard" else "Show keyboard",
                    onClick = onToggleKeyboard,
                )

                GlassPillButton(
                    iconRes = R.drawable.lucide_ellipsis_vertical,
                    contentDescription = "More actions",
                    onClick = { moreExpanded = true },
                )
            }

            MoreActionsDropdown(
                expanded = moreExpanded,
                onDismiss = { moreExpanded = false },
                isFullscreen = isFullscreen,
                onFindInOutput = { onFindInOutput(); moreExpanded = false },
                onRefresh = { onRefresh(); moreExpanded = false },
                onToggleFullscreen = { onToggleFullscreen(); moreExpanded = false },
                onNewSession = { onNewSession(); moreExpanded = false },
                onOpenSettings = { onOpenSettings(); moreExpanded = false },
                onClose = { onClose(); moreExpanded = false },
            )
        }
    }
}

@Composable
private fun MoreActionsDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    isFullscreen: Boolean,
    onFindInOutput: () -> Unit,
    onRefresh: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit,
) {
    // Not: hardcoded offset kaldırıldı — DropdownMenu artık anchor'ı olan
    // composable'a (bu Box) göre Compose tarafından otomatik konumlanıyor.
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        containerColor = IrisSurfaceVariant,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.border(
            width = 1.dp,
            color = IrisBorderSubtle.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp),
        ),
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
            onClick = { onNewSession() },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.lucide_plus),
                        contentDescription = null,
                        tint = IrisTextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "New session",
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
        DropdownMenuItem(
            onClick = { onFindInOutput() },
            text = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.lucide_search),
                        contentDescription = null,
                        tint = IrisTextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Find in output",
                        color = IrisText,
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
        )
        HorizontalDivider(
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
 * iOS-style glass pill button.
 *
 * Gerçek backdrop blur uygulamaz (Compose'da native karşılığı yok).
 * Bunun yerine yarı saydam surface + ince kenarlık + üstte hafif
 * highlight gradyanı ile "buzlu cam" hissi simüle edilir. Basılınca
 * hafif scale-down (spring, bounce yok) ile dokunma geri bildirimi verir.
 */
@Composable
private fun GlassPillButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "pillButtonScale",
    )

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(IrisSurfaceVariant.copy(alpha = if (pressed) 0.85f else 0.62f))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.06f),
                        Color.Transparent,
                    ),
                ),
                shape = CircleShape,
            )
            .border(
                width = 1.dp,
                color = IrisBorderSubtle.copy(alpha = 0.6f),
                shape = CircleShape,
            )
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
            tint = IrisText,
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        )
    }
}
