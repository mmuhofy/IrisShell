package com.iris.irisshell.data.session

import com.iris.irisshell.domain.session.SessionSnapshot
import com.iris.irisshell.domain.session.SessionState
import com.iris.irisshell.terminal.TerminalManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManagerAdapter @Inject constructor(
    private val sessionRepository: SessionRepositoryImpl,
    private val terminalManager: TerminalManager,
    @com.iris.irisshell.data.di.ApplicationScope private val appScope: CoroutineScope,
) {

    private var reconcileJob: Job? = null
    private var activeJob: Job? = null
    private var tickerJob: Job? = null

    private var lastNames: Map<String, String> = emptyMap()
    private var lastIds: Set<String> = emptySet()

    private val _activeId = MutableStateFlow<String?>(null)
    val activeIdFlow: StateFlow<String?> = _activeId.asStateFlow()

    fun start() {
        stop()

        reconcileJob = appScope.launch {
            sessionRepository.observeAll().collectLatest { snapshots ->
                reconcile(snapshots)
            }
        }

        activeJob = appScope.launch {
            sessionRepository.observeActiveId().collectLatest { id ->
                _activeId.value = id
                if (id != null) {
                    withContext(Dispatchers.Main.immediate) {
                        terminalManager.switchSessionById(id)
                    }
                }
            }
        }

        tickerJob = appScope.launch {
            while (true) {
                delay(SNAPSHOT_TICK_MS)
                captureLiveSnapshot()
            }
        }
    }

    fun stop() {
        reconcileJob?.cancel()
        activeJob?.cancel()
        tickerJob?.cancel()
        reconcileJob = null
        activeJob = null
        tickerJob = null
    }

    private suspend fun reconcile(snapshots: List<SessionSnapshot>) {
        val currentIds = snapshots.map { it.id }.toSet()
        val currentNames = snapshots.associate { it.id to it.name }

        withContext(Dispatchers.Main.immediate) {
            val added = currentIds - lastIds
            added.forEach { id ->
                val name = currentNames[id] ?: ""
                terminalManager.addTabWithId(id, name)
                sessionRepository.updateState(id, SessionState.Running)
            }

            lastNames.forEach { (id, oldName) ->
                val newName = currentNames[id]
                if (newName != null && newName != oldName) {
                    val idx = terminalManager.getIndexForId(id)
                    if (idx >= 0) terminalManager.renameTab(idx, newName)
                }
            }

            val removed = lastIds - currentIds
            removed.forEach { id ->
                val idx = terminalManager.getIndexForId(id)
                if (idx >= 0) {
                    terminalManager.closeTab(idx)
                    sessionRepository.updateState(id, SessionState.Closed)
                }
            }
        }

        lastIds = currentIds
        lastNames = currentNames
    }

    private suspend fun captureLiveSnapshot() {
        try {
            val session = terminalManager.currentSession ?: return
            val emulator = session.emulator ?: return
            val screen = emulator.screen ?: return

            val totalRows = screen.rows
            val startRow = maxOf(0, totalRows - 50)
            val lines = (startRow until totalRows).mapNotNull { rowIndex ->
                val row = screen.getLine(rowIndex) ?: return@mapNotNull null
                row.toString().trimEnd()
            }.filter { it.isNotEmpty() }

            if (lines.isNotEmpty()) {
                val id = terminalManager.activePersistentId()
                if (id != null) {
                    sessionRepository.updateLivePreview(id, lines)
                }
            }
        } catch (e: Exception) {
            // Non-critical, ignore
        }
    }

    private companion object {
        const val SNAPSHOT_TICK_MS = 500L
    }
}