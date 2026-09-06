package com.iris.irisshell.ui.setup.onboarding.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisOnPrimary
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisTextDisabled
import com.iris.irisshell.design.system.OutfitFontFamily

/**
 * Primary gold button used across onboarding scenes.
 *
 * Full-width by default, 52dp height, 24dp corner radius.
 * Disabled state uses IrisTextDisabled for the label (greyed).
 */
@Composable
fun SetupButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 52.dp,
    cornerRadius: Dp = 24.dp,
    fontSize: TextUnit = 15.sp,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(cornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) IrisPrimary else IrisTextDisabled.copy(alpha = 0.15f),
            contentColor = if (enabled) IrisOnPrimary else IrisTextDisabled,
            disabledContainerColor = IrisTextDisabled.copy(alpha = 0.15f),
            disabledContentColor = IrisTextDisabled,
        ),
        enabled = enabled,
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = fontSize,
                lineHeight = fontSize * 1.3,
            ),
        )
    }
}
