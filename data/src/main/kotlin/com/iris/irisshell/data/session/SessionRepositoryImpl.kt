package com.iris.irisshell.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.iris.irisshell.data.di.ApplicationScope
import com.iris.irisshell.data.local.IrisDatabase
import com.iris.irisshell.data.local.irisShellDataStore
import com.iris.irisshell.domain.session.SessionRepository
import com.iris.irisshell.domain.session.SessionSnapshot
import com.iris.irisshell.domain.session.SessionState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Room-backed implementation of [SessionRepository].
 *
 * Two storage tiers:
 *  - **Room** (`sessions` table) — durable metadata: id, name, state,
 *    timestamps, last-will snapshot. Survives process death.
 *  - **DataStore Preferences** (`KEY_ACTIVE_SESSION_ID`) — pointer to
 *    the currently-active session id. Survives process death so the
 *    app re-opens on the last session.
 *
 * The data layer does NOT own the PTY process — that lives in
 * `:terminal`'s `TerminalManager`. This class merely tracks metadata;
 * the TerminalManager is adapted (Phase 2) to honour this repository's
 * active-session id and to feed live previews back through a callback.
 *
 * Live preview ticker is wired in a follow-up step. In this iteration
 * the repository only knows how to merge Room + DataStore; the UI sees
 * `liveSnapshotLines = emptyList()` until the TerminalManager feeds it.
 */
@Singleton
class SessionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: IrisDatabase,
    @ApplicationScope private val appScope: CoroutineScope,
) : SessionRepository {

    private val dao: SessionDao = database.sessionDao()
    private val dataStore: DataStore<Preferences> = context.irisShellDataStore

    override fun observeAll(): Flow<List<SessionSnapshot>> =
        dao.observeAll()
            .map { rows -> rows.map { it.toSnapshot(emptyList()) } }
            .distinctUntilChanged()

    override fun observe(id: String): Flow<SessionSnapshot?> =
        dao.observe(id)
            .map { row -> row?.toSnapshot(emptyList()) }
            .distinctUntilChanged()

    override suspend fun create(name: String): String {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()
        dao.upsert(
            SessionEntity(
                id = id,
                name = name,
                state = SessionState.Idle.name,
                createdAtMs = now,
                lastUsedAtMs = now,
                lastSnapshot = "",
            )
        )
        // Default to activating the newly created session.
        appScope.launch {
            dataStore.edit { it[KEY_ACTIVE_SESSION_ID] = id }
        }
        return id
    }

    override suspend fun rename(id: String, newName: String) {
        dao.rename(id, newName)
    }

    override suspend fun delete(id: String) {
        dao.delete(id)
        // If we just deleted the active one, clear the active pointer
        // and (TODO Phase 2 follow-up) fall back to most-recent.
        val activeId = dataStore.data
            .map { it[KEY_ACTIVE_SESSION_ID] }
            .first()
        if (activeId == id) {
            dataStore.edit { it.remove(KEY_ACTIVE_SESSION_ID) }
        }
    }

    override suspend fun touch(id: String) {
        val row = dao.observe(id).first() ?: return
        dao.updateRuntime(
            id = id,
            nowMs = System.currentTimeMillis(),
            state = row.state,
            snapshot = row.lastSnapshot,
        )
    }

    /** In-memory pointer to the active session id. */
    fun observeActiveId(): Flow<String?> =
        dataStore.data.map { it[KEY_ACTIVE_SESSION_ID] }.distinctUntilChanged()

    /** Suspend getter for the active session id (or null). */
    suspend fun currentActiveId(): String? =
        dataStore.data.map { it[KEY_ACTIVE_SESSION_ID] }.first()

    /** Updates the in-memory + persisted active pointer. */
    suspend fun setActiveId(id: String?) {
        dataStore.edit { prefs ->
            if (id == null) prefs.remove(KEY_ACTIVE_SESSION_ID)
            else prefs[KEY_ACTIVE_SESSION_ID] = id
        }
    }

    /** Combined flow: active id + every-session flow → active snapshot. */
    fun observeActive(): Flow<SessionSnapshot?> =
        combine(observeActiveId(), dao.observeAll()) { activeId, rows ->
            if (activeId == null) null
            else rows.firstOrNull { it.id == activeId }?.toSnapshot(emptyList())
        }
            .distinctUntilChanged()

    private fun SessionEntity.toSnapshot(live: List<String>): SessionSnapshot =
        SessionSnapshot(
            id = id,
            name = name,
            state = runCatching { SessionState.valueOf(state) }.getOrDefault(SessionState.Idle),
            createdAtMs = createdAtMs,
            lastUsedAtMs = lastUsedAtMs,
            liveSnapshotLines = live,
        )

    private companion object {
        val KEY_ACTIVE_SESSION_ID = stringPreferencesKey("active_session_id")
    }
}
