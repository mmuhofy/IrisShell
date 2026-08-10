package com.iris.irisshell.ui.input

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.domain.input.ExtraKey
import com.iris.irisshell.domain.input.ExtraKeyBarLayout
import com.iris.irisshell.domain.input.InputIntent

/**
 * The 2-row on-screen bar — see [ExtraKeyBarLayout] for the default
 * layout. Renders one [ExtraKeyButton] per key, dispatching [onIntent]
 * on tap (translated from the key) and showing a [ModifierPopup] on
 * long-press of CTRL/ALT.
 *
 * The bar announces [ctrlStuck]/[altStuck] so modifier buttons render
 * the sticky highlight — these come from the owning
 * [InputBarViewModel], which reads them off `ExtraKeyState` (the
 * shared modifier container).
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(IrisSurfaceVariant),
    ) {
        ExtraKeyBarLayout.rows.forEach { row ->
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
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
