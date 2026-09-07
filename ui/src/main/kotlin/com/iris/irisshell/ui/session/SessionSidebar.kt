package com.iris.irisshell.ui.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisError
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.design.system.OutfitFontFamily
import com.iris.irisshell.domain.session.SessionSnapshot
import com.iris.irisshell.ui.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Slide-in sol sidebar — Obsidian Mobile referanslı, modern minimalist tasarım.
 *
 * Değişiklik notları (önceki versiyona göre):
 *  - Gerçek slide-in/out animasyonu (spring, bounce yok) — AnimatedVisibility ile.
 *  - Session item'ları düz pill stiline çevrildi (Obsidian gibi), state text kaldırıldı.
 *  - Inline rename: dropdown'dan "Rename" seçilince item text field'a dönüşür.
 *    (Önceki kodda "Rename" aslında mevcut ismi tekrar kaydediyordu — gerçek bir
 *    input alanı yoktu. Bu düzeltildi.)
 *  - Alt kısım: ikon toolbar (Yeni session + Arama) + kullanıcı profili satırı.
 *  - "Close session" — hiçbir şey yapmayan ölü UI elemanıydı, kaldırıldı.
 *  - Dropdown menu artık hardcoded offset kullanmıyor, anchor'a göre otomatik konumlanıyor.
 *  - Divider → HorizontalDivider (Material3, deprecated API düzeltmesi).
 *  - forEach + verticalScroll → LazyColumn (ölçeklenebilirlik).
 *
 * Public API değişmedi: SessionSidebar(isOpen, onDismiss, onOpenSettings).
 * userDisplayName opsiyonel — mevcut çağrı yerlerini bozmaz.
 */
@Composable
fun SessionSidebar(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
    userDisplayName: String = "User",
) {
    val viewModel: SessionSwitcherViewModel = hiltViewModel()
    val config = LocalConfiguration.current
    val sidebarW = remember(config) {
        val sw = config.screenWidthDp
        if (sw > 0) (sw * 0.75f).coerceAtMost(280f).dp else 280.dp
    }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    onClick = onDismiss,
                )
                .zIndex(1f),
        ) {
            AnimatedVisibility(
                visible = isOpen,
                modifier = Modifier.align(Alignment.CenterStart),
                enter = slideInHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    initialOffsetX = { fullWidth -> -fullWidth },
                ),
                exit = slideOutHorizontally(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                    targetOffsetX = { fullWidth -> -fullWidth },
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(sidebarW)
                        .clip(RoundedCornerShape(0.dp, 16.dp, 16.dp, 0.dp))
                        .background(IrisSurfaceVariant)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        // Panel içine tıklama scrim'e sızıp sidebar'ı kapatmasın.
                        .clickable(
                            indication = null,
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            onClick = {},
                        ),
                ) {
                    SidebarContent(
                        viewModel = viewModel,
                        onClose = onDismiss,
                        onOpenSettings = onOpenSettings,
                        userDisplayName = userDisplayName,
                    )
                }
            }
        }
    }
}

