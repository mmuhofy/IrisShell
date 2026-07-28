package com.iris.irisshell.data.session

import com.iris.irisshell.domain.session.SessionSnapshot
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

/**
 * Bridges the persistent session metadata in [SessionRepositoryImpl]
 * with the runtime PTY session list owned by [TerminalManager].
 *
 * Lives in `:data` because it composes two `:data`-owned dependencies
 * (the repository impl and the application scope). The `:ui` layer
 * should never touch this; it asks the repository directly.
 *
 * **Reconciliation strategy** — diff-driven:
 *  - On every emission of [SessionRepositoryImpl.observeAll], compute
 *    (added, removed, renamed) against the in-memory TerminalManager
 *    id map.
 *  - For each *added* id → call [TerminalManager.addTabWithId] so the
 *    PTY is spawned.
 *  - For each *removed* id → call [TerminalManager.closeTab] on the
 *    matching positional index (id-keyed lookup goes through
 *    [TerminalManager.getIndexForId]).
 *  - For each *renamed* id → call [TerminalManager.renameTab] on its
 *    positional index.
 *  - Active-id changes → [TerminalManager.switchSessionById].
 *  - Snapshot ticker is a separate loop, currently a no-op until the
 *    TerminalManager exposes `getActiveEmulator()`.
 */
@Singleton
class SessionManagerAdapter @Inject constructor(
    private val sessionRepository: SessionRepositoryImpl,
    private val terminalManager: TerminalManager,
    @com.iris.irisshell.data.di.ApplicationScope private val appScope: CoroutineScope,
) {

    private var reconcileJob: Job? = null
    private var activeJob: Job? = null
    private var tickerJob: Job? = null

    /** Last-seen map of session id → display name. Used for rename diff. */
    private var lastNames: Map<String, String> = emptyMap()

    /** Last-seen set of session ids. Used for add/remove diff. */
    private var lastIds: Set<String> = emptySet()

    private val _activeId = MutableStateFlow<String?>(null)
    val activeIdFlow: StateFlow<String?> = _activeId.asStateFlow()

    /**
     * Start the adapter. Idempotent — calling twice cancels existing
     * jobs first. Called from `IrisApplication.onCreate()` (Phase 2
     * follow-up) so the adapter boots up alongside the Hilt graph.
     */
    fun start() {
        reconcileJob?.cancel()
        activeJob?.cancel()
        tickerJob?.cancel()

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

    /**
     * Diff the latest snapshot list against the last-seen state and
     * tell TerminalManager what to do. Order of operations matters:
     * add → rename → remove. (Removing before renaming would lose
     * the positional mapping we need to rename.)
     */
    /**
     * Reconcile the Room snapshot list against TerminalManager. All
     * TerminalManager calls go through `withContext(Dispatchers.Main)`
     * because `addTabWithId` internally calls `terminalViewRef.attachSession`,
     * an Android View API that requires the main thread.
     */
    private suspend fun reconcile(snapshots: List<SessionSnapshot>) {
        val currentIds = snapshots.map { it.id }.toSet()
        val currentNames = snapshots.associate { it.id to it.name }

        withContext(Dispatchers.Main.immediate) {
            // 1. Adds — spawn PTYs for ids that weren't there before.
            val added = currentIds - lastIds
            added.forEach { id ->
                val name = currentNames[id] ?: ""
                terminalManager.addTabWithId(id, name)
            }

            // 2. Renames — push new display names for known ids.
            lastNames.forEach { (id, oldName) ->
                val newName = currentNames[id]
                if (newName != null && newName != oldName) {
                    val idx = terminalManager.getIndexForId(id)
                    if (idx >= 0) terminalManager.renameTab(idx, newName)
                }
            }

            // 3. Removes — kill the PTYs for ids that disappeared.
            val removed = lastIds - currentIds
            removed.forEach { id ->
                val idx = terminalManager.getIndexForId(id)
                if (idx >= 0) terminalManager.closeTab(idx)
            }
        }

        lastIds = currentIds
        lastNames = currentNames
    }

    private fun captureLiveSnapshot() {
        // Snapshot capture requires TerminalManager to expose its
        // active TerminalEmulator. Deferred to a Phase 2 follow-up.
    }

    private companion object {
        const val SNAPSHOT_TICK_MS = 500L
    }
}

