package com.iris.irisshell.ui.block

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.domain.block.Block

@Composable
fun BlockCard(
    block: Block,
    onCopy: () -> Unit,
    onToggleCollapse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, IrisBorderSubtle, RoundedCornerShape(10.dp)),
    ) {
        BlockHeader(
            block = block,
            onCopy = onCopy,
            onToggleCollapse = onToggleCollapse,
        )
        BlockBody(block = block)
    }
}
