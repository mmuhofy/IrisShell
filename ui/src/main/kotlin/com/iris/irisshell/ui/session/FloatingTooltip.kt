package com.iris.irisshell.ui.session

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisText

/**
 * Small floating pill that hovers above the user's pointer while in
 * long-press drag mode. Spring-animated enter/exit so it tracks the
 * finger without feeling laggy.
 *
 * The pill itself is just a [Text] with a tinted background and a
 * subtle shadow. Offset and positioning are controlled by the parent
 * — the tooltip is rendered at `Modifier.offset { (x, y) }` driven
 * by the pointer's last-known coordinates.
 */
@Composable
fun FloatingTooltip(
    text: String,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(initialScale = 0.7f),
        exit = fadeOut() + scaleOut(targetScale = 0.7f),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .shadow(6.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(IrisPrimary)
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = text,
                color = IrisSurface,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
