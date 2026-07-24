package com.iris.irisshell.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iris.irisshell.domain.terminal.ObserveFirstLaunchUseCase
import com.iris.irisshell.domain.terminal.TriggerBootstrapUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Drives [OnboardingScreen].
 *
 * Decides when to advance the pager vs. skip directly to the bootstrap
 * stepper. Persists [ObserveFirstLaunchUseCase.markCompleted] once the user
 * finishes the wizard.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val firstLaunch: ObserveFirstLaunchUseCase,
    private val trigger: TriggerBootstrapUseCase,
) : ViewModel() {

    val isCompleted: StateFlow<Boolean> = firstLaunch.isCompleted()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun finishOnboarding(thenStartBootstrap: Boolean = true) {
        viewModelScope.launch {
            firstLaunch.markCompleted()
            if (thenStartBootstrap) {
                trigger.start()
            }
        }
    }
}
