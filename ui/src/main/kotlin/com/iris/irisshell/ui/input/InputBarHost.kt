package com.iris.irisshell.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.domain.input.InputIntent

/**
 * Combines the [KeyboardHandle] with (optionally) an [ExtraKeyBar] in a
 * single column. The bar only renders when [uiState] shows
 * `renderBar = true` — see [InputBarUiState.renderBar].
 *
 * Designed to sit above the IME keyboard, just below the block-style
 * input field (block mode) or directly below the TerminalView (classic
 * mode). The host should apply `Modifier.imePadding()` on a parent so
 * the whole stack lifts above the system keyboard.
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
            .background(IrisBackground)
            .navigationBarsPadding(),
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
