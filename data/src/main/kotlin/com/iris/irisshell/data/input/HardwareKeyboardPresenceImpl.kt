package com.iris.irisshell.data.input

import android.content.Context
import android.content.res.Configuration
import android.view.InputDevice
import com.iris.irisshell.domain.input.HardwareKeyboardPresence
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects whether a hardware `KEYBOARD_TYPE_ALPHABETIC` input device is
 * currently connected. Polled at registration time and refreshed by an
 * Android configuration-change listener.
 *
 * Termux's heuristic: see
 * `termux/termux-app/TermuxTerminalViewClient.handleVirtualKeys` —
 * the bar is hidden when `InputDevice.getKeyboardType()` equals
 * `KEYBOARD_TYPE_ALPHABETIC`. We mirror that exactly.
 *
 * Note: we listen to the global `Configuration` callback. This fires on
 * orientation changes too, which is harmless — the predicate is
 * re-evaluated against the current set of input devices.
 */
@Singleton
class HardwareKeyboardPresenceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : HardwareKeyboardPresence {

    override val isPresent: Flow<Boolean> = callbackFlow {
        val listener = android.content.ComponentCallbacks2 { _ ->
            trySend(currentlyPresent())
        }
        context.registerComponentCallbacks(listener)
        awaitClose { context.unregisterComponentCallbacks(listener) }
    }
        .onStart { emit(currentlyPresent()) }
        .distinctUntilChanged()

    private fun currentlyPresent(): Boolean {
        val ids = InputDevice.getDeviceIds()
        ids.forEach { id ->
            val device = InputDevice.getDevice(id) ?: return@forEach
            if (device.keyboardType == InputDevice.KEYBOARD_TYPE_ALPHABETIC) return true
        }
        return false
    }

    @Suppress("unused")
    private fun isLandscape(configuration: Configuration): Boolean =
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}
