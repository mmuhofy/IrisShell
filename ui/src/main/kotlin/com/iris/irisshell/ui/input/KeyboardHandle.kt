package com.iris.irisshell.ui.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisPrimary

/**
 * Mini grab-handle that toggles the extra-keys bar. Just a simple pill (56×4)
 * centred horizontally. No background container - just the pill itself.
 * Gold when bar is open, subtle grey when closed.
 */
@Composable
fun KeyboardHandle(
    barVisible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(16.dp)
            .clickable(
                interactionSource = interactionSource,
                role = Role.Switch,
                onClick = onToggle,
            )
            .semantics {
                contentDescription =
                    if (barVisible) "Hide extra keys bar" else "Show extra keys bar"
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .size(width = 56.dp, height = 4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(if (barVisible) IrisPrimary else IrisBorderSubtle),
        )
    }
}
