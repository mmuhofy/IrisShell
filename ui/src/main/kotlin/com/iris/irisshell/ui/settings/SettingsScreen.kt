package com.iris.irisshell.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.design.system.IrisError
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.OutfitFontFamily
import com.iris.irisshell.domain.settings.CursorStyle
import com.iris.irisshell.ui.IrisIcons
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val useBlockEngine    by viewModel.useBlockEngine.collectAsStateWithLifecycle(false)
    val fontSizeSp        by viewModel.fontSizeSp.collectAsStateWithLifecycle(14)
    val prootStartCommand by viewModel.prootStartCommand.collectAsStateWithLifecycle("")
    val isPinLockEnabled  by viewModel.isPinLockEnabled.collectAsStateWithLifecycle(false)
    val cursorStyle       by viewModel.cursorStyle.collectAsStateWithLifecycle("Block")
    val cursorBlinkRateMs by viewModel.cursorBlinkRateMs.collectAsStateWithLifecycle(500)
    val autoLockTimeout   by viewModel.autoLockTimeout.collectAsStateWithLifecycle("Immediately")
    val aboutInfo         by viewModel.aboutInfo.collectAsStateWithLifecycle(null)

    var showPinEntry by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(IrisBackground),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .testTag("settings_content"),
        ) {
            SettingsTopBar(onBack = onBack)

            SettingsSection(label = "Terminal") {
                SettingsSectionContainer {
                    TerminalModeRow(
                        useBlockEngine = useBlockEngine,
                        onSelect = { viewModel.setUseBlockEngine(it) },
                    )
                    TerminalPreviewCard()
                    SettingsSubRow(
                        icon = IrisIcons.Type,
                        label = "Cursor Style",
                    ) {
                        CursorSegmentedControl(
                            selected = cursorStyle,
                            options = listOf("Block", "Beam", "Underline"),
                            onSelect = { viewModel.setCursorStyle(CursorStyle.fromString(it)) },
                        )
                    }
                    SettingsSubRow(
                        icon = IrisIcons.Gauge,
                        label = "Cursor Blink Rate",
                        description = "Pulse interval",
                    ) {
                        BlinkRateSlider(
                            value = cursorBlinkRateMs,
                            onValueChange = { viewModel.setCursorBlinkRateMs(it) },
                        )
                    }
                    SettingsSubRow(
                        icon = IrisIcons.ALargeSmall,
                        label = "Font Size",
                    ) {
                        FontSizeStepper(
                            value = fontSizeSp,
                            onValueChange = { viewModel.setFontSize(it) },
                        )
                    }
                    SettingsSubRow(
                        icon = IrisIcons.Terminal,
                        label = "PRoot Start Command",
                        description = "Experimental — changing this can break sessions",
                    ) {
                        ProotCommandDisplay(
                            command = prootStartCommand.ifEmpty { "\$shell --login" },
                        )
                    }
                }
            }

            SettingsSection(label = "Security") {
                SettingsSectionContainer {
                    SettingsSubRow(
                        icon = IrisIcons.Lock,
                        iconTint = IrisError,
                        label = "App Lock (PIN)",
                        description = "Require PIN on launch",
                    ) {
                        SettingsToggleSwitch(
                            checked = isPinLockEnabled,
                            onCheckedChange = {
                                if (it) showPinEntry = true else scope.launch { viewModel.clearPin() }
                            },
                        )
                    }
                    SettingsNavigationRow(
                        icon = IrisIcons.Timer,
                        label = "Auto-Lock Timeout",
                        trailingText = autoLockTimeout,
                        onClick = {},
                    )
                }
            }

            SettingsSection(label = "About") {
                SettingsSectionContainer {
                    SettingsNavigationRow(
                        icon = IrisIcons.Info,
                        label = "Version",
                        trailingText = aboutInfo?.version ?: "—",
                        onClick = {},
                    )
                    SettingsNavigationRow(
                        icon = IrisIcons.Terminal,
                        label = "Description",
                        trailingText = aboutInfo?.build ?: "Advanced terminal",
                        onClick = {},
                    )
                    SettingsNavigationRow(
                        icon = IrisIcons.Shield,
                        label = "License",
                        trailingBadge = aboutInfo?.license ?: "MIT",
                        showTrailingIcon = true,
                        onClick = {},
                    )
                }
            }

            SettingsSectionContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 24.dp),
            ) {
                SettingsNavigationRow(
                    icon = IrisIcons.CircleUser,
                    label = "Made by Muhofy",
                    trailingText = null,
                    onClick = {},
                )
            }
        }
    }

    if (showPinEntry) {
        com.iris.irisshell.ui.pin.PinEntryScreen(
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

@Composable
fun SettingsTopBar(onBack: () -> Unit) {
    val statusBarH = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = statusBarH, start = 16.dp, end = 16.dp, bottom = 8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .clickable(
                    onClick = onBack,
                )
                .padding(vertical = 6.dp),
        ) {
            Icon(
                imageVector = IrisIcons.ArrowLeft,
                contentDescription = "Back",
                tint = IrisPrimary,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = "Settings",
                color = IrisText,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = OutfitFontFamily,
            )
        }
    }
}
