package com.iris.irisshell.domain.terminal

import kotlinx.coroutines.flow.Flow

/**
 * Use case that triggers / re-triggers the bootstrap pipeline.
 *
 * The UI calls this from the Retry / Re-download / Reset buttons on the recovery
 * screen, and from the onboarding wizard's final "Continue" button.
 */
interface TriggerBootstrapUseCase {
    val state: State
    fun start()
    fun retry()
    fun reDownloadRootfs()
    fun resetEverything()

    enum class State { NotStarted, Running }
    val stateFlow: Flow<State>
}
