package com.iris.irisshell.ui.block

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.domain.block.Block
import com.iris.irisshell.domain.block.BlockState

@Composable
fun PromptBlock(
    block: Block,
    promptDir: String? = null,
    promptSuffix: String = "$",
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (!promptDir.isNullOrEmpty()) {
            Text(
                text = promptDir,
                color = IrisTextSecondary,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 0.dp),
            )
        }

        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = IrisPrimary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)) {
                    append("$promptSuffix ")
                }
                withStyle(SpanStyle(color = IrisPrimary, fontFamily = FontFamily.Monospace, fontSize = 13.sp)) {
                    append(block.command)
                }
            },
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
        )

        if (block.outputLines.isNotEmpty()) {
            val outputColor = when (block.state) {
                is BlockState.Error -> IrisTextMuted
                else -> IrisText
            }
            Text(
                text = block.outputLines.joinToString("\n"),
                color = outputColor,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
fun PromptDivider(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(IrisBorderSubtle),
    )
}
