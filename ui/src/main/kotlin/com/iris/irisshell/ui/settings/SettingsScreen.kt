package com.iris.irisshell.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.ArrowBackimport androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisSurfaceVariant
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
    val useBlockEngine     by viewModel.useBlockEngine.collectAsStateWithLifecycle()
    val extraKeysBarVisible by viewModel.extraKeysBarVisible.collectAsStateWithLifecycle()
    val fontSizeSp         by viewModel.fontSizeSp.collectAsStateWithLifecycle()
    val terminalBgColor    by viewModel.terminalBgColor.collectAsStateWithLifecycle()
    val accentColor        by viewModel.accentColor.collectAsStateWithLifecycle()
    val terminalTextColor  by viewModel.terminalTextColor.collectAsStateWithLifecycle()
    val prootStartCommand  by viewModel.prootStartCommand.collectAsStateWithLifecycle()
    val isPinLockEnabled by viewModel.isPinLockEnabled.collectAsStateWithLifecycle()

    var showPinEntry by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = IrisBackground,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text       = "Settings",
                        color      = IrisText,
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector    = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint           = IrisTextSecondary,
                        modifier       = Modifier
                            .size(width = 24.dp, height = 24.dp)
                            .clickable(onClick = onBack),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IrisSurface,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
        ) {
            SettingsSectionLabel("Terminal")
            TerminalModeCard(
                useBlockEngine = useBlockEngine,
                onSelect       = { viewModel.setUseBlockEngine(it) },
            )
            Spacer(Modifier.height(8.dp))

            SettingsCategoryCard {
                SettingsToggleRow(
                    iconRes         = R.drawable.lucide_keyboard,
                    label           = "Extra Keys Bar",
                    description     = "ESC, TAB, CTRL, ALT, yön tuşları",
                    checked         = extraKeysBarVisible,
                    onCheckedChange = { viewModel.setExtraKeysBarVisible(it) },
                )
            }
            Spacer(Modifier.height(24.dp))

            SettingsSectionLabel("Görünüm")
            SettingsCategoryCard {
                FontSizeSliderRow(
                    fontSizeSp   = fontSizeSp,
                    onSizeChange = { viewModel.setFontSize(it) },
                )
                SettingsDivider()
                ColorPickerRow(
                    iconRes     = R.drawable.lucide_minimize,
                    label       = "Arkaplan",
                    description = "Terminal zemin rengi",
                    options     = listOf(IrisBackground, IrisSurface, IrisSurfaceVariant),
                    selectedHex = terminalBgColor,
                    onSelect    = { viewModel.setTerminalBgColor(it) },
                )
                SettingsDivider()
                ColorPickerRow(
                    iconRes     = R.drawable.lucide_palette,
                    label       = "Vurgu Rengi",
                    description = "Komut istemi ve aktif öğeler",
                    options     = listOf(IrisPrimary),
                    selectedHex = accentColor,
                    onSelect    = { viewModel.setAccentColor(it) },
                )
                SettingsDivider()
                ColorPickerRow(
                    iconRes     = R.drawable.lucide_a_large_small,
                    label       = "Metin Rengi",
                    description = "Terminal çıktı metni",
                    options     = listOf(IrisText, IrisTextSecondary),
                    selectedHex = terminalTextColor,
                    onSelect    = { viewModel.setTerminalTextColor(it) },
                )
            }
            Spacer(Modifier.height(24.dp))

            SettingsSectionLabel("Güvenlik")
            SettingsCategoryCard {
                SettingsToggleRow(
                    iconRes         = R.drawable.lucide_lock,
                    label           = "Uygulama Kilidi",
                    description     = "4 haneli PIN (güvenleştirilmiş saklama)",
                    checked         = isPinLockEnabled,
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

            SettingsSectionLabel("Gelişmiş")
            SettingsCategoryCard {
                ProotStartCommandRow(
                    value      = prootStartCommand,
                    onValueChange = { viewModel.setProotStartCommand(it) },
                )
            }
            Spacer(Modifier.height(24.dp))
            SettingsCategoryCard {
                InfoRow(label = "Versiyon", value = "1.0.0")
                SettingsDivider()
                InfoRow(label = "Build", value = "Phase 1 — Terminal Core")
                SettingsDivider()
                InfoRow(label = "Lisans", value = "MIT")
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
                }
                showPinEntry = false
            },
            onCancel = { showPinEntry = false },
        )
    }
}
