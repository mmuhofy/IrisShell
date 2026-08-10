package com.iris.irisshell.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisOutline
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val useBlockEngine by viewModel.useBlockEngine.collectAsStateWithLifecycle()
    val extraKeysBarVisible by viewModel.extraKeysBarVisible.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = IrisBackground,
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = IrisText) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = IrisTextSecondary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IrisBackground,
                    titleContentColor = IrisText,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            SectionHeader("Terminal")
            SettingsToggleRow(
                title = "Block Mode",
                subtitle = "Render each command as a HUD-style card with exit code, duration, and network metrics.",
                checked = useBlockEngine,
                onCheckedChange = viewModel::setUseBlockEngine,
            )
            SettingsToggleRow(
                title = "Extra Keys Bar",
                subtitle = "Show an on-screen bar above the keyboard with ESC, TAB, CTRL, ALT, and arrow keys.",
                checked = extraKeysBarVisible,
                onCheckedChange = viewModel::setExtraKeysBarVisible,
            )
            HorizontalDivider(color = IrisOutline, modifier = Modifier.padding(vertical = 8.dp))
            SectionHeader("About")
            InfoRow(label = "Version", value = "1.0.0")
            InfoRow(label = "Build", value = "Phase 1 — Terminal Core")
        }
    }
}
