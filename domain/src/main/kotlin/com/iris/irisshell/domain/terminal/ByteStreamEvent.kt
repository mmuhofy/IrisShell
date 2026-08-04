package com.iris.irisshell.domain.terminal

/**
 * Events emitted by [com.iris.irisshell.terminal.ByteStreamParser] as
 * it consumes the raw PTY byte stream.
 *
 * Each event describes a single logical line of terminal activity.
 * Aggregating these per block yields the transcript shown on a
 * `BlockCard`.
 */
sealed interface ByteStreamEvent {
    /**
     * A line of plain output emitted by the running program.
     *
     * Trailing `\r`/`\n` has been stripped. The line is **echo** — it
     * could be shell prompt text, command echo, or program stdout/stderr.
     * Disambiguation happens in the wire layer.
     */
    data class OutputLine(val text: String) : ByteStreamEvent

    /**
     * The shell prompt is now visible. The given text is everything
     * that precedes the cursor on the prompt line (e.g. `iris$`,
     * `muhofy@iris:~$`). When unknown, [text] is empty and the wire
     * falls back to a default prompt.
     */
    data class PromptReady(val text: String) : ByteStreamEvent

    /**
     * The shell exited an alternate-screen application (vim, less,
     * fzf, …). On this event the wire typically resets running block
     * counters but does not close the block — the next prompt does.
     */
    data object TuiExited : ByteStreamEvent

    /**
     * The shell entered an alternate-screen application.
     */
    data object TuiEntered : ByteStreamEvent
}
