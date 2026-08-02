package com.iris.irisshell.ui.block

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.domain.block.Block

@Composable
fun BlockBody(block: Block, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        InputRow(prompt = block.prompt, command = block.command)
        if (block.outputLines.isNotEmpty() && !block.isCollapsed) {
            HorizontalDivider(thickness = 1.dp, color = IrisBorderSubtle)
            OutputRow(outputLines = block.outputLines)
        }
    }
}

@Composable
private fun InputRow(prompt: String, command: String) {
    Row(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = prompt,
            color = IrisPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
        )
        Text(
            text = " $command",
            color = IrisText,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun OutputRow(outputLines: List<String>) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
        outputLines.forEach { line ->
            Text(
                text = line,
                color = IrisText,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
            )
        }
    }
}
