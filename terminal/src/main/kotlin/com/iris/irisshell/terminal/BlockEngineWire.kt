package com.iris.irisshell.terminal

import com.iris.irisshell.domain.block.BlockRepository
import com.iris.irisshell.domain.terminal.ByteStreamEvent
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession

/**
 * Wires the Termux terminal session into the block engine via the raw
 * PTY byte stream.
 *
 * Subscribes to [TerminalEmulator.byteListener] when the active session
 * is known, so every PTY byte is observed **before** UTF-8 decoding and
 * screen rendering. Bytes feed a [ByteStreamParser] which emits
 * [ByteStreamEvent.OutputLine]s.
 *
 * The wire then:
 *
 *  1. Strips the user command's echo from the first line.
 *  2. Detects the shell prompt suffix (`$ `, `# `, `❯ `, `➜ `) on the
 *     final accumulated line(s) of the running block.
 *  3. When a prompt suffix is seen, closes the running block as
 *     `Success(0)` and records the captured prompt text.
 *  4. Pushes output lines to [BlockRepository.onOutputChunk] until the
 *     prompt suffix arrives.
 *
 * Designed to be set up exactly once per active session in
 * [TerminalManager]. Switching sessions detaches and re-attaches.
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

    fun attach(session: TerminalSession) {
        if (subscribedSession === session) return
        detach()
        subscribedSession = session
        session.emulator?.byteListener = { byte -> onByte(byte) }
        parser.reset()
    }

    fun detach() {
        subscribedSession?.emulator?.byteListener = null
        subscribedSession = null
    }

    fun onCommandSubmitted(command: String) {
        pendingEcho = command
    }

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
                val promptSuffix = PROMPT_SUFFIX_REGEX.find(text)
                if (promptSuffix != null) {
                    val promptText = text.substring(0, promptSuffix.range.first)
                    if (promptText.isNotEmpty()) {
                        blockRepository.onOutputChunk(promptText)
                    }
                    lastPrompt = promptText.ifBlank { DEFAULT_PROMPT }
                    pendingEcho = null
                    blockRepository.onCommandCompleted(exitCode = 0)
                } else {
                    blockRepository.onOutputChunk(text)
                }
            }
            ByteStreamEvent.TuiEntered -> {
                blockRepository.onCommandCancelled()
            }
            ByteStreamEvent.TuiExited -> {
                // Next prompt will close the block properly.
            }
            ByteStreamEvent.PromptReady -> {
                // No-op: prompt detection happens inside OutputLine
                // handling so we can capture the prompt text from the
                // same line buffer flush.
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
        // Common shell prompt terminators: `$`, `#`, `❯`, `➜` — followed
        // by optional trailing space.
        val PROMPT_SUFFIX_REGEX = Regex("""[#$❯➜]\s*$""")
    }
}
