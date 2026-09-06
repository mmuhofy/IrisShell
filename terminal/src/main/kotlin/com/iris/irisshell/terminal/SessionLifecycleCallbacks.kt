package com.iris.irisshell.terminal

import com.termux.terminal.TerminalSession

/**
 * Callback interface allowing [TerminalManager] to notify the data/session
 * layer about terminal session lifecycle events.
 *
 * Inspired by Termux's `TerminalSessionClient` callback pattern
 * (github.com/termux/termux-app, terminal-emulator/.../TerminalSessionClient.kt),
 * where the service-bound client receives `onSessionFinished` and
 * `setTerminalShellPid` events. Iris Shell splits this: the PTY-level
 * events flow through [TerminalSessionClientImpl], and the session-level
 * events (keyed by persistent id) flow through this interface.
 *
 * Ported from: mmuhofy/IrisCode — terminal/TerminalManager.kt
 * Adapted for Iris Shell — com.iris.irisshell
 */
interface SessionLifecycleCallbacks {
    /**
     * Called when a terminal session process exits.
     *
     * @param persistentId The Room session id, or null if the session
     *                     was created without one (e.g. via [TerminalManager.addTab]).
     * @param exitCode     The process exit code. -1 if killed by signal.
     */
    fun onSessionFinished(persistentId: String?, exitCode: Int)

    /**
     * Called when the shell pid is assigned for a session.
     *
     * @param persistentId The Room session id, or null if not id-keyed.
     * @param pid          The OS process id of the shell.
     */
    fun onSessionPidChanged(persistentId: String?, pid: Int)

    /**
     * Called when the last live PTY session exits and [TerminalManager]
     * would have no sessions left. The data layer should ensure at least
     * one session exists (e.g. by creating a default session in Room)
     * so the terminal never goes blank.
     */
    fun onLastSessionExited()
}
