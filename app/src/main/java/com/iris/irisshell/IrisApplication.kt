package com.iris.irisshell

import android.app.Application
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
 * Phase 2 — boots [SessionManagerAdapter] so the persistent session
 * graph and the runtime TerminalManager stay in sync from the very
 * first frame.
 */
@HiltAndroidApp
class IrisApplication : Application() {

    @Inject lateinit var sessionManagerAdapter: SessionManagerAdapter

    override fun onCreate() {
        super.onCreate()
        sessionManagerAdapter.start()
    }
}

