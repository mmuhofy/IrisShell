package com.iris.irisshell.ui.setup

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iris.irisshell.domain.terminal.ObserveFirstLaunchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Drives [OnboardingScreen].
 *
 * Persists [ObserveFirstLaunchUseCase.markCompleted] once the user finishes
 * the wizard. The bootstrap is kicked off by [com.iris.irisshell.MainActivity.RootScreen]
 * via its own [com.iris.irisshell.domain.terminal.TriggerBootstrapUseCase]
 * injection, so this VM is intentionally NOT responsible for starting it
 * (avoids double-trigger).
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val firstLaunch: ObserveFirstLaunchUseCase,
) : ViewModel() {

    val isCompleted: StateFlow<Boolean> = firstLaunch.isCompleted()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    suspend fun finishOnboarding() {
        try {
            firstLaunch.markCompleted()
        } catch (t: Throwable) {
            Log.e(TAG, "finishOnboarding: markCompleted failed", t)
            throw t
        }
    }

    private companion object {
        const val TAG = "OnboardingVM"
    }
}
