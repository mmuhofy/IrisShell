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
 *   3. **Anchor-diff** against the previous snapshot to extract only new
 *      lines (handles buffer scroll without losing the prefix).
 *   4. If a prompt boundary is detected at the end of the new chunk,
 *      close the running block as `Success(0)` (exit-code injection is
 *      a v2 feature — see `docs/block-engine/PLAN.md` §7).
 *   5. Otherwise forward the new chunk to [BlockRepository.onOutputChunk]
 *      to append it to the currently running block.
 *
 * Diff strategy: pure suffix matching fails when the buffer scrolls
 * (the previous tail's oldest lines drop off). Instead, the previous
 * tail is searched backwards for the **most recent line that still
 * exists** in the current tail; everything after that anchor is new.
 *
 * UNTESTED — verify before use.
 */
class BlockEngineWire(
    private val blockRepository: BlockRepository,
) {

    private val boundaryDetector = CommandBoundaryDetector()
    private val tailSize: Int = 64
    private var previousTail: List<String> = emptyList()
    private var previousTranscriptLength: Int = 0
    private var lastSeenPrompt: Boolean = false

    /** Called by [TerminalManager] whenever the active session's text changes. */
    fun onSessionTextChanged(session: TerminalSession) {
        val emulator: TerminalEmulator = session.emulator ?: return
        val raw = emulator.getScreen().getTranscriptTextWithoutJoinedLines()
        val currentTail = AnsiStripper.strip(raw).lines().takeLast(tailSize)
        if (currentTail.isEmpty()) return

        val boundary = boundaryDetector.detectPromptReady(currentTail)

        if (previousTranscriptLength == 0) {
            previousTranscriptLength = raw.length
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
        previousTranscriptLength = raw.length
    }

    /**
     * Anchor-based diff: find the most recent line from [previous] that
     * still appears in [current], then return everything after it.
     *
     * Returns an empty list if no anchor exists (nothing changed in a
     * way we can match — defensive fallback).
     */
    private fun computeNewLines(previous: List<String>, current: List<String>): List<String> {
        if (previous.isEmpty()) return emptyList()
        val currentIndexByLine = HashMap<String, Int>(current.size * 2)
        current.forEachIndexed { idx, line -> currentIndexByLine.putIfAbsent(line, idx) }

        for (i in previous.indices.reversed()) {
            val anchorIndex = currentIndexByLine[previous[i]]
            if (anchorIndex != null) {
                return current.drop(anchorIndex + 1)
            }
        }

        return emptyList()
    }

    /** Reset internal state — call when the active session changes. */
    fun reset() {
        previousTail = emptyList()
        previousTranscriptLength = 0
        lastSeenPrompt = false
        blockRepository.clear()
    }
}
