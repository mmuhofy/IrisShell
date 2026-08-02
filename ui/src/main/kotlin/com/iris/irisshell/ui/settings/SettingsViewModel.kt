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

    val useBlockEngine: StateFlow<Boolean> = settings.useBlockEngine
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setUseBlockEngine(enabled: Boolean) {
        viewModelScope.launch { settings.setUseBlockEngine(enabled) }
    }
}
