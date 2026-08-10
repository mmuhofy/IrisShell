package com.iris.irisshell.domain.settings

import kotlinx.coroutines.flow.Flow

/**
 * User preferences that live in DataStore.
 *
 * Currently surfaces:
 *  - Block Mode toggle — when enabled, the terminal renders as a stack of
 *    HUD cards (one per command) instead of the classic Termux
 *    `TerminalView`. Both modes share the same session and
 *    `TerminalManager`; only the render layer switches.
 *  - Extra-keys bar visibility toggle — when true, the on-screen
 *    keyboard handle shows a bar above the IME with ESC/TAB/CTRL/ALT
 *    and arrow keys (Phase 3 Sprint 1).
 *
 * Default: both false. Users opt into either from Settings.
 *
 * See `docs/block-engine/PLAN.md` §9 and `docs/MEMORYBANK.md` §8.
 */
interface SettingsRepository {

    /** Hot stream of the Block Mode flag. Emits false on first launch. */
    val useBlockEngine: Flow<Boolean>

    /** Persists the Block Mode flag. */
    suspend fun setUseBlockEngine(enabled: Boolean)

    /** Hot stream of the extra-keys bar visibility flag. Emits false on first launch. */
    val extraKeysBarVisible: Flow<Boolean>

    /** Persists the extra-keys bar visibility flag. */
    suspend fun setExtraKeysBarVisible(visible: Boolean)
}
