package com.iris.irisshell.ui.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iris.irisshell.domain.input.InputIntent

/**
 * Hosts the keyboard handle and the floating Drosh Liquid Glass extra-key
 * surface immediately above the IME.
 */
@Composable
fun InputBarHost(
    uiState: InputBarUiState,
    onToggle: () -> Unit,
    onIntent: (InputIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
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
