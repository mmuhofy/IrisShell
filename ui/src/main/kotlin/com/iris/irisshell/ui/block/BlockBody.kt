package com.iris.irisshell.ui.block

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.domain.block.Block
import com.iris.irisshell.domain.block.BlockState

@Composable
fun BlockBody(block: Block, modifier: Modifier = Modifier) {
    val prompt = block.prompt
    val command = block.command
    val showPrompt = !isLikelyPrompt(prompt)
    val inputAnnotated: AnnotatedString = if (showPrompt) {
        buildAnnotatedString {
            withStyle(SpanStyle(color = IrisPrimary)) { append(prompt) }
            append(" ")
            withStyle(SpanStyle(color = IrisText)) { append(command) }
        }
    } else {
        buildAnnotatedString { append(command) }
    }

    Column(modifier = modifier) {
        SelectionContainer(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = inputAnnotated,
                style = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    textAlign = TextAlign.Start,
                ),
            )
        }
        if (!block.isCollapsed) {
            if (block.outputLines.isNotEmpty()) {
                SelectionContainer(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
                    Column {
                        block.outputLines.forEach { line ->
                            Text(
                                text = line.ifEmpty { " " },
                                color = IrisTextSecondary,
                                style = LocalTextStyle.current.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 13.sp,
                                    lineHeight = 19.sp,
                                    textAlign = TextAlign.Start,
                                ),
                            )
                        }
                    }
                }
            } else {
                EmptyOutputPlaceholder(isRunning = block.state is BlockState.Running)
            }
        }
    }
}

private fun isLikelyPrompt(text: String): Boolean {
    val trimmed = text.trimEnd()
    if (trimmed.isEmpty()) return true
    return trimmed.endsWith("$ ") ||
        trimmed.endsWith("# ") ||
        trimmed.endsWith("❯ ") ||
        trimmed.endsWith("➜ ")
}

@Composable
private fun EmptyOutputPlaceholder(isRunning: Boolean, modifier: Modifier = Modifier) {
    val text = if (isRunning) "running…" else "no output"
    Text(
        text = text,
        color = IrisTextMuted,
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp,
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        modifier = modifier.fillMaxWidth().padding(top = 2.dp),
    )
}
