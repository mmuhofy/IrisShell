package com.iris.irisshell.data.terminal

import android.util.Log
import com.iris.irisshell.domain.terminal.SubmitBlockCommandUseCase
import com.iris.irisshell.terminal.TerminalManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Writes a command string into the currently active terminal session.
 *
 * Uses `TerminalEmulator.paste(text + "\r")` so the emulator handles
 * bracketed paste mode and DEC private modes correctly. CR is used as
 * the line terminator because PTYs expect CR (Enter key) to dispatch a
 * command.
 *
 * No-op if there is no active session.
 *
 * UNTESTED — verify before use.
 */
@Singleton
class SubmitBlockCommandUseCaseImpl @Inject constructor(
    private val terminalManager: TerminalManager,
) : SubmitBlockCommandUseCase {

    override suspend fun submit(command: String) {
        Log.d("SubmitBlockCommand", "submit called: '$command'")
        if (command.isBlank()) {
            Log.d("SubmitBlockCommand", "blank, skipping")
            return
        }
        val session = terminalManager.currentSession
        if (session == null) {
            Log.w("SubmitBlockCommand", "no current session")
            return
        }
        val emulator = session.emulator
        if (emulator == null) {
            Log.w("SubmitBlockCommand", "session has no emulator")
            return
        }
        Log.d("SubmitBlockCommand", "pasting ${command.length + 1} bytes to PTY")
        emulator.paste(command + "\r")
        Log.d("SubmitBlockCommand", "paste returned")
    }
}
