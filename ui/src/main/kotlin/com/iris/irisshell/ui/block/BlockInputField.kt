package com.iris.irisshell.ui.block

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
fun BlockInputField(onSubmit: (String) -> Unit, modifier: Modifier = Modifier) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(IrisSurface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "iris$",
            color = IrisPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
        )
        BasicTextField(
            value = text,
            onValueChange = { text = it },
            textStyle = TextStyle(
                color = IrisText,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
            ),
            cursorBrush = SolidColor(IrisPrimary),
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
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
                        text = "type a command…",
                        color = IrisTextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                    )
                }
                inner()
            },
        )
    }
}
