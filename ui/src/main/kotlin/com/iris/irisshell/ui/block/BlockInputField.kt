package com.iris.irisshell.ui.block

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import com.composables.icons.lucide.Lucide.ArrowUp
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisBorderSubtle
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

    val borderColor by animateFloatAsState(
        targetValue = if (focused) 1f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "input-border-alpha",
    )
    val surfaceAlpha by animateFloatAsState(
        targetValue = if (focused) 1f else 0.92f,
        animationSpec = tween(durationMillis = 180),
        label = "input-surface-alpha",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(IrisSurface.copy(alpha = 0.6f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(
                    color = IrisSurface.copy(alpha = surfaceAlpha),
                    shape = RoundedCornerShape(10.dp),
                )
                .border(
                    width = 1.dp,
                    color = IrisPrimary.copy(alpha = borderColor),
                    shape = RoundedCornerShape(10.dp),
                )
                .padding(start = 14.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(16.dp)
                    .background(IrisPrimary),
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
            if (text.isNotBlank() && enabled) {
                IconButton(
                    onClick = {
                        onSubmit(text)
                        text = ""
                    },
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Lucide.ArrowUp,
                        contentDescription = "Send",
                        tint = IrisPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            } else {
                Box(modifier = Modifier.size(32.dp))
            }
        }
    }
}
