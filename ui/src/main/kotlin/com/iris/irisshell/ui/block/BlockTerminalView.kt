package com.iris.irisshell.ui.block

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.domain.block.Block
import kotlinx.coroutines.flow.StateFlow

@Composable
fun BlockTerminalView(
    blocks: StateFlow<List<Block>>,
    onToggleCollapsed: (blockId: String) -> Unit,
    onCommandSubmitted: (prompt: String, command: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val list by blocks.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val clipboard = LocalClipboardManager.current

    val shouldAutoScroll by remember(list.size) {
        derivedStateOf {
            list.isEmpty() || listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0 >= list.size - 2
        }
    }
    LaunchedEffect(list.size) {
        if (shouldAutoScroll && list.isNotEmpty()) {
            listState.animateScrollToItem(list.size - 1)
        }
    }

    Column(modifier = modifier.fillMaxSize().background(IrisBackground)) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            state = listState,
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
        ) {
            items(list, key = { it.id }) { block ->
                val isActive = block.id == list.lastOrNull()?.id
                BlockCard(
                    block = block,
                    isActive = isActive,
                    onCopy = {
                        val text = buildString {
                            append(block.prompt)
                            append(" ")
                            append(block.command)
                            append("\n")
                            append(block.outputLines.joinToString("\n"))
                        }
                        clipboard.setText(AnnotatedString(text))
                    },
                    onToggleCollapse = { onToggleCollapsed(block.id) },
                    modifier = Modifier.padding(vertical = 2.dp),
                )
            }
        }
        BlockInputField(onSubmit = { cmd ->
            onCommandSubmitted("", cmd)
        })
    }
}
