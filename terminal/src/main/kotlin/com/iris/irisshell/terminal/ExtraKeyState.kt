package com.iris.irisshell.terminal

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class ExtraKeyState {
    var ctrlActive by mutableStateOf(false)
    var ctrlLocked by mutableStateOf(false)
    var altActive by mutableStateOf(false)
    var altLocked by mutableStateOf(false)

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