@Composable
private fun SidebarContent(
    viewModel: SessionSwitcherViewModel,
    onClose: () -> Unit,
    onOpenSettings: () -> Unit,
    userDisplayName: String,
) {
    val sessions by viewModel.allSessions.collectAsStateWithLifecycle()
    val activeId by viewModel.activeId.collectAsStateWithLifecycle()

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var renamingSessionId by remember { mutableStateOf<String?>(null) }
    var renameValue by remember { mutableStateOf("") }

    val filteredSessions = remember(sessions, searchQuery) {
        if (searchQuery.isBlank()) {
            sessions
        } else {
            sessions.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    fun commitRename() {
        val id = renamingSessionId
        val newName = renameValue.trim()
        if (id != null && newName.isNotEmpty()) {
            viewModel.rename(id, newName)
        }
        renamingSessionId = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        // Obsidian tarzı: sabit header yok. İstenirse arama burada inline açılır.
        if (isSearchActive) {
            SearchField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                onClose = {
                    isSearchActive = false
                    searchQuery = ""
                },
            )
            Spacer(Modifier.height(8.dp))
        }

        // Session listesi — flat pill stili, LazyColumn (performans için).
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (filteredSessions.isEmpty()) {
                item {
                    Text(
                        text = if (searchQuery.isBlank()) "No active sessions" else "No results",
                        color = IrisTextMuted,
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                    )
                }
            } else {
                items(filteredSessions, key = { it.id }) { snapshot ->
                    SessionItem(
                        snapshot = snapshot,
                        isActive = snapshot.id == activeId,
                        isRenaming = renamingSessionId == snapshot.id,
                        renameValue = renameValue,
                        onRenameValueChange = { renameValue = it },
                        onRenameCommit = { commitRename() },
                        onRenameCancel = { renamingSessionId = null },
                        onClick = {
                            if (renamingSessionId == null) viewModel.activate(snapshot.id)
                        },
                        onStartRename = {
                            renamingSessionId = snapshot.id
                            renameValue = snapshot.name
                        },
                        onDelete = { viewModel.delete(snapshot.id) },
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = IrisBorderSubtle, thickness = 1.dp)
        Spacer(Modifier.height(4.dp))

        // Alt ikon toolbar — Yeni session + Arama.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { viewModel.createNew("shell") }) {
                Icon(
                    painter = painterResource(R.drawable.lucide_plus),
                    contentDescription = "New session",
                    tint = IrisText,
                    modifier = Modifier.size(18.dp),
                )
            }
            // UNTESTED — verify R.drawable.lucide_search exists in the project.
            IconButton(onClick = { isSearchActive = !isSearchActive }) {
                Icon(
                    painter = painterResource(R.drawable.lucide_search),
                    contentDescription = "Search sessions",
                    tint = if (isSearchActive) IrisPrimary else IrisText,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        HorizontalDivider(color = IrisBorderSubtle, thickness = 1.dp)
        Spacer(Modifier.height(4.dp))

        // Kullanıcı profili satırı — Obsidian tarzı.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onOpenSettings)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(
                    text = userDisplayName,
                    color = IrisText,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
                Text(
                    text = "${sessions.size} session(s)",
                    color = IrisTextMuted,
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                )
            }
            IconButton(onClick = onOpenSettings) {
                Icon(
                    painter = painterResource(R.drawable.lucide_settings),
                    contentDescription = "Settings",
                    tint = IrisTextSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        // Alan ekrana geldiğinde klavye odaklansın.
        focusRequester.requestFocus()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(IrisSurfaceVariant)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            singleLine = true,
            textStyle = TextStyle(
                color = IrisText,
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { /* no-op, live filter already applied */ }),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = "Search sessions",
                        color = IrisTextMuted,
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                    )
                }
                inner()
            },
        )
        IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
            Icon(
                painter = painterResource(R.drawable.lucide_x),
                contentDescription = "Close search",
                tint = IrisTextSecondary,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
private fun SessionItem(
    snapshot: SessionSnapshot,
    isActive: Boolean,
    isRenaming: Boolean,
    renameValue: String,
    onRenameValueChange: (String) -> Unit,
    onRenameCommit: () -> Unit,
    onRenameCancel: () -> Unit,
    onClick: () -> Unit,
    onStartRename: () -> Unit,
    onDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val nameColor = if (isActive) IrisPrimary else IrisText

    LaunchedEffect(isRenaming) {
        if (isRenaming) {
            // Text field kompozisyona girdikten hemen sonra odak istenir.
            focusRequester.requestFocus()
        }
    }

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(percent = 50))
                .background(if (isActive) IrisSurfaceVariant.copy(alpha = 1f) else Color.Transparent)
                .then(
                    if (!isRenaming) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isRenaming) {
                BasicTextField(
                    value = renameValue,
                    onValueChange = onRenameValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            if (!state.isFocused) onRenameCommit()
                        },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = nameColor,
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { onRenameCommit() },
                    ),
                )
            } else {
                Text(
                    text = snapshot.name,
                    color = nameColor,
                    fontFamily = OutfitFontFamily,
                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                    fontSize = 15.sp,
                    modifier = Modifier.weight(1f),
                )
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(28.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.lucide_ellipsis_vertical),
                        contentDescription = "More",
                        tint = IrisTextSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }

        // Offset artık hardcoded değil — anchor'a (bu Box) göre Compose otomatik konumlandırır.
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            containerColor = IrisSurfaceVariant,
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(12.dp),
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
                onClick = {
                    showMenu = false
                    onStartRename()
                },
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
