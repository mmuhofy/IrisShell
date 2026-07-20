package com.iris.irisshell.ui.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.icons.Icons
import androidx.compose.material3.icons.filled.Send
import androidx.compose.material3.icons.filled.KeyboardArrowDown
import androidx.compose.material3.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.ui.theme.IrisPrimary
import com.iris.irisshell.ui.theme.IrisSurface
import com.iris.irisshell.ui.theme.IrisText
import com.iris.irisshell.ui.theme.IrisTextDisabled
import com.iris.irisshell.ui.theme.IrisTextSecondary

/**
 * Phase 1 input bar — captures line-mode text and forwards it to the
 * underlying [TerminalSession] via [onSubmit]. Send icon bypasses the IME
 * dependency: typing a command requires only a USB/Bluetooth keyboard or the
 * on-screen system keyboard. The "collapse" toggle hides the bar so the
 * raw TerminalView fills the screen for full-screen terminal use.
 *
 * Future work (Phase 3): multi-line editing, IDE-style cursor movement,
 * ghost-text autocomplete (MEMORYBANK §286–298).
 */
@Composable
fun TerminalInputBar(
    visible: Boolean,
    onSubmit: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textState: MutableState<String> = remember { mutableStateOf("") }

    if (visible) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            color = IrisSurface,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                BashPromptBox()
                Box(Modifier.weight(1f)) {
                    BasicTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = textState.value,
                        onValueChange = { textState.value = it },
                        textStyle = TextStyle(
                            color = IrisText,
                            fontSize = 13.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        ),
                        cursorBrush = SolidColor(IrisPrimary),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            val cmd = textState.value.trim()
                            if (cmd.isNotEmpty()) {
                                onSubmit(cmd)
                                textState.value = ""
                            }
                        }),
                        decorationBox = { inner ->
                            if (textState.value.isEmpty()) {
                                Text(
                                    text = "type a command…",
                                    color = IrisTextDisabled,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Start,
                                )
                            }
                            inner()
                        },
                    )
                }
                IconButton(onClick = {
                    val cmd = textState.value.trim()
                    if (cmd.isNotEmpty()) {
                        onSubmit(cmd)
                        textState.value = ""
                    }
                }) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send command",
                        tint = IrisPrimary,
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(IrisTextSecondary.copy(alpha = 0.18f)),
        )
    }

    // Collapse / expand handle — always visible regardless of bar state.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IrisSurface.copy(alpha = 0.9f))
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onToggleVisibility,
            modifier = Modifier.size(28.dp),
        ) {
            Icon(
                imageVector = if (visible) Icons.Filled.KeyboardArrowDown else Icons.Filled.KeyboardArrowUp,
                contentDescription = if (visible) "Hide input bar" else "Show input bar",
                tint = IrisTextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun BashPromptBox() {
    Box(
        modifier = Modifier.size(width = 32.dp, height = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$",
            color = IrisPrimary,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            fontSize = 16.sp,
        )
    }
}
