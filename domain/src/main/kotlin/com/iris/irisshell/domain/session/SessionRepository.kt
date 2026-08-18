package com.iris.irisshell.domain.session

import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun observeAll(): Flow<List<SessionSnapshot>>
    fun observe(id: String): Flow<SessionSnapshot?>
    suspend fun create(name: String): String
    suspend fun rename(id: String, newName: String)
    suspend fun delete(id: String)
    suspend fun restoreSession(snapshot: SessionSnapshot, activate: Boolean = false)
    suspend fun touch(id: String)
    suspend fun updateLivePreview(id: String, lines: List<String>)
    suspend fun updateState(id: String, state: SessionState)
}