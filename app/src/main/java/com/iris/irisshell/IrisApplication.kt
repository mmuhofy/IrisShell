package com.iris.irisshell

import android.app.Application
import java.io.File
import android.content.Intent
import androidx.core.content.ContextCompat
import com.iris.irisshell.data.session.SessionManagerAdapter
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry — `@HiltAndroidApp` triggers Hilt's code generation for the
 * entire component tree.
 *
 * Ported from mmuhofy/IrisCode — app/src/main/kotlin/com/iris/iriscode/IrisCodeApp.kt
 * Adapted for Iris Shell — com.iris.irisshell
 *
 * Phase 1+ — boots [SessionManagerAdapter] for session reconciliation,
 * then starts [TerminalService] as a foreground service so PTY sessions
 * survive process-level death by the Activity.
 */
@HiltAndroidApp
class IrisApplication : Application() {

    @Inject lateinit var sessionManagerAdapter: SessionManagerAdapter
    @Inject lateinit var terminalManager: TerminalManager

    override fun onCreate() {
        super.onCreate()

        // Pre-create hooks file + completion file at /sdcard/IrisShell/
        // so TerminalService can start polling immediately — even before
        // the first PTY session is created. The hooks file is injected
        // via $ENV into zsh, and the completion file is written by precmd.
        terminalManager?.writeShellHooksFile()
        ensureCompletionFile()

        sessionManagerAdapter.start()
        ContextCompat.startForegroundService(this, Intent(this, TerminalService::class.java))
    }

    private fun ensureCompletionFile() {
        val dir = File("/sdcard/IrisShell")
        dir.mkdirs()
        val file = File(dir, TerminalService.COMPLETION_FILE_NAME)
        if (!file.exists()) file.createNewFile()
    }
}

