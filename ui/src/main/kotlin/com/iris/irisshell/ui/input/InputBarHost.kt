package com.iris.irisshell.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.domain.input.InputIntent

/**
 * Combines the [KeyboardHandle] with (optionally) an [ExtraKeyBar] in a
 * single column. Layout when the bar is open:
 *
 * ```
 * ┌─────────────────────────────┐
 * │   ExtraKeyBar (rounded top) │  ← liquid-glass surface
 * ├─────────────────────────────┤
 * │   KeyboardHandle (thin pill) │  ← toggle affordance
 * └─────────────────────────────┘
 * ```
 *
 * When closed, only [KeyboardHandle] renders. The host is meant to sit
 * directly above the IME keyboard; the parent should apply
 * `Modifier.imePadding()` so the whole stack lifts above the soft
 * keyboard.
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
            ExtraKeyBar(
                ctrlStuck = uiState.ctrlStuck,
                altStuck = uiState.altStuck,
                onIntent = onIntent,
            )
            KeyboardHandle(
                barVisible = true,
                onToggle = onToggle,
            )
        } else {
            KeyboardHandle(
                barVisible = false,
                onToggle = onToggle,
            )
        }
    }
}
