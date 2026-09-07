package com.iris.irisshell.ui.input

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.blur
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.domain.input.ExtraKey
import com.iris.irisshell.domain.input.InputIntent

private val PILL_SHAPE = RoundedCornerShape(26.dp)

/**
 * Drosh floating Liquid Glass extra-key bar.
 *
 * One horizontal floating pill, visually matching the HTML reference:
 * translucent dark glass, ambient color clusters, soft highlight, subtle
 * shadow and responsive key sizing. The second-level keys live in a compact
 * glass panel above the pill.
 */
@Composable
fun ExtraKeyBar(
    ctrlStuck: Boolean,
    altStuck: Boolean,
    onIntent: (InputIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var activePopup: ExtraKey.Special? by remember { mutableStateOf(null) }
    var moreOpen by remember { mutableStateOf(false) }

    val pillAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "pillAlpha",
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(
            visible = moreOpen,
            enter = fadeIn(tween(150)) + scaleIn(
                initialScale = 0.98f,
                animationSpec = tween(180, easing = FastOutSlowInEasing),
            ),
            exit = fadeOut(tween(110)) + scaleOut(
                targetScale = 0.98f,
                animationSpec = tween(120),
            ),
        ) {
            MoreKeysPanel(
                onIntent = onIntent,
                onDismiss = { moreOpen = false },
            )
        }

        AnimatedVisibility(
            visible = activePopup != null && !moreOpen,
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(100)),
        ) {
            activePopup?.let { modifierKey ->
                ModifierPopup(
                    modifier = modifierKey,
                    onComboSelected = { intents -> intents.forEach(onIntent) },
                    onDismiss = { activePopup = null },
                )
            }
        }

        if (moreOpen) Spacer(Modifier.size(height = 8.dp, width = 1.dp))

        Box(
            modifier = Modifier
                .widthIn(max = 720.dp)
                .fillMaxWidth()
                .graphicsLayer { alpha = pillAlpha }
                .shadow(
                    elevation = 12.dp,
                    shape = PILL_SHAPE,
                    clip = false,
                )
                .clip(PILL_SHAPE),
        ) {
            LiquidGlassSurface()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                val row = listOf(
                    ExtraKey.Navigation.ESC,
                    ExtraKey.Special.CTRL,
                    ExtraKey.Special.ALT,
                    ExtraKey.Navigation.TAB,
                    ExtraKey.Navigation.ARROW_UP,
                    ExtraKey.Navigation.ARROW_DOWN,
                    ExtraKey.Navigation.ARROW_LEFT,
                    ExtraKey.Navigation.ARROW_RIGHT,
                )
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
                                moreOpen = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                MoreKeyButton(
                    expanded = moreOpen,
                    onClick = {
                        moreOpen = !moreOpen
                        activePopup = null
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MoreKeysPanel(
    onIntent: (InputIntent) -> Unit,
    onDismiss: () -> Unit,
) {
    val keys = listOf(
        "HOME" to InputIntent.Navigate(ExtraKey.Navigation.HOME),
        "END" to InputIntent.Navigate(ExtraKey.Navigation.END),
        "PGUP" to InputIntent.Navigate(ExtraKey.Navigation.PAGE_UP),
        "PGDN" to InputIntent.Navigate(ExtraKey.Navigation.PAGE_DOWN),
        "|" to InputIntent.TypeChar('|'),
        "~" to InputIntent.TypeChar('~'),
        "/" to InputIntent.TypeChar('/'),
        "\\" to InputIntent.TypeChar('\\'),
        "_" to InputIntent.TypeChar('_'),
        "-" to InputIntent.TypeChar('-'),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp)),
    ) {
        LiquidGlassPanelSurface()
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            keys.forEach { (label, intent) ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            onIntent(intent)
                            onDismiss()
                        },
                    contentAlignment = androidx.compose.ui.Alignment.Center,
                ) {
                    androidx.compose.material3.Text(
                        text = label,
                        color = Color(0xFFC9CCD2),
                        fontSize = 10.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    )
                }
            }
        }
    }
}

@Composable
private fun LiquidGlassPanelSurface() {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .blur(22.dp),
    ) {
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF252A31).copy(alpha = 0.50f),
                    Color(0xFF1E2024).copy(alpha = 0.46f),
                    Color(0xFF29232B).copy(alpha = 0.48f),
                ),
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(18.dp.toPx()),
        )
    }
}

@Composable
private fun MoreKeyButton(
    expanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .size(width = 48.dp, height = 38.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (expanded) Color.White.copy(alpha = 0.13f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        androidx.compose.material3.Text(
            text = "•••",
            color = Color(0xFFC9CCD2),
            fontSize = 17.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
        )
    }
}

@Composable
private fun LiquidGlassSurface() {
    // Compose cannot use CSS backdrop-filter. These ambient clusters are
    // deliberately painted inside the translucent surface to reproduce the
    // same iOS-style vibrancy impression without changing terminal rendering.
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .blur(26.dp),
    ) {
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF252A31).copy(alpha = 0.48f),
                    Color(0xFF1E2024).copy(alpha = 0.42f),
                    Color(0xFF29232B).copy(alpha = 0.46f),
                ),
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                x = 26.dp.toPx(),
                y = 26.dp.toPx(),
            ),
        )

        val w = size.width
        val h = size.height
        drawCircle(
            color = Color(0xFF31577A).copy(alpha = 0.13f),
            radius = h * 2.0f,
            center = androidx.compose.ui.geometry.Offset(-w * 0.08f, h * 0.45f),
        )
        drawCircle(
            color = Color(0xFF6A466E).copy(alpha = 0.11f),
            radius = h * 1.9f,
            center = androidx.compose.ui.geometry.Offset(w * 1.05f, -h * 0.35f),
        )
        drawCircle(
            color = Color(0xFF315E52).copy(alpha = 0.08f),
            radius = h * 1.65f,
            center = androidx.compose.ui.geometry.Offset(w * 0.90f, h * 1.30f),
        )

        // Specular top edge + subtle lower falloff.
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.095f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.08f),
                ),
            ),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                x = 26.dp.toPx(),
                y = 26.dp.toPx(),
            ),
        )
        drawRoundRect(
            color = Color.White.copy(alpha = 0.14f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                x = 26.dp.toPx(),
                y = 26.dp.toPx(),
            ),
        )
    }
}

private fun ExtraKey.toSingleIntent(): InputIntent = when (this) {
    is ExtraKey.Special -> InputIntent.ArmModifier(this)
    is ExtraKey.Text -> InputIntent.TypeChar(glyph.first())
    is ExtraKey.Navigation -> InputIntent.Navigate(this)
}
