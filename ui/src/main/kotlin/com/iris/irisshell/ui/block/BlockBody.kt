package com.iris.irisshell.ui.block

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.domain.block.Block
import com.iris.irisshell.domain.block.BlockState

@Composable
fun BlockBody(block: Block, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        InputRow(prompt = block.prompt, command = block.command)
        if (!block.isCollapsed) {
            if (block.outputLines.isNotEmpty()) {
                OutputRow(outputLines = block.outputLines)
            } else {
                EmptyOutputPlaceholder(isRunning = block.state is BlockState.Running)
            }
        }
    }
}

@Composable
private fun InputRow(prompt: String, command: String) {
    val style = LocalTextStyle.current.copy(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        lineHeight = 21.sp,
        textAlign = TextAlign.Start,
    )
    Row(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)) {
        Text(text = prompt, color = IrisPrimary, style = style)
        Text(text = command, color = IrisText, style = style)
    }
}

@Composable
private fun OutputRow(outputLines: List<String>) {
    val style = LocalTextStyle.current.copy(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        lineHeight = 21.sp,
        textAlign = TextAlign.Start,
    )
    Column(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
        outputLines.forEach { line ->
            Text(
                text = line.ifEmpty { " " },
                color = IrisTextSecondary,
                style = style,
            )
        }
    }
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
