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
 *   1. Pull the full transcript from the emulator's screen buffer.
 *   2. Strip ANSI sequences to get plain text.
 *   3. **Substring diff with rolling anchor** — find the longest tail
 *      of [previousTranscript] that exists as a substring in
 *      [current]. Everything after that match point in [current] is new.
 *   4. Strip the echoed command line.
 *   5. If a prompt boundary is detected at the end of the appended
 *      slice, close the running block as `Success(0)`.
 *   6. Otherwise forward the new lines to [BlockRepository.onOutputChunk].
 *
 * Why rolling anchor and not simple longest-common-prefix:
 *
 * The terminal buffer scrolls when output exceeds the screen height.
 * The previous transcript's prefix is no longer present in the new
 * transcript, so a pure prefix diff returns zero overlap and every
 * tick silently drops all output. The rolling anchor searches the
 * previous transcript for **any** substring that still exists in the
 * current one — preferring the longest tail match so we anchor as
 * close to the cursor as possible.
 *
 * UNTESTED — verify before use.
 */
class BlockEngineWire(
    private val blockRepository: BlockRepository,
) {

    private val boundaryDetector = CommandBoundaryDetector()
    private var previousTranscript: String = ""
    private var lastSeenPrompt: Boolean = false

    /** Called by [TerminalManager] whenever the active session's text changes. */
    fun onSessionTextChanged(session: TerminalSession) {
        val emulator: TerminalEmulator = session.emulator ?: return
        val raw = emulator.getScreen().getTranscriptTextWithoutJoinedLines()
        val current = AnsiStripper.strip(raw)
        if (current.isEmpty()) return

        if (previousTranscript.isEmpty()) {
            previousTranscript = current
            val firstBoundary = boundaryDetector.detectPromptReady(current.lines())
            lastSeenPrompt = firstBoundary is CommandBoundary.PromptReady
            return
        }

        // Anchor search: find the longest tail of [previousTranscript]
        // that still appears as a substring of [current]. We constrain
        // the search to tails of length >= MIN_ANCHOR bytes to avoid
        // spurious matches on tiny fragments.
        val anchor = findRollingAnchor(previousTranscript, current)
        if (anchor == null || anchor.length < MIN_ANCHOR_BYTES) {
            // No usable anchor — buffer scrolled past everything we
            // remembered. Re-seed and drop this tick's output (we have
            // no way to know which lines are new).
            previousTranscript = current
            return
        }

        val anchorIdxInCurrent = current.lastIndexOf(anchor)
        if (anchorIdxInCurrent < 0) {
            // Should not happen given the find, but be defensive.
            previousTranscript = current
            return
        }
        val appended = current.substring(anchorIdxInCurrent + anchor.length)
        previousTranscript = current

        if (appended.isEmpty()) return

        val appendedLines = appended.split('\n')

        // Echo handling — the shell echoes the user's typed command at
        // the start of its output. Strip it from the slice before
        // pushing to the repository.
        val echoed = blockRepository.currentCommand()
        val linesAfterEcho = if (echoed != null && appendedLines.isNotEmpty()) {
            val first = appendedLines.first().trimEnd()
            if (first.endsWith(echoed)) appendedLines.drop(1) else appendedLines
        } else {
            appendedLines
        }

        val trimmed = linesAfterEcho
            .map { it.trimEnd() }
            .let { if (it.isNotEmpty() && it.last().isEmpty()) it.dropLast(1) else it }
            .filter { it.isNotBlank() }

        val boundary = boundaryDetector.detectPromptReady(
            linesAfterEcho.map { it.trimEnd() },
        )

        if (trimmed.isNotEmpty() && boundary !is CommandBoundary.PromptReady) {
            blockRepository.onOutputChunk(trimmed.joinToString(separator = "\n"))
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
    }

    /**
     * Returns the longest tail of [previous] that appears as a substring
     * of [current]. Constrains the search to tails whose length is in
     * [[MIN_ANCHOR_BYTES], [MAX_ANCHOR_BYTES]] — too small matches
     * cause false positives; too large matches become expensive and
     * unlikely to survive a scroll.
     *
     * Uses [String.lastIndexOf] which returns the rightmost match —
     * i.e. the anchor closest to the cursor, which is what we want.
     *
     * Returns null if no acceptable match is found.
     */
    private fun findRollingAnchor(previous: String, current: String): String? {
        val maxLen = minOf(previous.length, MAX_ANCHOR_BYTES)
        val minLen = minOf(maxLen, MIN_ANCHOR_BYTES)
        var len = maxLen
        while (len >= minLen) {
            val tail = previous.substring(previous.length - len)
            if (current.lastIndexOf(tail) >= 0) return tail
            len--
            if (len < minLen) break
        }
        return null
    }

    /** Reset internal state — call when the active session changes. */
    fun reset() {
        previousTranscript = ""
        lastSeenPrompt = false
        blockRepository.clear()
    }

    private companion object {
        const val MIN_ANCHOR_BYTES = 16
        const val MAX_ANCHOR_BYTES = 8192
    }
}
