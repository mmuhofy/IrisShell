package com.iris.irisshell.terminal

import com.iris.irisshell.domain.block.BlockRepository
import com.iris.irisshell.domain.block.CommandBoundary
import com.iris.irisshell.domain.block.CommandBoundaryDetector
import com.termux.terminal.TerminalEmulator
import com.termux.terminal.TerminalSession

/**
 * Wires the Termux terminal session into the block engine.
 *
 * Triggered from [TerminalManager] every time the session's text changes
 * (`onTextChanged`). On each tick:
 *
 *   1. Pull the last N transcript lines from the emulator's screen buffer.
 *   2. Strip ANSI sequences to get plain text.
 *   3. **Prefix-anchored diff** against the previous snapshot to extract
 *      only new lines (handles buffer scroll without losing alignment).
 *   4. If a prompt boundary is detected at the end of the new chunk,
 *      close the running block as `Success(0)`.
 *   5. Otherwise forward the new chunk to [BlockRepository.onOutputChunk]
 *      to append it to the currently running block.
 *
 * Diff strategy: a pure suffix match fails when the terminal scrolls
 * (oldest lines fall off the `takeLast` window). A pure line-hash match
 * misfires on duplicate lines (very common — many CLIs repeat `""` or
 * the prompt itself across rows). The fix: ignore blank lines for
 * anchoring, then find the **most recent** line from the previous tail
 * that still appears in the current tail. Everything after it (in the
 * original full tail, blanks included) is new.
 *
 * UNTESTED — verify before use.
 */
class BlockEngineWire(
    private val blockRepository: BlockRepository,
) {

    private val boundaryDetector = CommandBoundaryDetector()
    private val tailSize: Int = 64
    private var previousTail: List<String> = emptyList()
    private var lastSeenPrompt: Boolean = false

    /** Called by [TerminalManager] whenever the active session's text changes. */
    fun onSessionTextChanged(session: TerminalSession) {
        val emulator: TerminalEmulator = session.emulator ?: return
        val raw = emulator.getScreen().getTranscriptTextWithoutJoinedLines()
        val currentTail = AnsiStripper.strip(raw).lines().takeLast(tailSize)
        if (currentTail.isEmpty()) return

        val boundary = boundaryDetector.detectPromptReady(currentTail)

        if (previousTail.isEmpty()) {
            previousTail = currentTail
            lastSeenPrompt = boundary is CommandBoundary.PromptReady
            return
        }

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
     * Anchor-based diff with blank-line skipping.
     *
     * Returns the slice of [current] that comes strictly after the last
     * matching non-blank line. Returns an empty list when no anchor is
     * found (defensive fallback — never forward everything).
     */
    private fun computeNewLines(previous: List<String>, current: List<String>): List<String> {
        val prevNonBlank = previous.withIndex().filter { it.value.isNotBlank() }
        if (prevNonBlank.isEmpty()) return emptyList()

        val currNonBlank = current.withIndex().filter { it.value.isNotBlank() }
        if (currNonBlank.isEmpty()) return emptyList()

        // Walk [previous]'s non-blank entries from the end; for each,
        // find its last occurrence in [current]'s non-blank entries. The
        // first hit is our scroll anchor.
        for ((prevIdx, line) in prevNonBlank.reversed()) {
            val matchIdx = currNonBlank.indexOfLast { it.value == line }
            if (matchIdx >= 0) {
                val currOriginalIdx = currNonBlank[matchIdx].index
                // Map back to the original tail (which includes blanks)
                // to preserve visual layout.
                return current.drop(currOriginalIdx + 1).filter { it.isNotBlank() }
            }
            // Unused: suppress compiler warning
            @Suppress("UNUSED_EXPRESSION") prevIdx
        }

        return emptyList()
    }

    /** Reset internal state — call when the active session changes. */
    fun reset() {
        previousTail = emptyList()
        lastSeenPrompt = false
        blockRepository.clear()
    }
}
