package com.iris.irisshell.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisTextSecondary

/**
 * Thin drag handle that sits above the system keyboard. Tap toggles the
 * extra-keys bar. Visually inspired by Material 3 `drawerHandle` but
 * hand-tuned for the Iris dark+gold palette.
 *
 * State convention:
 *   - `barVisible = false` → handle is dim with a subtle hairline.
 *   - `barVisible = true`  → handle is highlighted with the gold accent.
 *
 * The handle is intentionally never draggable in v1 — drag-to-resize
 * is a follow-up. Tap-only keeps the gesture surface unambiguous.
 */
@Composable
fun KeyboardHandle(
    barVisible: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(IrisSurfaceVariant)
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = false, radius = 18.dp),
                role = Role.Switch,
                onClick = onToggle,
            )
            .semantics {
                contentDescription =
                    if (barVisible) "Hide extra keys bar" else "Show extra keys bar"
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 6.dp)
                .size(width = 36.dp, height = 3.dp)
                .background(
                    color = if (barVisible) IrisPrimary else IrisBorderSubtle,
                    shape = RoundedCornerShape(2.dp),
                ),
        )
        // Caption tucked under the handle — only when bar is closed, to
        // advertise the toggle affordance without competing with the bar.
        if (!barVisible) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 18.dp),
            ) {
                androidx.compose.material3.Text(
                    text = "▲",
                    color = IrisTextSecondary,
                    style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}
