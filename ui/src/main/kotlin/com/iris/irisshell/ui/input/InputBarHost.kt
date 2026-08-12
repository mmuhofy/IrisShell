package com.iris.irisshell.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.domain.input.InputIntent

/**
 * Host layout for on-screen input chrome:
 *
 * ```
 * Bar open   →   ┌────────────────────────┐
 *                 │  KeyboardHandle (pill) │  ← toggle, sits above the bar
 *                 ├────────────────────────┤
 *                 │  ExtraKeyBar (glass)   │  ← compact 2-row keys
 *                 └────────────────────────┘
 *
 * Bar closed  →   ┌────────────────────────┐
 *                 │  KeyboardHandle (pill) │  ← flush against IME
 *                 └────────────────────────┘
 * ```
 *
 * The host is meant to sit just above the IME soft keyboard; the
 * parent applies `Modifier.imePadding()` so the whole stack lifts.
 *
 * UNTESTED — verify on device.
 */
@Composable
fun InputBarHost(
    uiState: InputBarUiState,
    onToggle: () -> Unit,
    onIntent: (InputIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(IrisBackground),
    ) {
        if (uiState.renderBar) {
            KeyboardHandle(
                barVisible = true,
                onToggle = onToggle,
            )
            ExtraKeyBar(
                ctrlStuck = uiState.ctrlStuck,
                altStuck = uiState.altStuck,
                onIntent = onIntent,
            )
        } else {
            KeyboardHandle(
                barVisible = false,
                onToggle = onToggle,
            )
        }
    }
}
