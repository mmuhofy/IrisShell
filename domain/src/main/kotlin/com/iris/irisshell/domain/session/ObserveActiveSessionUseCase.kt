package com.iris.irisshell.domain.session

import kotlinx.coroutines.flow.Flow

/**
 * Use case exposing the currently-active session id and its snapshot.
 *
 * Active = "the TerminalScreen is rendering this session's PTY right now".
 * Exactly one session is active at any moment. Closing the active
 * session falls back to the most-recently-used one, or to nothing if the
 * repository is empty (in which case the UI should auto-create a new
 * one).
 *
 * Inspired by mmuhofy/IrisCode — TerminalScreen.activeSessionId flow.
 */
interface ObserveActiveSessionUseCase {
    /** Current active session id, or null if the repository is empty. */
    fun activeId(): Flow<String?>

    /** Hot stream of the currently-active [SessionSnapshot], or null. */
    fun activeSnapshot(): Flow<SessionSnapshot?>

    /** Switch the active session. Triggers PTY attach on the data side. */
    suspend fun setActive(id: String)

    /** Convenience: create a new session and immediately activate it. */
    suspend fun createAndActivate(name: String): String
}
