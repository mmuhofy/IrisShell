package com.iris.irisshell.domain.settings

import kotlinx.coroutines.flow.Flow

/**
 * User preferences that live in DataStore.
 *
 * Currently surfaces only the "Block Mode" toggle — when enabled, the
 * terminal renders as a stack of HUD cards (one per command) instead of
 * the classic Termux `TerminalView`. Both modes share the same session
 * and `TerminalManager`; only the render layer switches.
 *
 * Default: false (Classic Mode). Users opt into Block Mode from Settings.
 *
 * See `docs/block-engine/PLAN.md` §9.
 */
interface SettingsRepository {

    /** Hot stream of the Block Mode flag. Emits false on first launch. */
    val useBlockEngine: Flow<Boolean>

    /** Persists the Block Mode flag. */
    suspend fun setUseBlockEngine(enabled: Boolean)
}
