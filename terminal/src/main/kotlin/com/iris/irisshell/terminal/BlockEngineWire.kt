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
 *   3. **Substring diff** against the previous transcript to extract
 *      only the appended text (handles echo, scroll, prompt changes).
 *   4. Strip the echoed command line (the shell echoes typed chars as
 *      they arrive — they belong on the input row, not in the output).
 *   5. If a prompt boundary is detected at the end of the appended text,
 *      close the running block as `Success(0)`.
 *   6. Otherwise forward the new lines to [BlockRepository.onOutputChunk].
 *
 * Diff strategy: line-based anchor diff fails when the echoed command
 * line concatenates with the previous prompt (e.g.
 * `muhofy@iris-shell:~$ ls`) because the anchor line is no longer the
 * prefix of any single current line. Substring-based diff handles this
 * correctly — find the longest common prefix between the previous
 * transcript's tail and the current transcript, then forward what
 * comes after.
 *
 * UNTESTED — verify before use.
 */
class BlockEngineWire(
    private val blockRepository: BlockRepository,
) {

    private val boundaryDetector = CommandBoundaryDetector()
    private val previousTranscript: StringBuilder = StringBuilder()
    private var initialized: Boolean = false
    private var lastSeenPrompt: Boolean = false

    /** Called by [TerminalManager] whenever the active session's text changes. */
    fun onSessionTextChanged(session: TerminalSession) {
        val emulator: TerminalEmulator = session.emulator ?: return
        val raw = emulator.getScreen().getTranscriptTextWithoutJoinedLines()
        val current = AnsiStripper.strip(raw)
        if (current.isEmpty()) return

        if (!initialized) {
            previousTranscript.setLength(0)
            previousTranscript.append(current)
            initialized = true
            val firstBoundary = boundaryDetector.detectPromptReady(current.lines())
            lastSeenPrompt = firstBoundary is CommandBoundary.PromptReady
            return
        }

        // Longest common prefix between previous transcript (tail) and
        // current transcript. We anchor on the previous transcript's
        // tail of up to ANCHOR_WINDOW bytes (4 KB) — enough to span a
        // typical prompt + a line or two of context, while keeping the
        // comparison bounded.
        val anchorWindow = previousTranscript.length.coerceAtMost(ANCHOR_WINDOW_BYTES)
        val anchor = previousTranscript.substring(previousTranscript.length - anchorWindow)
        val overlapLen = commonPrefixLength(anchor, current)
        if (overlapLen <= 0) {
            // No overlap — buffer fully refreshed (e.g. terminal reset).
            // Drop previous state and reseed.
            previousTranscript.setLength(0)
            previousTranscript.append(current)
            return
        }

        val appended = current.substring(overlapLen)
        // Append the new tail to the running transcript.
        previousTranscript.append(appended)

        // Trim transcript to a reasonable size so it does not grow
        // unbounded across long sessions.
        if (previousTranscript.length > MAX_TRANSCRIPT_BYTES) {
            val drop = previousTranscript.length - MAX_TRANSCRIPT_BYTES
            previousTranscript.delete(0, drop)
        }

        if (appended.isEmpty()) return

        val appendedLines = appended.split('\n')
        // Echoed command handling: if the first appended line ends with
        // the running block's command (the user just typed it), drop it
        // — it belongs on the input row, not in the output. The block
        // repository knows the current command; we look it up.
        val echoed = blockRepository.currentCommand()
        val outputLines = appendedLines
            .let { lines ->
                if (echoed != null && lines.isNotEmpty()) {
                    val first = lines.first().trimEnd()
                    if (first.endsWith(echoed)) lines.drop(1) else lines
                } else {
                    lines
                }
            }
            .map { it.trimEnd() }
            // Drop a trailing empty line caused by the final \n.
            .let { if (it.isNotEmpty() && it.last().isEmpty()) it.dropLast(1) else it }
            .filter { it.isNotBlank() }

        val boundary = boundaryDetector.detectPromptReady(
            // For prompt detection, look at the *appended* lines only —
            // not the full transcript — so we only react to prompts
            // that appeared since the last tick.
            appendedLines.map { it.trimEnd() },
        )

        if (outputLines.isNotEmpty() && boundary !is CommandBoundary.PromptReady) {
            blockRepository.onOutputChunk(outputLines.joinToString(separator = "\n"))
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

    /** Reset internal state — call when the active session changes. */
    fun reset() {
        previousTranscript.setLength(0)
        initialized = false
        lastSeenPrompt = false
        blockRepository.clear()
    }

    private fun commonPrefixLength(a: String, b: String): Int {
        val max = minOf(a.length, b.length)
        var i = 0
        while (i < max && a[i] == b[i]) i++
        return i
    }

    private companion object {
        const val ANCHOR_WINDOW_BYTES = 4096
        const val MAX_TRANSCRIPT_BYTES = 32 * 1024
    }
}
