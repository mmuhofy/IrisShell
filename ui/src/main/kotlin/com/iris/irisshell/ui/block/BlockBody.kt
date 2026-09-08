package com.iris.irisshell.ui.block

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.domain.UrlDetector
import com.iris.irisshell.domain.block.Block
import com.iris.irisshell.domain.block.BlockState

@Composable
fun BlockBody(
    block: Block,
    onLongClick: (() -> Unit)? = null,
    onUrlClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
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
        // Input line: long-press opens context menu. Selection is
        // available too — combinedClickable handles both.
        if (onLongClick != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                        onLongClick = onLongClick,
                    ),
            ) {
                SelectionContainer {
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
            }
        } else {
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
        }
        if (!block.isCollapsed) {
            if (block.outputLines.isNotEmpty()) {
                SelectionContainer(modifier = Modifier.fillMaxWidth().padding(top = 2.dp)) {
                    Column {
                        block.outputLines.forEach { line ->
                            OutputLineWithLinks(
                                line = line,
                                onUrlClick = onUrlClick,
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

@Composable
private fun OutputLineWithLinks(
    line: String,
    onUrlClick: (String) -> Unit,
) {
    val textStyle = LocalTextStyle.current.copy(
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        lineHeight = 19.sp,
        textAlign = TextAlign.Start,
    )

    val urlMatches = UrlDetector.findUrls(line)

    if (urlMatches.isEmpty()) {
        Text(
            text = line.ifEmpty { " " },
            color = IrisTextSecondary,
            style = textStyle,
            modifier = Modifier.fillMaxWidth(),
        )
        return
    }

    val text = line.ifEmpty { " " }
    val annotated = buildAnnotatedString {
        var cursor = 0
        for (match in urlMatches) {
            if (match.start > cursor) {
                withStyle(SpanStyle(color = IrisTextSecondary)) {
                    append(text.substring(cursor, match.start))
                }
            }
            withStyle(
                SpanStyle(
                    color = IrisPrimary,
                    textDecoration = TextDecoration.Underline,
                ),
            ) {
                append(text.substring(match.start, match.end))
            }
            cursor = match.end
        }
        if (cursor < text.length) {
            withStyle(SpanStyle(color = IrisTextSecondary)) {
                append(text.substring(cursor))
            }
        }
    }

    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Text(
        text = annotated,
        style = textStyle,
        softWrap = true,
        onTextLayout = { layoutResult = it },
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures { tapPosition: Offset ->
                    val layout = layoutResult ?: return@detectTapGestures
                    val offset = layout.getOffsetForPosition(tapPosition)
                    val clamped = offset.coerceAtLeast(0).coerceAtMost(text.length - 1)
                    val charIndex = if (clamped < 0) 0 else clamped
                    for (match in urlMatches) {
                        if (charIndex in match.start..match.end) {
                            onUrlClick(match.url)
                            return@detectTapGestures
                        }
                    }
                }
            },
    )
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
