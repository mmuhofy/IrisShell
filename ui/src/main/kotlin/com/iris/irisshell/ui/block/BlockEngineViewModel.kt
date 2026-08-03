package com.iris.irisshell.ui.block

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iris.irisshell.domain.block.Block
import com.iris.irisshell.domain.block.BlockRepository
import com.iris.irisshell.domain.block.NetworkMetricsCollector
import com.iris.irisshell.domain.terminal.SubmitBlockCommandUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Block Mode terminal view.
 *
 * Exposes the block list and currently-running block to the UI, and
 * drives a 500ms network-refresh loop that pushes live RX/TX totals
 * into the running block via [BlockRepository.updateRunningCounters].
 *
 * User actions (run, cancel, toggle collapse) are forwarded to the
 * repository — this VM is a thin observer.
 *
 * UNTESTED — verify before use.
 */
@HiltViewModel
class BlockEngineViewModel @Inject constructor(
    private val blockRepository: BlockRepository,
    private val trafficStats: NetworkMetricsCollector,
    private val submitCommand: SubmitBlockCommandUseCase,
) : ViewModel() {

    val blocks: StateFlow<List<Block>> = blockRepository.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val runningBlock: StateFlow<Block?> = blockRepository.observeRunningBlock()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _networkEnabled = MutableStateFlow(false)
    val networkEnabled: StateFlow<Boolean> = _networkEnabled.asStateFlow()

    private var networkJob: Job? = null

    init {
        blockRepository.clear()
        startNetworkTicker()
    }

    private fun startNetworkTicker() {
        if (networkJob?.isActive == true) return
        networkJob = viewModelScope.launch {
            _networkEnabled.value = true
            while (true) {
                tickRunningBlock()
                delay(NETWORK_TICK_MS)
            }
        }
    }

    private fun tickRunningBlock() {
        val running = runningBlock.value ?: return
        val (rx, tx) = trafficStats.snapshotTotals()
        blockRepository.updateRunningCounters(
            blockId = running.id,
            currentRxBytes = rx,
            currentTxBytes = tx,
        )
    }

    fun onCommandSubmitted(prompt: String, command: String) {
        val (rx, tx) = trafficStats.snapshotTotals()
        blockRepository.onCommandSubmitted(
            prompt = prompt,
            command = command,
            startRxBytes = rx,
            startTxBytes = tx,
        )
        viewModelScope.launch { submitCommand.submit(command) }
    }

    fun onCommandCompleted(exitCode: Int) {
        blockRepository.onCommandCompleted(exitCode)
    }

    fun onCommandCancelled() {
        blockRepository.onCommandCancelled()
    }

    fun onToggleCollapsed(blockId: String) {
        val current = blocks.value.firstOrNull { it.id == blockId } ?: return
        blockRepository.setCollapsed(blockId, !current.isCollapsed)
    }

    fun onOutputChunk(chunk: String) {
        blockRepository.onOutputChunk(chunk)
    }

    override fun onCleared() {
        super.onCleared()
        networkJob?.cancel()
    }

    private companion object {
        const val NETWORK_TICK_MS = 500L
    }
}
