package com.iris.irisshell.domain.settings

import kotlinx.coroutines.flow.Flow

/**
 * User preferences that live in DataStore.
 *
 * Surfaces:
 *  - Block Mode toggle
 *  - Extra-keys bar visibility toggle
 *  - Terminal font size (sp)
 *  - Terminal background color (hex string)
 *  - Accent color (hex string)
 *  - Terminal text color (hex string)
 *
 * See MEMORYBANK.md §5 and §8.
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

    /** Hot stream of the font size in sp. Emits 14 on first launch. */
    val fontSizeSp: Flow<Int>

    /** Persists the font size in sp. */
    suspend fun setFontSize(size: Int)

    /** Hot stream of the terminal background color as a hex string. Default: "#0C0C0C". */
    val terminalBgColor: Flow<String>

    /** Persists the terminal background color. */
    suspend fun setTerminalBgColor(hex: String)

    /** Hot stream of the accent color as a hex string. Default: "#E8C547". */
    val accentColor: Flow<String>

    /** Persists the accent color. */
    suspend fun setAccentColor(hex: String)

    /** Hot stream of the terminal text color as a hex string. Default: "#EEEEEE". */
    val terminalTextColor: Flow<String>

    /** Persists the terminal text color. */
    suspend fun setTerminalTextColor(hex: String)
}
