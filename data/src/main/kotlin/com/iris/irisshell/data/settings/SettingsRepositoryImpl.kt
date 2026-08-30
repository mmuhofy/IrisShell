package com.iris.irisshell.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.iris.irisshell.data.local.irisShellDataStore
import com.iris.irisshell.domain.settings.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore-backed implementation of [SettingsRepository].
 *
 * Persists all user preferences under named keys. Defaults match
 * MEMORYBANK.md §5 Visual Identity tokens.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    private val dataStore: DataStore<Preferences> = context.irisShellDataStore

    // ── Block Mode ────────────────────────────────────────────────────────────

    override val useBlockEngine: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[KEY_USE_BLOCK_ENGINE] ?: DEFAULT_USE_BLOCK_ENGINE }

    override suspend fun setUseBlockEngine(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_USE_BLOCK_ENGINE] = enabled }
    }

    // ── Extra Keys Bar ────────────────────────────────────────────────────────

    override val extraKeysBarVisible: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[KEY_EXTRA_KEYS_BAR_VISIBLE] ?: DEFAULT_EXTRA_KEYS_BAR_VISIBLE }

    override suspend fun setExtraKeysBarVisible(visible: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_EXTRA_KEYS_BAR_VISIBLE] = visible }
    }

    // ── Font Size ─────────────────────────────────────────────────────────────

    override val fontSizeSp: Flow<Int> =
        dataStore.data.map { prefs -> prefs[KEY_FONT_SIZE_SP] ?: DEFAULT_FONT_SIZE_SP }

    override suspend fun setFontSize(size: Int) {
        dataStore.edit { prefs -> prefs[KEY_FONT_SIZE_SP] = size }
    }

    // ── Terminal Background Color ─────────────────────────────────────────────

    override val terminalBgColor: Flow<String> =
        dataStore.data.map { prefs -> prefs[KEY_TERMINAL_BG_COLOR] ?: DEFAULT_TERMINAL_BG_COLOR }

    override suspend fun setTerminalBgColor(hex: String) {
        dataStore.edit { prefs -> prefs[KEY_TERMINAL_BG_COLOR] = hex }
    }

    // ── Accent Color ──────────────────────────────────────────────────────────

    override val accentColor: Flow<String> =
        dataStore.data.map { prefs -> prefs[KEY_ACCENT_COLOR] ?: DEFAULT_ACCENT_COLOR }

    override suspend fun setAccentColor(hex: String) {
        dataStore.edit { prefs -> prefs[KEY_ACCENT_COLOR] = hex }
    }

    // ── Terminal Text Color ───────────────────────────────────────────────────

    override val terminalTextColor: Flow<String> =
        dataStore.data.map { prefs -> prefs[KEY_TERMINAL_TEXT_COLOR] ?: DEFAULT_TERMINAL_TEXT_COLOR }

    override suspend fun setTerminalTextColor(hex: String) {
        dataStore.edit { prefs -> prefs[KEY_TERMINAL_TEXT_COLOR] = hex }
    }

    // ── Keys & Defaults ───────────────────────────────────────────────────────

    private companion object {
        val KEY_USE_BLOCK_ENGINE        = booleanPreferencesKey("use_block_engine")
        val KEY_EXTRA_KEYS_BAR_VISIBLE  = booleanPreferencesKey("extra_keys_bar_visible")
        val KEY_FONT_SIZE_SP            = intPreferencesKey("font_size_sp")
        val KEY_TERMINAL_BG_COLOR       = stringPreferencesKey("terminal_bg_color")
        val KEY_ACCENT_COLOR            = stringPreferencesKey("accent_color")
        val KEY_TERMINAL_TEXT_COLOR     = stringPreferencesKey("terminal_text_color")

        const val DEFAULT_USE_BLOCK_ENGINE       = false
        const val DEFAULT_EXTRA_KEYS_BAR_VISIBLE = false
        const val DEFAULT_FONT_SIZE_SP           = 14
        const val DEFAULT_TERMINAL_BG_COLOR      = "#0C0C0C"
        const val DEFAULT_ACCENT_COLOR           = "#E8C547"
        const val DEFAULT_TERMINAL_TEXT_COLOR    = "#EEEEEE"
    }
}
