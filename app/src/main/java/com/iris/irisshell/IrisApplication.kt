package com.iris.irisshell

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry — `@HiltAndroidApp` triggers Hilt's code generation for the
 * entire component tree.
 *
 * Ported from mmuhofy/IrisCode — app/src/main/kotlin/com/iris/iriscode/IrisCodeApp.kt
 * Adapted for Iris Shell — com.iris.irisshell
 *
 * Phase 1 has no extra init logic (PRoot download, Room, DataStore) — those
 * arrive with the wired components in :data and :data/local.
 */
@HiltAndroidApp
class IrisApplication : Application()
