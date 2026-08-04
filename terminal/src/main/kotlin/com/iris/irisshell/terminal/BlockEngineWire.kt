package com.iris.irisshell.terminal

import com.iris.irisshell.domain.block.BlockRepository
import com.iris.irisshell.domain.terminal.ByteStreamEvent
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession

/**
 * Wires the Termux terminal session into the block engine via the
 * raw PTY byte stream.
 *
 * Subscribes to [TerminalEmulator.byteListener] when the active
 * session is known, so we receive every PTY byte **before** UTF-8
 * decoding and screen rendering. Each byte is fed through a
 * [ByteStreamParser] which produces [ByteStreamEvent]s.
 *
 * The wire then maps events to repository calls:
 *
 *  - [ByteStreamEvent.OutputLine] — appended to the running block
 *    unless the line matches the user's just-submitted command
 *    (echo suppression).
 *  - [ByteStreamEvent.PromptReady] — closes the running block as
 *    `Success(0)`. Captures the prompt text for future use.
 *  - [ByteStreamEvent.TuiEntered] / [ByteStreamEvent.TuiExited] —
 *    bookkeeping only in v1; wire resets its line buffer on TUI exit
 *    but does not close the block (the next prompt will).
 *
 * Designed to be set up exactly once per active session in
 * [TerminalManager] — switching sessions detaches and re-attaches.
 */
class BlockEngineWire(
    private val blockRepository: BlockRepository,
) {

    private val parser = ByteStreamParser()

    /** Session currently subscribed for byte events. */
    private var subscribedSession: TerminalSession? = null

    /** Last prompt text we saw — used as the `prompt` of new blocks. */
    @Volatile var lastPrompt: String = DEFAULT_PROMPT
        private set

    /** Last command the user submitted — used to suppress shell echo. */
    private var pendingEcho: String? = null

    /**
     * Called by [TerminalManager] whenever the active session changes.
     * Detaches from the previous session's emulator and attaches to the
     * new one.
     */
    fun attach(session: TerminalSession) {
        if (subscribedSession === session) return
        detach()
        subscribedSession = session
        session.emulator?.byteListener = { byte -> onByte(byte) }
        parser.reset()
    }

    /** Detach from any currently subscribed session. */
    fun detach() {
        subscribedSession?.emulator?.byteListener = null
        subscribedSession = null
    }

    /**
     * Notify the wire that the user just submitted a command. The wire
     * uses this to suppress the next line that matches the command
     * verbatim (the shell echoes typed input).
     */
    fun onCommandSubmitted(command: String) {
        pendingEcho = command
    }

    /** Reset all internal state — call when the active session changes. */
    fun reset() {
        detach()
        parser.reset()
        pendingEcho = null
        blockRepository.clear()
    }

    private fun onByte(byte: Byte) {
        parser.feed(byte)
        val events = parser.drainEvents()
        for (event in events) {
            handleEvent(event)
        }
    }

    private fun handleEvent(event: ByteStreamEvent) {
        when (event) {
            is ByteStreamEvent.OutputLine -> {
                val text = event.text
                if (suppressEcho(text)) return
                if (text.isEmpty()) return
                blockRepository.onOutputChunk(text)
            }
            is ByteStreamEvent.PromptReady -> {
                lastPrompt = event.text.ifBlank { DEFAULT_PROMPT }
                pendingEcho = null
                blockRepository.onCommandCompleted(exitCode = 0)
            }
            ByteStreamEvent.TuiEntered -> {
                // TUI alternates screen — discard buffered output so we
                // don't flood the block with vim's screen contents.
                blockRepository.onCommandCancelled(reason = "TUI entered")
            }
            ByteStreamEvent.TuiExited -> {
                // Next prompt will close the block properly.
            }
        }
    }

    private fun suppressEcho(text: String): Boolean {
        val echo = pendingEcho ?: return false
        val stripped = text.trimEnd()
        if (stripped == echo || stripped.endsWith(echo)) {
            pendingEcho = null
            return true
        }
        return false
    }

    private companion object {
        const val DEFAULT_PROMPT = "muhofy@iris-shell:~/IrisShell$"
    }
}
