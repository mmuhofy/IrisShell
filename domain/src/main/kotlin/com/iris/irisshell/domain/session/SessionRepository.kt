package com.iris.irisshell.domain.session

import kotlinx.coroutines.flow.Flow

/**
 * Repository facade for terminal session metadata + live previews.
 *
 * The UI layer consumes this interface only; the implementation lives in
 * `:data` and bridges Room (persistent metadata) + the in-memory snapshot
 * ticker (live preview).
 *
 * Inspired by mmuhofy/IrisCode — but the design was revised for the
 * modal-switcher (no Home screen) flow chosen on 2026-07-28.
 */
interface SessionRepository {

    /** Hot stream of every session, sorted by [SessionSnapshot.lastUsedAtMs] desc. */
    fun observeAll(): Flow<List<SessionSnapshot>>

    /** Hot stream of a single session, or `null` once removed. */
    fun observe(id: String): Flow<SessionSnapshot?>

    /**
     * Create a new session with the given display name. Returns the new
     * session id. The PTY process is spawned by the data layer; this
     * call returns as soon as the row is persisted (process spawn is
     * fire-and-forget).
     */
    suspend fun create(name: String): String

    /** Rename an existing session. */
    suspend fun rename(id: String, newName: String)

    /** Drop a session entirely. Closes its PTY if open. */
    suspend fun delete(id: String)

    /** Mark the session as the most-recently-used (updates lastUsedAtMs). */
    suspend fun touch(id: String)
}
