package com.iris.irisshell.terminal

// Ported from: mmuhofy/IrisCode — app/src/main/kotlin/com/iris/iriscode/terminal/ExtraKeyState.kt
// Adapted for Iris Shell — com.iris.irisshell
//
// Originally used Compose runtime's mutableStateOf/getValue/setValue. Since this
// module does NOT apply the Compose plugin (Compose belongs to :ui and :app per
// AGENT.md §109, §139), we replace the observable properties with plain mutable
// Kotlin properties. The :ui layer wraps these in Compose-backed state where
// recomposition is needed.

class ExtraKeyState {
    var ctrlActive: Boolean = false
    var ctrlLocked: Boolean = false
    var altActive: Boolean = false
    var altLocked: Boolean = false

    fun tapCtrl() {
        if (ctrlLocked) {
            ctrlLocked = false
            ctrlActive = false
        } else if (ctrlActive) {
            ctrlActive = false
        } else {
            ctrlActive = true
        }
    }

    fun longPressCtrl() {
        ctrlActive = true
        ctrlLocked = true
    }

    fun readCtrl(): Boolean {
        if (!ctrlActive) return false
        if (!ctrlLocked) ctrlActive = false
        return true
    }

    fun tapAlt() {
        if (altLocked) {
            altLocked = false
            altActive = false
        } else if (altActive) {
            altActive = false
        } else {
            altActive = true
        }
    }

    fun longPressAlt() {
        altActive = true
        altLocked = true
    }

    fun readAlt(): Boolean {
        if (!altActive) return false
        if (!altLocked) altActive = false
        return true
    }
}
