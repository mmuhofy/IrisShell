package com.iris.irisshell.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iris.irisshell.domain.settings.AboutInfo
import com.iris.irisshell.domain.settings.PinLockRepository
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
    private val pinLock: PinLockRepository,
) : ViewModel() {

    // ── PIN Lock ────────────────────────────────────────────────────────────────

    val isPinLockEnabled: StateFlow<Boolean> = pinLock.isEnabled
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun setPinLockEnabled(enabled: Boolean) {
        viewModelScope.launch { pinLock.setEnabled(enabled) }
    }

    suspend fun setPin(pin: String) {
        pinLock.setPin(pin)
    }

    suspend fun verifyPin(pin: String): Boolean = pinLock.verify(pin)

    suspend fun clearPin() {
        pinLock.clearPin()
    }


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
        .stateIn(viewModelScope, SharingStarted.Eagerly, "#000000")

    val accentColor: StateFlow<String> = settings.accentColor
        .stateIn(viewModelScope, SharingStarted.Eagerly, "#3B82F6")

    val terminalTextColor: StateFlow<String> = settings.terminalTextColor
        .stateIn(viewModelScope, SharingStarted.Eagerly, "#E8E8E8")

    val prootStartCommand: StateFlow<String> = settings.prootStartCommand
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

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
        normalizeHex(hex)?.let { viewModelScope.launch { settings.setTerminalBgColor(it) } }
    }

    fun setAccentColor(hex: String) {
        normalizeHex(hex)?.let { viewModelScope.launch { settings.setAccentColor(it) } }
    }

    fun setTerminalTextColor(hex: String) {
        normalizeHex(hex)?.let { viewModelScope.launch { settings.setTerminalTextColor(it) } }
    }

    fun setProotStartCommand(command: String) {
        viewModelScope.launch { settings.setProotStartCommand(command) }
    }

    // ── App Info ────────────────────────────────────────────────────────────────

    val aboutInfo: StateFlow<AboutInfo?> = settings.appInfo
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
}
