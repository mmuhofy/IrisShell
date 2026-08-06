package com.iris.irisshell.ui.block

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisBuild
import com.iris.irisshell.design.system.IrisError
import com.iris.irisshell.design.system.IrisOutline
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSuccess
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.domain.block.Block
import com.iris.irisshell.domain.block.BlockState
import com.iris.irisshell.ui.R

@Composable
fun BlockCard(
    block: Block,
    isActive: Boolean,
    onCopy: () -> Unit,
    onCopyCommand: () -> Unit,
    onCopyOutput: () -> Unit,
    onRerun: () -> Unit,
    onEdit: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit,
    onToggleCollapse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var contextOpen by remember { mutableStateOf(false) }
    val accentColor = when (val state = block.state) {
        is BlockState.Success -> if (isActive) IrisPrimary else IrisSuccess
        is BlockState.Error -> if (isActive) IrisPrimary else IrisError
        BlockState.Running -> IrisBuild
        else -> IrisOutline
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(IrisSurface, RoundedCornerShape(6.dp))
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = { contextOpen = true },
            )
            .padding(start = 12.dp, end = 10.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
                    .background(accentColor.copy(alpha = if (isActive) 0.8f else 0.6f)),
            )
            Column(modifier = Modifier.fillMaxWidth().padding(start = 10.dp)) {
                BlockHeader(
                    block = block,
                    onCopy = onCopy,
                    onToggleCollapse = onToggleCollapse,
                )
                BlockBody(block = block)
            }
        }
        DropdownMenu(
            expanded = contextOpen,
            onDismissRequest = { contextOpen = false },
        ) {
            MenuItem(iconRes = R.drawable.lucide_copy, label = "Komutu kopyala", onClick = { onCopyCommand(); contextOpen = false })
            MenuItem(iconRes = R.drawable.lucide_play, label = "Tekrar çalıştır", onClick = { onRerun(); contextOpen = false })
            MenuItem(iconRes = R.drawable.lucide_pencil, label = "Komutu düzenle", onClick = { onEdit(); contextOpen = false })
            MenuItem(iconRes = R.drawable.lucide_copy, label = "Output'u kopyala", onClick = { onCopyOutput(); contextOpen = false })
            MenuItem(iconRes = R.drawable.lucide_download, label = "Dışa aktar", onClick = { onExport(); contextOpen = false })
            MenuItem(iconRes = R.drawable.lucide_trash_2, label = "Block'u sil", onClick = { onDelete(); contextOpen = false })
        }
    }
}

@Composable
private fun MenuItem(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
) {
    DropdownMenuItem(
        leadingIcon = {
            androidx.compose.material3.Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = IrisTextSecondary,
                modifier = Modifier.width(16.dp),
            )
        },
        text = {
            Text(
                text = label,
                color = IrisText,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
            )
        },
        onClick = onClick,
    )
}
