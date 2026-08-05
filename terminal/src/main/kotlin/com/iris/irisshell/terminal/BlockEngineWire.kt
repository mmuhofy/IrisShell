package com.iris.irisshell.terminal

import android.util.Log
import com.iris.irisshell.domain.block.BlockEngineState
import com.iris.irisshell.domain.block.BlockRepository
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession

/**
 * Wires the Termux terminal session into the block engine via the
 * transcript snapshot of the emulator screen.
 *
 * Each [onSessionTextChanged] call:
 *  1. Pulls the current transcript.
 *  2. Strips ANSI sequences via [AnsiStripper].
 *  3. Diffs against the previously seen transcript using a rolling
 *     anchor (longest common tail-substring) so output survives
 *     buffer scroll.
 *  4. Detects the shell prompt suffix on the **last** line of the new
 *     text — if present, closes the running block and records the
 *     captured prompt for next time.
 *  5. Echo-suppression: drops the first appended line if it matches
 *     the most recently submitted command.
 *
 * This is a **transcript polling** design — every UI frame, the wire
 * compares what the user can see now versus what we last saw, and
 * pushes the difference into the block repository. See PLAN.md §3.
 */
class BlockEngineWire(
    private val blockRepository: BlockRepository,
) : BlockEngineState {

    private var previousTranscript: String = ""

    @Volatile
    override var lastPrompt: String = DEFAULT_PROMPT
        private set

    private var pendingEcho: String? = null

    fun onSessionTextChanged(session: TerminalSession) {
        val emulator: TerminalEmulator = session.emulator ?: return
        val raw = emulator.getScreen().getTranscriptTextWithoutJoinedLines()
        val current = AnsiStripper.strip(raw)
        Log.d(TAG, "tick: raw.len=${raw.length} stripped.len=${current.length} prevLen=${previousTranscript.length} lastLine='${current.substringAfterLast("\n").take(80)}'")
        if (current.isEmpty()) {
            previousTranscript = ""
            return
        }

        val appended = if (previousTranscript.isEmpty()) {
            current
        } else {
            val anchor = findRollingAnchor(previousTranscript, current)
                ?: return resetAnchor(current)
            val idx = current.lastIndexOf(anchor)
            if (idx < 0) return resetAnchor(current)
            current.substring(idx + anchor.length)
        }
        previousTranscript = current
        if (appended.isEmpty()) return

        val lines = appended.split('\n')
        val nonEmpty = lines
            .map { it.trimEnd('\r') }
            .filter { it.isNotEmpty() || lines.size == 1 }
        if (nonEmpty.isEmpty()) return

        val firstIsEcho = pendingEcho?.let { echo ->
            nonEmpty.first().trimEnd() == echo || nonEmpty.first().trimEnd().endsWith(echo)
        } ?: false
        if (firstIsEcho) pendingEcho = null
        val linesAfterEcho = if (firstIsEcho) nonEmpty.drop(1) else nonEmpty

        if (linesAfterEcho.isEmpty()) return

        val lastLine = linesAfterEcho.last()
        val promptSuffix = PROMPT_SUFFIX_REGEX.find(lastLine)
        if (promptSuffix != null) {
            val promptText = lastLine.substring(0, promptSuffix.range.first).trimEnd()
            val outputLines = if (promptText.isEmpty()) {
                linesAfterEcho.dropLast(1)
            } else {
                linesAfterEcho.toMutableList().apply { set(lastIndex, promptText) }
            }
            if (outputLines.isNotEmpty()) {
                blockRepository.onOutputChunk(outputLines.joinToString("\n"))
            }
            lastPrompt = promptText.ifBlank { DEFAULT_PROMPT }
            pendingEcho = null
            blockRepository.onCommandCompleted(exitCode = 0)
        } else {
            blockRepository.onOutputChunk(linesAfterEcho.joinToString("\n"))
        }
    }

    fun onCommandSubmitted(command: String) {
        pendingEcho = command
    }

    fun reset() {
        previousTranscript = ""
        pendingEcho = null
        blockRepository.clear()
    }

    private fun resetAnchor(current: String) {
        previousTranscript = current
    }

    private fun findRollingAnchor(previous: String, current: String): String? {
        val maxLen = minOf(previous.length, MAX_ANCHOR_BYTES)
        val minLen = minOf(maxLen, MIN_ANCHOR_BYTES)
        var len = maxLen
        while (len >= minLen) {
            val tail = previous.substring(previous.length - len)
            if (current.lastIndexOf(tail) >= 0) return tail
            len--
        }
        return null
    }

    private companion object {
        const val TAG = "BlockEngineWire"
        const val DEFAULT_PROMPT = "muhofy@iris-shell:~/IrisShell$"
        const val MIN_ANCHOR_BYTES = 16
        const val MAX_ANCHOR_BYTES = 8192
        val PROMPT_SUFFIX_REGEX = Regex("""[#$❯➜]\s*$""")
    }
}
