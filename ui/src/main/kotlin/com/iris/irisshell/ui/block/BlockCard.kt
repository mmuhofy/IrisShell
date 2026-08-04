package com.iris.irisshell.ui.block

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.iris.irisshell.design.system.IrisBuild
import com.iris.irisshell.design.system.IrisError
import com.iris.irisshell.design.system.IrisOutline
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSuccess
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.domain.block.Block
import com.iris.irisshell.domain.block.BlockState

@Composable
fun BlockCard(
    block: Block,
    isActive: Boolean,
    onCopy: () -> Unit,
    onToggleCollapse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor: Color = when (val state = block.state) {
        is BlockState.Success -> if (isActive) IrisPrimary else IrisSuccess
        is BlockState.Error -> if (isActive) IrisPrimary else IrisError
        BlockState.Running -> IrisBuild
        else -> IrisOutline
    }
    val shape = RoundedCornerShape(8.dp)
    val borderColor = if (isActive) accentColor.copy(alpha = 0.4f) else IrisOutline
    val shadowModifier = if (isActive) {
        Modifier.shadow(
            elevation = 4.dp,
            shape = shape,
            ambientColor = IrisPrimary.copy(alpha = 0.15f),
            spotColor = IrisPrimary.copy(alpha = 0.10f),
        )
    } else Modifier

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(IrisSurface, shape)
            .border(1.dp, borderColor, shape)
            .then(shadowModifier),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(accentColor),
            )
            Column(modifier = Modifier.fillMaxWidth()) {
                BlockHeader(
                    block = block,
                    onCopy = onCopy,
                    onToggleCollapse = onToggleCollapse,
                )
                BlockBody(block = block)
            }
        }
    }
}
