package com.iris.irisshell.ui.block

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.domain.block.Block
import com.iris.irisshell.domain.block.BlockState
import com.iris.irisshell.domain.block.NetworkDelta

@Composable
fun BlockHeader(block: Block, onCopy: () -> Unit, onToggleCollapse: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(IrisSurfaceVariant, RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ExitBadge(block)
        Spacer(Modifier.width(8.dp))
        DurationBadge(block)
        Spacer(modifier = Modifier.weight(1f))
        if (block.networkDelta.hasTraffic) {
            NetworkBadge(block.networkDelta)
            Spacer(Modifier.width(8.dp))
        }
        CopyButton(onCopy)
        Spacer(Modifier.width(4.dp))
        CollapseButton(block.isCollapsed, onToggleCollapse)
    }
}

@Composable
private fun ExitBadge(block: Block) {
    val state = block.state
    val isRunning = state is BlockState.Running
    val (label, color) = when (state) {
        BlockState.Running -> "Running" to Color(0xFF4A90E2)
        is BlockState.Success -> "Exit ${state.exitCode}" to Color(0xFF27AE60)
        is BlockState.Error -> "Exit ${state.exitCode}" to Color(0xFFC0392B)
        BlockState.Cancelled -> "Cancelled" to Color(0xFF888888)
        BlockState.Idle -> "Idle" to Color(0xFF666666)
    }
    if (isRunning) {
        val transition = rememberInfiniteTransition(label = "running-spinner")
        val rotation by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200),
                repeatMode = RepeatMode.Restart,
            ),
            label = "spinner-rotation",
        )
        Row(
            modifier = Modifier
                .background(color.copy(alpha = 0.16f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .rotate(rotation)
                    .background(color, CircleShape),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = label,
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
            )
        }
    } else {
        Pill(text = label, color = color)
    }
}

@Composable
private fun DurationBadge(block: Block) {
    val text = formatDuration(block.elapsedMs(System.currentTimeMillis()))
    Pill(text = "⏱ $text", color = Color(0xFF888888))
}

@Composable
private fun NetworkBadge(delta: NetworkDelta) {
    val text = buildString {
        if (delta.rxBytes > 0) append("↓ ${formatBytes(delta.rxBytes)}")
        if (delta.rxBytes > 0 && delta.txBytes > 0) append("  ")
        if (delta.txBytes > 0) append("↑ ${formatBytes(delta.txBytes)}")
    }
    Pill(text = text, color = IrisPrimary)
}

@Composable
private fun CopyButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clickable(onClick = onClick)
            .border(1.dp, IrisBorderSubtle, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = "Copy",
            tint = IrisTextSecondary,
            modifier = Modifier.size(14.dp),
        )
    }
}

@Composable
private fun CollapseButton(isCollapsed: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clickable(onClick = onClick)
            .border(1.dp, IrisBorderSubtle, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.ExpandMore,
            contentDescription = if (isCollapsed) "Expand" else "Collapse",
            tint = IrisTextSecondary,
            modifier = Modifier
                .size(16.dp)
                .rotate(if (isCollapsed) -90f else 0f),
        )
    }
}

@Composable
private fun Pill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.16f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Monospace,
        )
    }
}

private fun formatDuration(ms: Long): String {
    if (ms < 1000) return "${ms}ms"
    val sec = ms / 1000
    if (sec < 60) return "${sec}s"
    val min = sec / 60
    val remSec = sec % 60
    return if (min < 60) "${min}m ${remSec}s"
    else {
        val hr = min / 60
        val remMin = min % 60
        "${hr}h ${remMin}m"
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${kb.toInt()} KB"
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format("%.1f GB", gb)
}
