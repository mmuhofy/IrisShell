package com.iris.irisshell.ui.block

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted

@Composable
fun BlockInputField(
    onSubmit: (String) -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    var focused by remember { mutableStateOf(false) }

    val borderWidth by animateFloatAsState(
        targetValue = if (focused) 1.5f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "input-border-width",
    )
    val surfaceAlpha by animateFloatAsState(
        targetValue = if (focused) 1f else 0.92f,
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
                .background(
                    color = if (focused) IrisPrimary.copy(alpha = 0.1f) else androidx.compose.ui.graphics.Color.Transparent,
                    shape = RoundedCornerShape(24.dp),
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
                modifier = Modifier.padding(start = 10.dp, end = 6.dp),
            )
            Box(modifier = Modifier.weight(1f)) {
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
                    color = IrisTextMuted.copy(alpha = 0.6f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
