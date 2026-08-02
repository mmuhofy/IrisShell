package com.iris.irisshell.terminal

import com.iris.irisshell.domain.block.BlockRepository
import com.iris.irisshell.domain.block.CommandBoundary
import com.iris.irisshell.domain.block.CommandBoundaryDetector
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wires the Termux terminal session into the block engine.
 *
 * Triggered from [TerminalManager] every time the session's text changes
 * (`onTextChanged`). On each tick:
 *
 *   1. Pull the last N transcript lines from the emulator's screen buffer.
 *   2. Strip ANSI sequences to get plain text.
 *   3. **Diff** against the previous snapshot to extract only new lines.
 *   4. If a prompt boundary is detected at the end of the new chunk,
 *      close the running block as `Success(0)` (exit-code injection is
 *      a v2 feature — see `docs/block-engine/PLAN.md` §7).
 *   5. Otherwise forward the new chunk to [BlockRepository.onOutputChunk]
 *      to append it to the currently running block.
 *
 * Diff strategy: each call remembers the previously-seen tail lines.
 * New lines are those beyond the longest common prefix between the
 * previous tail and the current transcript. Cheap, allocation-light,
 * handles screen reflow without state explosion.
 *
 * UNTESTED — verify before use.
 */
@Singleton
class BlockEngineWire @Inject constructor(
    private val blockRepository: BlockRepository,
) {

    private val boundaryDetector = CommandBoundaryDetector()
    private val tailSize: Int = 32
    private var previousTail: List<String> = emptyList()
    private var lastSeenPrompt: Boolean = false

    /** Called by [TerminalManager] whenever the active session's text changes. */
    fun onSessionTextChanged(session: TerminalSession) {
        val emulator: TerminalEmulator = session.emulator ?: return
        val raw = emulator.screen.transcriptTextWithoutJoinedLines
        val currentTail = AnsiStripper.strip(raw).lines().takeLast(tailSize)
        if (currentTail.isEmpty()) return

        val boundary = boundaryDetector.detectPromptReady(currentTail)

        // First ever sample: seed previous tail, do not forward anything.
        if (previousTail.isEmpty()) {
            previousTail = currentTail
            lastSeenPrompt = boundary is CommandBoundary.PromptReady
            return
        }

        // Compute new lines beyond the previous tail.
        val newLines = computeNewLines(previousTail, currentTail)
        if (newLines.isNotEmpty()) {
            blockRepository.onOutputChunk(newLines.joinToString(separator = "\n"))
        }

        when (boundary) {
            is CommandBoundary.PromptReady -> {
                if (!lastSeenPrompt) {
                    blockRepository.onCommandCompleted(exitCode = 0)
                }
                lastSeenPrompt = true
            }
            else -> lastSeenPrompt = false
        }

        previousTail = currentTail
    }

    /**
     * Returns lines from [current] that did not appear in [previous].
     * Uses the longest common suffix prefix match: if the previous tail
     * is `["a","b","c","d"]` and current is `["a","b","c","d","e","f"]`,
     * the result is `["e","f"]`.
     */
    private fun computeNewLines(previous: List<String>, current: List<String>): List<String> {
        if (previous.isEmpty()) return current
        if (current.size <= previous.size && current == previous.takeLast(current.size)) {
            return emptyList()
        }
        val matchCount = commonSuffixSize(previous, current)
        return current.drop(matchCount)
    }

    private fun commonSuffixSize(a: List<String>, b: List<String>): Int {
        var count = 0
        var i = a.size - 1
        var j = b.size - 1
        while (i >= 0 && j >= 0 && a[i] == b[j]) {
            count++
            i--
            j--
        }
        return count
    }

    /** Reset internal state — call when the active session changes. */
    fun reset() {
        previousTail = emptyList()
        lastSeenPrompt = false
        blockRepository.clear()
    }
}
