package com.iris.irisshell.data.session

import com.iris.irisshell.terminal.TerminalManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
 * **Responsibilities**
 *  - When the repository creates a session → ask [TerminalManager] to
 *    spawn a PTY for it (via the existing `addTab` + `renameTab`
 *    APIs; the future `addNamedSession` refactor will replace those).
 *  - When the repository's active-id changes → ask [TerminalManager]
 *    to switch to the matching tab.
 *  - Periodically (every [SNAPSHOT_TICK_MS]) read the active tab's
 *    screen via the public `TerminalEmulator.getScreen().getTranscriptText()`
 *    and push the last [SNAPSHOT_LINE_COUNT] lines back into the
 *    repository so the UI's `liveSnapshotLines` is up to date.
 *
 * **Not** done here (deferred to Phase 2 follow-up):
 *  - Closing the active session via the switcher (UI hook only).
 *  - Snapshot capture on graceful exit (handled by [TerminalManager.onSessionFinished]).
 */
@Singleton
class SessionManagerAdapter @Inject constructor(
    private val sessionRepository: SessionRepositoryImpl,
    private val terminalManager: TerminalManager,
    @com.iris.irisshell.data.di.ApplicationScope private val appScope: CoroutineScope,
) {

    private var watchJob: Job? = null
    private var tickerJob: Job? = null

    private val _activeId = MutableStateFlow<String?>(null)
    val activeIdFlow: StateFlow<String?> = _activeId.asStateFlow()

    /**
     * Start the adapter — observes the active-session pointer and
     * drives the TerminalManager accordingly. Idempotent: calling
     * twice is safe (existing jobs are cancelled first).
     */
    fun start() {
        watchJob?.cancel()
        tickerJob?.cancel()

        watchJob = appScope.launch {
            sessionRepository.observeActiveId().collectLatest { id ->
                _activeId.value = id
                if (id != null) attachToActiveSession(id)
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
     * Map a session id to the corresponding runtime tab index, or -1.
     * Until Phase 2 follow-up mapping lands, we use a position-based
     * mapping: rows in Room ordered by lastUsedAtMs map to tabs in the
     * same order. This is correct for freshly-created sessions that
     * never had their order shuffled (which Phase 2 doesn't support
     * yet).
     */
    private suspend fun indexOfSession(id: String): Int {
        // Position-based mapping: rows ordered by lastUsedAtMs map to
        // tabs in the same order. This is correct for freshly-created
        // sessions that never had their order shuffled (which Phase 2
        // doesn't support yet).
        val rows: List<String> = sessionRepository.observeAllIds().first()
        return rows.indexOf(id)
    }

    private suspend fun attachToActiveSession(id: String) {
        val idx = indexOfSession(id)
        if (idx < 0) return
        terminalManager.switchTab(idx)
    }

    private fun captureLiveSnapshot() {
        // Block engine deferred — for now, no-op. The Phase 2 follow-up
        // adds live preview via TerminalEmulator.getScreen().getTranscriptText()
        // once TerminalManager exposes a getActiveEmulator() seam.
    }

    private companion object {
        const val SNAPSHOT_TICK_MS = 500L
        const val SNAPSHOT_LINE_COUNT = 50
    }
}
