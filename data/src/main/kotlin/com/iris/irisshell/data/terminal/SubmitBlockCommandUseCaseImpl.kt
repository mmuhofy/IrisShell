package com.iris.irisshell.data.terminal

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
        if (command.isBlank()) return
        val session = terminalManager.currentSession ?: return
        val emulator = session.emulator ?: return
        emulator.paste(command + "\r")
    }
}
