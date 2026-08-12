package com.iris.irisshell.ui.input

import androidx.compose.foundation.background
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
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisPrimary

/**
 * Mini grab-handle that toggles the extra-keys bar. Visible affordance
 * is a short 56×4 pill, centred horizontally with rounded ends. When the
 * bar is open, the pill takes the gold accent; when closed, it sits dim
 * grey so it doesn't compete with the terminal content.
 *
 * The tappable area is the full width of the host — generous hit
 * target, tiny visual footprint (iOS-style grabber).
 *
 * UNTESTED — verify on device.
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
            .background(IrisBackground)
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
