package com.iris.irisshell.ui.setup.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import com.iris.irisshell.design.system.OutfitFontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.domain.terminal.BootstrapStep
import com.iris.irisshell.domain.terminal.StepState
import com.iris.irisshell.ui.setup.theme.SetupPalette

/**
 * A single row in the setup stepper.
 *
 * Renders: [StepStateIcon] → title + optional subLine + 1dp connector line.
 *
 * The connector line colour is animated: gold if this step is done, gold if
 * next step is active, otherwise muted.
 *
 * Per Iris style — minimal, no card chrome. Each step is just text + a marker.
 *
 * @param showConnector  false for the last row to suppress trailing connector.
 */
@Composable
fun StepRow(
    step: BootstrapStep,
    title: String,
    subLine: String?,
    state: StepState,
    showConnector: Boolean,
    modifier: Modifier = Modifier,
) {
    val connectorAlpha by animateFloatAsState(
        targetValue = if (state == StepState.Done || state == StepState.Active) 1f else 0.35f,
        animationSpec = tween(420),
        label = "connector-alpha",
    )

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp),
        ) {
            StepStateIcon(state = state)
            if (showConnector) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(34.dp)
                        .background(
                            color = if (state == StepState.Done)
                                SetupPalette.Primary.copy(alpha = connectorAlpha)
                            else
                                SetupPalette.TextDisabled.copy(alpha = connectorAlpha),
                        ),
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .padding(top = 2.dp, bottom = 18.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                color = when (state) {
                    StepState.Pending -> SetupPalette.TextMuted
                    StepState.Active -> SetupPalette.Primary
                    StepState.Done -> SetupPalette.Text
                    StepState.Failed -> SetupPalette.Error
                },
                style = TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontSize = 15.sp,
                    fontWeight = when (state) {
                        StepState.Active -> FontWeight.SemiBold
                        else -> FontWeight.Medium
                    },
                ),
            )
            if (!subLine.isNullOrBlank() && state != StepState.Pending) {
                Text(
                    text = subLine,
                    color = SetupPalette.TextSecondary,
                    style = TextStyle(
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                    ),
                    maxLines = 2,
                )
            }
        }
    }
}

/** Default no-op spacer for symmetry in stacks. */
@Suppress("unused")
@Composable
internal fun Dot(
    color: Color = SetupPalette.TextDisabled,
    size: androidx.compose.ui.unit.Dp = 4.dp,
) {
    Box(modifier = Modifier.size(size).background(color))
}
