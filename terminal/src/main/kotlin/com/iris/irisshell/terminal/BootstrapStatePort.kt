package com.iris.irisshell.terminal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Internal bridge that lets `:data` observe bootstrap state + logs without
 * `ui/` ever importing `terminal/UbuntuSetupState` or `UbuntuBootstrap`.
 *
 * The `:data` layer calls [runBootstrap] to drive the pipeline. The port
 * forwards each state transition and each emitted log line through typed
 * flows. The `:data` repository translates these into the `:domain` shape
 * (`BootstrapProgress` + log `Flow<String>`).
 *
 * Contravariant design: the bootstrap instance is owned by `MainActivity`'s
 * DI graph (Hilt); the port is the thin facade over it.
 */
class BootstrapStatePort(private val bootstrap: UbuntuBootstrap) {

    private val _state = MutableStateFlow<UbuntuSetupState>(UbuntuSetupState.Idle)
    val state: StateFlow<UbuntuSetupState> = _state.asStateFlow()

    private val _logs = MutableSharedFlow<String>(extraBufferCapacity = 256)
    val logs: SharedFlow<String> = _logs.asSharedFlow()

    /**
     * Drives the bootstrap pipeline. Safe to call multiple times — each call
     * resets `_state` to `Idle` before running.
     */
    fun runBootstrap(
        scope: CoroutineScope,
        installPackages: Boolean = true,
        optimize: Boolean = true,
    ) {
        scope.launch {
            _state.value = UbuntuSetupState.Idle
            bootstrap.install(
                installPackages = installPackages,
                optimize = optimize,
                onState = { _state.value = it },
                onLog = { _logs.tryEmit(it) },
            )
        }
    }
}
