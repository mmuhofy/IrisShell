package com.iris.irisshell.data.session

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persistent session metadata, stored in Room.
 *
 * Live preview is **not** in this table — it lives in memory and is
 * exposed by [SessionRepositoryImpl] through [SessionEntity] → [com.iris.irisshell.domain.session.SessionSnapshot]
 * on demand. Closing the app drops live previews but the row remains so
 * the user can re-open the session later.
 *
 * `lastSnapshot` is the last live preview captured at the moment the
 * process exited. Empty if the session has never been opened.
 */
@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "state")
    val state: String, // maps to com.iris.irisshell.domain.session.SessionState.name

    @ColumnInfo(name = "created_at_ms")
    val createdAtMs: Long,

    @ColumnInfo(name = "last_used_at_ms")
    val lastUsedAtMs: Long,

    /**
     * Joined with `\n`. Length capped to ~16 KB (Room TEXT column is
     * unbounded but we cap ourselves to bound the row size). Last ~50
     * lines of transcript at the moment of process exit.
     */
    @ColumnInfo(name = "last_snapshot")
    val lastSnapshot: String,
)
