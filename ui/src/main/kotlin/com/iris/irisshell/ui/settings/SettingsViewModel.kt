package com.iris.irisshell.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iris.irisshell.domain.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settings: SettingsRepository,
) : ViewModel() {

    // ── Terminal mode & input ─────────────────────────────────────────────────

    val useBlockEngine: StateFlow<Boolean> = settings.useBlockEngine
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val extraKeysBarVisible: StateFlow<Boolean> = settings.extraKeysBarVisible
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // ── Font size ─────────────────────────────────────────────────────────────

    val fontSizeSp: StateFlow<Int> = settings.fontSizeSp
        .stateIn(viewModelScope, SharingStarted.Eagerly, 14)

    // ── Colors ────────────────────────────────────────────────────────────────

    val terminalBgColor: StateFlow<String> = settings.terminalBgColor
        .stateIn(viewModelScope, SharingStarted.Eagerly, "#0C0C0C")

    val accentColor: StateFlow<String> = settings.accentColor
        .stateIn(viewModelScope, SharingStarted.Eagerly, "#E8C547")

    val terminalTextColor: StateFlow<String> = settings.terminalTextColor
        .stateIn(viewModelScope, SharingStarted.Eagerly, "#EEEEEE")

    // ── Setters ───────────────────────────────────────────────────────────────

    fun setUseBlockEngine(enabled: Boolean) {
        viewModelScope.launch { settings.setUseBlockEngine(enabled) }
    }

    fun setExtraKeysBarVisible(visible: Boolean) {
        viewModelScope.launch { settings.setExtraKeysBarVisible(visible) }
    }

    fun setFontSize(size: Int) {
        viewModelScope.launch { settings.setFontSize(size) }
    }

    fun setTerminalBgColor(hex: String) {
        viewModelScope.launch { settings.setTerminalBgColor(hex) }
    }

    fun setAccentColor(hex: String) {
        viewModelScope.launch { settings.setAccentColor(hex) }
    }

    fun setTerminalTextColor(hex: String) {
        viewModelScope.launch { settings.setTerminalTextColor(hex) }
    }
}
