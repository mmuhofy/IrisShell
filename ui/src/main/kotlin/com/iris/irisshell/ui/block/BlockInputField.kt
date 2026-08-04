package com.iris.irisshell.ui.block

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted

/**
 * Floating capsule input field for the Block Mode terminal.
 *
 * Visual: a horizontally-centered pill-shaped field that floats above
 * the terminal area. Renders an opaque surface so it stays readable
 * over the running block cards; uses an IrisPrimary border when focused
 * for a clear focus affordance. The submit arrow `▸` replaces the
 * static `$` prompt marker once the field is focused.
 */
@Composable
fun BlockInputField(
    onSubmit: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    var focused by remember { mutableStateOf(false) }

    val borderColor = if (focused) IrisPrimary else IrisSurfaceVariant
    val borderWidth = if (focused) 1.5.dp else 1.dp
    val borderAlpha by animateFloatAsState(
        targetValue = if (focused) 1f else 0.6f,
        animationSpec = tween(durationMillis = 200),
        label = "input-border-alpha",
    )
    val surfaceAlpha by animateFloatAsState(
        targetValue = if (focused) 1f else 0.85f,
        animationSpec = tween(durationMillis = 200),
        label = "input-surface-alpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .alpha(surfaceAlpha)
                .background(IrisSurface.copy(alpha = surfaceAlpha), RoundedCornerShape(24.dp))
                .border(borderWidth, borderColor.copy(alpha = borderAlpha), RoundedCornerShape(24.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Prompt marker dot — left accent.
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .width(4.dp)
                    .background(IrisPrimary, CircleShape),
            )
            Text(
                text = if (focused) "iris ▸" else "iris$",
                color = IrisPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 10.dp),
            )
            Box(modifier = Modifier.weight(1f).padding(start = 6.dp)) {
                BasicTextField(
                    value = text,
                    onValueChange = { if (enabled) text = it },
                    enabled = enabled,
                    textStyle = TextStyle(
                        color = IrisText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                    ),
                    cursorBrush = SolidColor(IrisPrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focused = it.isFocused },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (text.isNotBlank()) {
                                onSubmit(text)
                                text = ""
                            }
                        },
                    ),
                    decorationBox = { inner ->
                        if (text.isEmpty()) {
                            Text(
                                text = if (enabled) "type a command…" else "block mode disabled",
                                color = IrisTextMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                            )
                        }
                        inner()
                    },
                )
            }
            if (text.isNotEmpty()) {
                Text(
                    text = "${text.length}",
                    color = IrisTextMuted.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .alpha(if (focused) 1f else 0.5f),
                )
            }
        }
    }
}
