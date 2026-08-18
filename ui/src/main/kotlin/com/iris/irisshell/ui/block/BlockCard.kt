package com.iris.irisshell.ui.block

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.iris.irisshell.design.system.IrisBuild
import com.iris.irisshell.design.system.IrisDropdownMenu
import com.iris.irisshell.design.system.IrisError
import com.iris.irisshell.design.system.IrisMenuItem
import com.iris.irisshell.design.system.IrisMenuItemStyle
import com.iris.irisshell.design.system.IrisOutline
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSuccess
import com.iris.irisshell.design.system.IrisSurface
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
    val accentColor = when (block.state) {
        is BlockState.Success -> if (isActive) IrisPrimary else IrisSuccess
        is BlockState.Error -> if (isActive) IrisPrimary else IrisError
        BlockState.Running -> IrisBuild
        else -> IrisOutline
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(IrisSurface, RoundedCornerShape(6.dp))
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
                BlockBody(block = block, onLongClick = { contextOpen = true })
            }
        }

        IrisDropdownMenu(
            expanded = contextOpen,
            onDismissRequest = { contextOpen = false },
            items = listOf(
                IrisMenuItem(
                    label = "Komutu kopyala",
                    icon = painterResource(R.drawable.lucide_copy),
                ),
                IrisMenuItem(
                    label = "Tekrar çalıştır",
                    icon = painterResource(R.drawable.lucide_play),
                ),
                IrisMenuItem(
                    label = "Komutu düzenle",
                    icon = painterResource(R.drawable.lucide_pencil),
                ),
                IrisMenuItem(
                    label = "Output'u kopyala",
                    icon = painterResource(R.drawable.lucide_copy),
                    dividerBefore = true,
                ),
                IrisMenuItem(
                    label = "Dışa aktar",
                    icon = painterResource(R.drawable.lucide_download),
                ),
                IrisMenuItem(
                    label = "Block'u sil",
                    icon = painterResource(R.drawable.lucide_trash_2),
                    style = IrisMenuItemStyle.Destructive,
                    dividerBefore = true,
                ),
            ),
            onItemClick = { item ->
                when (item.label) {
                    "Komutu kopyala" -> onCopyCommand()
                    "Tekrar çalıştır" -> onRerun()
                    "Komutu düzenle" -> onEdit()
                    "Output'u kopyala" -> onCopyOutput()
                    "Dışa aktar" -> onExport()
                    "Block'u sil" -> onDelete()
                }
            },
        )
    }
}
