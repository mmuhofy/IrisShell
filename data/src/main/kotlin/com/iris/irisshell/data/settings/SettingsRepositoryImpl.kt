package com.iris.irisshell.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
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
 * Block Mode flag lives at [KEY_USE_BLOCK_ENGINE]; extra-keys bar
 * visibility lives at [KEY_EXTRA_KEYS_BAR_VISIBLE]. Both default to
 * false on first launch, so the user's out-of-the-box experience is
 * Classic Terminal with no on-screen bar — matching
 * `docs/block-engine/PLAN.md` §2 and `docs/MEMORYBANK.md` §8.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    private val dataStore: DataStore<Preferences> = context.irisShellDataStore

    override val useBlockEngine: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[KEY_USE_BLOCK_ENGINE] ?: DEFAULT_USE_BLOCK_ENGINE }

    override suspend fun setUseBlockEngine(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_USE_BLOCK_ENGINE] = enabled }
    }

    override val extraKeysBarVisible: Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[KEY_EXTRA_KEYS_BAR_VISIBLE] ?: DEFAULT_EXTRA_KEYS_BAR_VISIBLE }

    override suspend fun setExtraKeysBarVisible(visible: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_EXTRA_KEYS_BAR_VISIBLE] = visible }
    }

    override val fontSizeSp: Flow<Int> =
        dataStore.data.map { prefs -> prefs[KEY_FONT_SIZE_SP] ?: DEFAULT_FONT_SIZE_SP }

    override suspend fun setFontSize(size: Int) {
        dataStore.edit { prefs -> prefs[KEY_FONT_SIZE_SP] = size }
    }

    private companion object {
        val KEY_USE_BLOCK_ENGINE = booleanPreferencesKey("use_block_engine")
        const val DEFAULT_USE_BLOCK_ENGINE: Boolean = false
        val KEY_EXTRA_KEYS_BAR_VISIBLE = booleanPreferencesKey("extra_keys_bar_visible")
        const val DEFAULT_EXTRA_KEYS_BAR_VISIBLE: Boolean = false
        val KEY_FONT_SIZE_SP = intPreferencesKey("font_size_sp")
        const val DEFAULT_FONT_SIZE_SP: Int = 14
    }
}
