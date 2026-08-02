package com.iris.irisshell.domain.block

/**
 * Result of a single command-boundary scan.
 *
 * `atLine` is the buffer row index (0-based, relative to the visible
 * screen, not the scrollback) where the boundary was found.
 * `exitCode` is only meaningful for `CommandBoundary.Completed` —
 * for `CommandBoundary.PromptReady` it is null because the new prompt
 * just appeared and we don't know yet what exit code the previous
 * command produced.
 */
sealed class CommandBoundary {
    /** New prompt is visible at row `atLine` — previous block closed. */
    data class PromptReady(val atLine: Int) : CommandBoundary()

    /** Command finished with known exit code at row `atLine`. */
    data class Completed(val atLine: Int, val exitCode: Int) : CommandBoundary()

    /** No boundary found in this scan. */
    object None : CommandBoundary()
}
