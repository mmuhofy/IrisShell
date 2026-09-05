package com.iris.irisshell.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.design.system.IrisBuild
import com.iris.irisshell.design.system.IrisError
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSuccess
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.design.system.IrisWarning
import com.iris.irisshell.ui.R

// ── Palettes ──────────────────────────────────────────────────────────────────

private val BG_PALETTE = listOf(
    IrisBackground,
    IrisSurface,
    IrisSurfaceVariant,
    Color(0xFF000000), // OLED
)

private val ACCENT_PALETTE = listOf(
    IrisPrimary,
    IrisSuccess,
    IrisBuild,
    IrisError,
    IrisWarning,
)

private val TEXT_PALETTE = listOf(
    Color(0xFFEEEEEE), // IrisText
    Color(0xFFFFFFFF),
    Color(0xFF888888), // IrisTextSecondary
    Color(0xFF666666), // IrisTextMuted
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack    : () -> Unit,
    viewModel : SettingsViewModel = hiltViewModel(),
) {
    val useBlockEngine     by viewModel.useBlockEngine.collectAsStateWithLifecycle()
    val extraKeysBarVisible by viewModel.extraKeysBarVisible.collectAsStateWithLifecycle()
    val fontSizeSp         by viewModel.fontSizeSp.collectAsStateWithLifecycle()
    val terminalBgColor    by viewModel.terminalBgColor.collectAsStateWithLifecycle()
    val accentColor        by viewModel.accentColor.collectAsStateWithLifecycle()
    val terminalTextColor  by viewModel.terminalTextColor.collectAsStateWithLifecycle()

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
                    IconButton(
                        onClick  = onBack,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            painter            = painterResource(R.drawable.lucide_arrow_big_left),
                            contentDescription = "Back",
                            tint               = IrisTextSecondary,
                            modifier           = Modifier.size(20.dp),
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IrisBackground,
                ),
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .widthIn(min = 1.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding      = PaddingValues(top = 4.dp, bottom = 40.dp),
        ) {

            // ── TERMINAL ──────────────────────────────────────────────────────

            item {
                SettingsSectionLabel("Terminal")
            }

            item {
                TerminalModeCard(
                    useBlockEngine = useBlockEngine,
                    onSelect       = viewModel::setUseBlockEngine,
                )
                Spacer(Modifier.height(8.dp))
            }

            item {
                SettingsCategoryCard {
                    SettingsToggleRow(
                        iconRes         = R.drawable.lucide_keyboard,
                        label           = "Extra Keys Bar",
                        description     = "ESC, TAB, CTRL, ALT ve yön tuşları",
                        checked         = extraKeysBarVisible,
                        onCheckedChange = viewModel::setExtraKeysBarVisible,
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            // ── GÖRÜNÜM ───────────────────────────────────────────────────────

            item {
                SettingsSectionLabel("Görünüm")
            }

            item {
                SettingsCategoryCard {

                    FontSizeSliderRow(
                        fontSizeSp   = fontSizeSp,
                        onSizeChange = viewModel::setFontSize,
                    )

                    SettingsDivider()

                    ColorPickerRow(
                        iconRes     = R.drawable.lucide_minimize,
                        label       = "Arkaplan",
                        description = "Terminal zemin rengi",
                        options     = BG_PALETTE,
                        selectedHex = terminalBgColor,
                        onSelect    = viewModel::setTerminalBgColor,
                    )

                    SettingsDivider()

                    ColorPickerRow(
                        iconRes     = R.drawable.lucide_palette,
                        label       = "Vurgu Rengi",
                        description = "Komut istemi ve aktif öğeler",
                        options     = ACCENT_PALETTE,
                        selectedHex = accentColor,
                        onSelect    = viewModel::setAccentColor,
                    )

                    SettingsDivider()

                    ColorPickerRow(
                        iconRes     = R.drawable.lucide_a_large_small,
                        label       = "Metin Rengi",
                        description = "Terminal çıktı metni",
                        options     = TEXT_PALETTE,
                        selectedHex = terminalTextColor,
                        onSelect    = viewModel::setTerminalTextColor,
                    )
                }
                Spacer(Modifier.height(24.dp))
            }

            // ── HAKKINDA ──────────────────────────────────────────────────────

            item {
                SettingsSectionLabel("Hakkında")
            }

            item {
                SettingsCategoryCard {
                    InfoRow(label = "Versiyon", value = "1.0.0")
                    SettingsDivider()
                    InfoRow(label = "Build", value = "Phase 1 — Terminal Core")
                    SettingsDivider()
                    InfoRow(label = "Lisans", value = "MIT")
                }
            }
        }
    }
}
