package com.iris.irisshell.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.domain.input.ExtraKey
import com.iris.irisshell.domain.input.ExtraKeyBarLayout
import com.iris.irisshell.domain.input.InputIntent

/**
 * The 2-row on-screen bar — see [ExtraKeyBarLayout] for the default
 * layout. Keys are flush against one another (no gaps, iOS-keyboard
 * style); a per-key rounded highlight is painted by [ExtraKeyButton]
 * on press/sticky armed.
 *
 * The bar uses a translucent surface with a soft drop-shadow and
 * rounded top corners so it reads as a "liquid glass" overlay surface
 * floating above the terminal — rather than a heavy opaque slab.
 *
 * UNTESTED — verify on device.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExtraKeyBar(
    ctrlStuck: Boolean,
    altStuck: Boolean,
    onIntent: (InputIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var activePopup: ExtraKey.Special? by remember { mutableStateOf(null) }

    val barShape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(barShape)
            .background(IrisSurfaceVariant.copy(alpha = 0.78f))
            .padding(vertical = 6.dp),
    ) {
        ExtraKeyBarLayout.rows.forEachIndexed { index, row ->
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 4.dp,
                        vertical = if (index == 0) 2.dp else 2.dp,
                    ),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                maxItemsInEachRow = row.size,
            ) {
                row.forEach { key ->
                    val stuck = when (key) {
                        ExtraKey.Special.CTRL -> ctrlStuck
                        ExtraKey.Special.ALT -> altStuck
                        else -> false
                    }
                    ExtraKeyButton(
                        key = key,
                        stuckActive = stuck,
                        onTap = { onIntent(key.toSingleIntent()) },
                        onLongPress = {
                            if (key is ExtraKey.Special) {
                                activePopup = key
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    activePopup?.let { modifierKey ->
        ModifierPopup(
            modifier = modifierKey,
            onComboSelected = { intents -> intents.forEach(onIntent) },
            onDismiss = { activePopup = null },
        )
    }
}

private fun ExtraKey.toSingleIntent(): InputIntent = when (this) {
    is ExtraKey.Special -> InputIntent.ArmModifier(this)
    is ExtraKey.Text -> InputIntent.TypeChar(glyph.first())
    is ExtraKey.Navigation -> InputIntent.Navigate(this)
}
