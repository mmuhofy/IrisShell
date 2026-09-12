package com.iris.irisshell.ui.block

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.domain.block.Block
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PromptTerminal(
    blocks: StateFlow<List<Block>>,
    promptLabel: String = "iris",
    modifier: Modifier = Modifier,
) {
    val blockList by blocks.collectAsStateWithLifecycle(emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(IrisBackground)
            .verticalScroll(rememberScrollState()),
    ) {
        blockList.forEachIndexed { index, block ->
            PromptBlock(block = block)
            if (index < blockList.lastIndex) {
                Divider()
            }
        }
    }
}

@Composable
fun PromptBlock(
    block: Block,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = IrisPrimary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)) {
                    append("${block.prompt}$ ")
                }
                withStyle(SpanStyle(color = IrisPrimary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)) {
                    append(block.command)
                }
            },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
        )

        if (block.outputLines.isNotEmpty()) {
            val outputText = block.outputLines.joinToString("\n")
            Text(
                text = outputText,
                color = if (block.state.isError()) IrisTextMuted else IrisText,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun Divider(
    modifier: Modifier = Modifier,
) {
    val dividerColor = IrisBorderSubtle
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(dividerColor),
    )
}
