package com.iris.irisshell.domain.block

import kotlinx.coroutines.flow.StateFlow

/**
 * Public interface of the block engine, exposed to `:ui`.
 *
 * The implementation lives in `:data` and is injected via Hilt
 * (`BlockRepositoryImpl`). UI consumers observe the block list and
 * forward user actions (run/cancel) through the same interface.
 *
 * State transitions are exposed as `StateFlow` for Compose collection.
 * Mutations go through dedicated methods — never via state setters.
 *
 * See `docs/block-engine/PLAN.md` §3.
 */
interface BlockRepository {

    /** Current block list for the active session. In-memory only. */
    fun observe(): StateFlow<List<Block>>

    /** Currently-running block, or null if none. */
    fun observeRunningBlock(): StateFlow<Block?>

    /**
     * Notify the engine that the user has submitted a new command.
     * Closes any running block (as success/unknown) and opens a fresh
     * running block.
     */
    fun onCommandSubmitted(prompt: String, command: String, startRxBytes: Long, startTxBytes: Long)

    /**
     * Stream a chunk of output text. Lines are split on `\n`; trailing
     * partial lines are buffered until the next chunk.
     */
    fun onOutputChunk(chunk: String)

    /**
     * Mark the running block as completed with the given exit code.
     * Idempotent — calling twice has no effect.
     */
    fun onCommandCompleted(exitCode: Int)

    /** Mark the running block as cancelled by user action. */
    fun onCommandCancelled()

    /** Toggle the collapsed state of a specific block. */
    fun setCollapsed(blockId: String, collapsed: Boolean)

    /** Drop all blocks (e.g. on session close). */
    fun clear()
}
