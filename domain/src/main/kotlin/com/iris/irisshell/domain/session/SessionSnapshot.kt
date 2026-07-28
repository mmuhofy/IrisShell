package com.iris.irisshell.domain.session

/**
 * Snapshot of a terminal session, surfaced to the UI.
 *
 * Combines two tiers:
 *  - **persistent metadata** (id, name, createdAt, lastUsedAt, state) — survives
 *    app restarts, stored in Room.
 *  - **live preview** (liveSnapshotLines) — refreshed by the data layer every
 *    ~500ms while the session is open, in-memory only.
 *
 * Live preview is intentionally short (last 50 lines). The full transcript is
 * held by Termux's `TerminalEmulator`/`TerminalBuffer` and only surfaced when
 * the user opens the session, never serialised.
 *
 * State transitions:
 *   Idle → Opening → Running → Closed
 *   Running → Closed (process exit)
 *   Closed → Running (user re-opens)
 */
data class SessionSnapshot(
    val id: String,
    val name: String,
    val state: SessionState,
    val createdAtMs: Long,
    val lastUsedAtMs: Long,
    val liveSnapshotLines: List<String> = emptyList(),
)

enum class SessionState {
    /** App knows about it but the PTY process has never spawned. */
    Idle,

    /** PTY is currently alive. Live snapshot is updated. */
    Running,

    /** PTY exited. lastUsedAtMs reflects last activity. */
    Closed,
}
