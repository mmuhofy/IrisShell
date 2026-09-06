package com.iris.irisshell.ui.setup.onboarding.components

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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisError
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSuccess
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.design.system.IrisWarning
import com.iris.irisshell.design.system.OutfitFontFamily

/**
 * Status icon color for [DeviceCheckItem].
 */
enum class CheckStatus {
    Ok,
    Warn,
    Error,
    Unknown,
}

/**
 * Single row in the device-check list — icon, label, value, and status dot.
 *
 * Example layout:
 *
 *   ✓  Mimari    arm64-v8a
 *   ✓  Android   14 (API 34)
 *   ⚠  Pil       %23
 *   ✗  Depolama  3 GB — yeterli değil
 */
@Composable
fun DeviceCheckItem(
    icon: Painter,
    label: String,
    value: String,
    status: CheckStatus,
    modifier: Modifier = Modifier,
    iconSize: Dp = 20.dp,
    fontSize: TextUnit = 14.sp,
) {
    val statusColor = when (status) {
        CheckStatus.Ok -> IrisSuccess
        CheckStatus.Warn -> IrisWarning
        CheckStatus.Error -> IrisError
        CheckStatus.Unknown -> IrisTextMuted
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = IrisText,
            modifier = Modifier.size(iconSize),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = fontSize,
                    lineHeight = fontSize * 1.4,
                ),
                color = IrisText,
            )
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                ),
                color = IrisTextMuted,
            )
        }
        Box(
            modifier = Modifier
                .size(8.dp),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = statusColor)
            }
        }
    }
}
