package com.iris.irisshell.ui.block

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisBuild
import com.iris.irisshell.design.system.IrisError
import com.iris.irisshell.design.system.IrisSuccess
import com.iris.irisshell.domain.block.Block
import com.iris.irisshell.domain.block.BlockState

@Composable
fun BlockCard(
    block: Block,
    onCopy: () -> Unit,
    onToggleCollapse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor: Color? = when (val state = block.state) {
        BlockState.Running -> {
            val transition = rememberInfiniteTransition(label = "running-pulse")
            val alpha by transition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1500),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "running-pulse-alpha",
            )
            IrisBuild.copy(alpha = alpha)
        }
        is BlockState.Success -> IrisSuccess
        is BlockState.Error -> IrisError
        else -> null
    }
    val borderColor = accentColor ?: IrisBorderSubtle
    val borderWidth = if (block.state is BlockState.Running) 1.5.dp else 1.dp

    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(10.dp)),
    ) {
        BlockHeader(
            block = block,
            onCopy = onCopy,
            onToggleCollapse = onToggleCollapse,
        )
        BlockBody(block = block)
    }
}
