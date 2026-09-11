package com.iris.irisshell.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.ui.R
import com.iris.irisshell.ui.pin.PinEntryScreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val useBlockEngine    by viewModel.useBlockEngine.collectAsStateWithLifecycle(initialValue = false)
    val fontSizeSp        by viewModel.fontSizeSp.collectAsStateWithLifecycle(initialValue = 14)
    val terminalBgColor   by viewModel.terminalBgColor.collectAsStateWithLifecycle(initialValue = "#000000")
    val accentColor       by viewModel.accentColor.collectAsStateWithLifecycle(initialValue = "#3B82F6")
    val terminalTextColor by viewModel.terminalTextColor.collectAsStateWithLifecycle(initialValue = "#E8E8E8")
    val prootStartCommand by viewModel.prootStartCommand.collectAsStateWithLifecycle(initialValue = "")
    val isPinLockEnabled  by viewModel.isPinLockEnabled.collectAsStateWithLifecycle(initialValue = false)
    val aboutInfo         by viewModel.aboutInfo.collectAsStateWithLifecycle(initialValue = null)

    var showPinEntry by rememberSaveable { mutableStateOf(false) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        state = androidx.compose.material3.rememberTopAppBarState(),
    )

    Scaffold(
        containerColor = IrisBackground,
        topBar = {
            ModernSettingsTopBar(title = "Settings", onBack = onBack, scrollBehavior = scrollBehavior)
        },
    ) { innerPadding: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .padding(innerPadding),
        ) {
            SettingsSectionLabel("Terminal")
            TerminalModeCard(
                useBlockEngine = useBlockEngine,
                onSelect = { viewModel.setUseBlockEngine(it) },
            )
            Spacer(Modifier.height(16.dp))

            SettingsCategoryCard {
                ColorPickerRow(
                    iconRes = R.drawable.lucide_minimize,
                    label = "Background",
                    description = "Arkaplan rengi",
                    options = listOf(
                        com.iris.irisshell.design.system.IrisBackground,
                        com.iris.irisshell.design.system.IrisSurface,
                        com.iris.irisshell.design.system.IrisSurfaceVariant,
                    ),
                    selectedHex = terminalBgColor,
                    onSelect = { viewModel.setTerminalBgColor(it) },
                    onHexChange = { viewModel.setTerminalBgColor(it) },
                )
            }
            SettingsDivider()

            SettingsCategoryCard {
                ColorPickerRow(
                    iconRes = R.drawable.lucide_palette,
                    label = "Accent",
                    description = "Vurgu rengi (cyan)",
                    options = listOf(IrisPrimary),
                    selectedHex = accentColor,
                    onSelect = { viewModel.setAccentColor(it) },
                    onHexChange = { viewModel.setAccentColor(it) },
                )
            }
            SettingsDivider()

            SettingsCategoryCard {
                ColorPickerRow(
                    iconRes = R.drawable.lucide_a_large_small,
                    label = "Text",
                    description = "Metin rengi",
                    options = listOf(IrisText, IrisTextSecondary),
                    selectedHex = terminalTextColor,
                    onSelect = { viewModel.setTerminalTextColor(it) },
                    onHexChange = { viewModel.setTerminalTextColor(it) },
                )
            }
            SettingsDivider()

            SettingsCategoryCard {
                FontSizeSliderRow(
                    fontSizeSp = fontSizeSp,
                    onSizeChange = { viewModel.setFontSize(it) },
                )
            }
            SettingsDivider()

            SettingsCategoryCard {
                ProotStartCommandRow(
                    value = prootStartCommand,
                    onValueChange = { viewModel.setProotStartCommand(it) },
                )
            }
            Spacer(Modifier.height(24.dp))

            SettingsSectionLabel("Güvenlik")
            SettingsCategoryCard {
                SettingsToggleRow(
                    iconRes = R.drawable.lucide_lock,
                    label = "Uygulama Kilidi",
                    description = "4 haneli PIN (güvenli saklama)",
                    checked = isPinLockEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            showPinEntry = true
                        } else {
                            scope.launch { viewModel.clearPin() }
                        }
                    },
                )
            }
            Spacer(Modifier.height(24.dp))

            SettingsSectionLabel("Hakkında")
            SettingsCategoryCard {
                InfoRow(label = "Versiyon", value = aboutInfo?.version ?: "—")
                SettingsDivider()
                InfoRow(label = "Açıklama", value = aboutInfo?.build ?: "Advanced terminal, but not the best.")
                SettingsDivider()
                InfoRow(label = "Lisans", value = aboutInfo?.license ?: "MIT")
            }
        }
    }

    if (showPinEntry) {
        PinEntryScreen(
            title = "Set PIN",
            subtitle = "Enter a new 4-digit PIN",
            onPinReady = { pin ->
                scope.launch {
                    viewModel.setPin(pin)
                    viewModel.setPinLockEnabled(true)
                    showPinEntry = false
                }
            },
            onCancel = { showPinEntry = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSettingsTopBar(
    title: String,
    onBack: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior,
) {
    MediumTopAppBar(
        windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = IrisSurface,
            scrolledContainerColor = IrisSurface,
        ),
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(32.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.lucide_arrow_left),
                    contentDescription = "Back",
                    tint = IrisTextSecondary,
                    modifier = Modifier.size(16.dp),
                )
            }
        },
        title = {
            Text(
                text = title,
                color = IrisText,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
    )
}

private fun Color.Companion.fromHex(hex: String): Color {
    val c = hex.removePrefix("#")
    return Color(
        red = c.substring(0, 2).toInt(16) / 255f,
        green = c.substring(2, 4).toInt(16) / 255f,
        blue = c.substring(4, 6).toInt(16) / 255f,
    )
}
