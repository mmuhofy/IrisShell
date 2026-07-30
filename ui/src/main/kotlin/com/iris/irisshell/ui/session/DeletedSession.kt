package com.iris.irisshell.ui.session

import com.iris.irisshell.domain.session.SessionSnapshot

/**
 * Snapshot of a session that was just deleted from the switcher, kept
 * around briefly so the user can hit "Undo" in the Snackbar.
 *
 * `wasActive` records whether the deleted session was the active one at
 * delete-time — used to label the Snackbar correctly. `fallbackName`
 * is the name of the session that became active after the delete (if
 * any); null means the list is now empty.
 */
data class DeletedSession(
    val snapshot: SessionSnapshot,
    val wasActive: Boolean,
    val fallbackName: String?,
)
