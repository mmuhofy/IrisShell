package com.iris.irisshell.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.design.system.IrisError
import com.iris.irisshell.design.system.IrisOutline
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisSurfaceContainerLowest
import com.iris.irisshell.design.system.IrisSurfaceHigh
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextDisabled
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.design.system.IrisWarning
import com.iris.irisshell.design.system.OutfitFontFamily
import androidx.compose.ui.graphics.Color
import com.iris.irisshell.ui.IrisIcons

@Composable
fun SettingsSection(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = label.uppercase(),
            color = IrisTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            fontFamily = OutfitFontFamily,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        content()
    }
}

@Composable
fun SettingsSectionContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(IrisSurface),
    ) {
        content()
    }
}

@Composable
fun SettingsSubRow(
    icon: ImageVector,
    label: String,
    description: String? = null,
    iconTint: Color = IrisPrimary,
    trailing: @Composable () -> Unit,
) {
    val bgTint = if (iconTint == IrisError) IrisError.copy(alpha = 0.12f) else IrisPrimary.copy(alpha = 0.12f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(100))
                .background(bgTint),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                color = IrisText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = OutfitFontFamily,
            )
            if (description != null) {
                Text(
                    text = description,
                    color = IrisTextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 1.dp),
                    fontFamily = OutfitFontFamily,
                )
            }
        }
        trailing()
    }
}

@Composable
fun TerminalModeRow(
    useBlockEngine: Boolean,
    onSelect: (Boolean) -> Unit,
) {
    SettingsSubRow(
        icon = IrisIcons.Terminal,
        label = "Terminal Mode",
    ) {
        SegmentControl(
            options = listOf("Classic", "Block"),
            selectedIndex = if (useBlockEngine) 1 else 0,
            onSelect = { onSelect(it == 1) },
        )
    }
}

@Composable
private fun SegmentControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val trackHeight = 32.dp
    Row(
        modifier = modifier
            .height(trackHeight)
            .background(IrisSurfaceHigh, RoundedCornerShape(16.dp))
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEachIndexed { index, opt ->
            val isSelected = index == selectedIndex
            val animColor by animateColorAsState(
                targetValue = if (isSelected) IrisPrimary else Color.Transparent,
                animationSpec = tween(200),
                label = "segment_color_$index",
            )
            val animTextColor by animateColorAsState(
                targetValue = if (isSelected) Color(0xFF14171B) else IrisTextSecondary,
                animationSpec = tween(200),
                label = "segment_text_$index",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(100))
                    .background(animColor)
                    .clickable(
                        onClick = { onSelect(index) },
                    )
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = opt,
                    color = animTextColor,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    fontFamily = OutfitFontFamily,
                )
            }
        }
    }
}

@Composable
fun CursorSegmentedControl(
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    val selectedIndex = options.indexOf(selected).coerceAtLeast(0)
    SegmentControl(
        options = options,
        selectedIndex = selectedIndex,
        onSelect = { onSelect(options[it]) },
    )
}

@Composable
fun TerminalPreviewCard() {
    val lines = listOf(
        Triple("user@irisshell ~ %", "neofetch", true),
        Triple("OS:", " Iris Linux aarch64 (POSIX)", false),
        Triple("Shell:", " zsh 5.9", false),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(IrisSurfaceContainerLowest)
            .border(1.dp, IrisOutline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(14.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            listOf(IrisError, IrisWarning, IrisPrimary).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(RoundedCornerShape(100)),
                    contentAlignment = Alignment.Center,
                ) {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawCircle(color = color, radius = 5f)
                    }
                }
            }
        }

        lines.forEach { (prompt, output, _) ->
            val text = buildAnnotatedString {
                withStyle(SpanStyle(color = IrisPrimary, fontWeight = FontWeight.Medium)) {
                    append(prompt)
                }
                withStyle(SpanStyle(color = IrisText)) {
                    append(output)
                }
            }
            Text(
                text = text,
                fontSize = 12.sp,
                fontFamily = OutfitFontFamily,
                color = Color.Unspecified,
                lineHeight = 16.sp,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        ) {
            Text(
                text = "user@irisshell ~ %",
                color = IrisPrimary,
                fontSize = 12.sp,
                fontFamily = OutfitFontFamily,
            )
            var cmdText by rememberSaveable { mutableStateOf("") }
            Text(
                text = cmdText,
                color = IrisText,
                fontSize = 12.sp,
                fontFamily = OutfitFontFamily,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp),
            )
            BlinkingCursor(visible = true, rateMs = 500)
        }
    }
}

@Composable
fun BlinkingCursor(visible: Boolean, rateMs: Int) {
    if (visible) {
        var isVisible by remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            while (isActive) {
                delay(rateMs.toLong())
                isVisible = !isVisible
            }
        }
        val color = if (isVisible) IrisPrimary else Color.Transparent
        Canvas(modifier = Modifier.size(8.dp, 14.dp)) {
            drawRoundRect(
                color = color,
                size = Size(8f, 14f),
                cornerRadius = CornerRadius(2f),
            )
        }
    }
}

@Composable
fun BlinkRateSlider(
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "Slow",
            color = IrisTextSecondary,
            fontSize = 12.sp,
            fontFamily = OutfitFontFamily,
        )
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 150f..1000f,
            steps = 16,
            colors = SliderDefaults.colors(
                thumbColor = IrisBackground,
                activeTrackColor = IrisPrimary,
                inactiveTrackColor = IrisSurfaceHigh,
                activeTickColor = Color.Transparent,
                inactiveTickColor = Color.Transparent,
            ),
            modifier = Modifier
                .weight(1f)
                .height(20.dp),
        )
        Text(
            text = "Fast",
            color = IrisTextSecondary,
            fontSize = 12.sp,
            fontFamily = OutfitFontFamily,
        )
        Text(
            text = "${value} ms",
            color = IrisPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = OutfitFontFamily,
            modifier = Modifier
                .background(IrisSurfaceHigh, RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun FontSizeStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        IconButton(
            onClick = { if (value > 10) onValueChange(value - 1) },
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(IrisSurfaceHigh),
        ) {
            Icon(
                imageVector = IrisIcons.Minus,
                contentDescription = "Decrease font size",
                tint = IrisTextSecondary,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = "${value}sp",
            color = IrisPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = OutfitFontFamily,
            modifier = Modifier
                .background(IrisSurfaceHigh, RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
        IconButton(
            onClick = { if (value < 24) onValueChange(value + 1) },
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(IrisSurfaceHigh),
        ) {
            Icon(
                imageVector = IrisIcons.Plus,
                contentDescription = "Increase font size",
                tint = IrisTextSecondary,
                modifier = Modifier.size(14.dp),
            )
        }
    }
}

@Composable
fun ProotCommandDisplay(
    command: String,
) {
    Text(
        text = command,
        color = IrisPrimary,
        fontSize = 12.sp,
        fontFamily = OutfitFontFamily,
        modifier = Modifier
            .background(IrisSurfaceContainerLowest, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

@Composable
fun SettingsToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) IrisPrimary else IrisSurfaceHigh,
        animationSpec = tween(200),
        label = "toggle_track",
    )
    val thumbOffset by animateFloatAsState(
        targetValue = if (checked) 18f else 2f,
        animationSpec = tween(200),
        label = "toggle_thumb_offset",
    )

    Box(
        modifier = Modifier
            .size(44.dp, 26.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(trackColor)
            .clickable(
                onClick = { onCheckedChange(!checked) },
            )
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .offset(x = thumbOffset.dp)
                .clip(RoundedCornerShape(100)),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.size(22.dp)) {
                drawCircle(color = Color.White, radius = 10f)
            }
        }
    }
}

@Composable
fun SettingsNavigationRow(
    icon: ImageVector,
    label: String,
    trailingText: String? = null,
    trailingBadge: String? = null,
    showTrailingIcon: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(100))
                .background(IrisSurfaceHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = IrisTextSecondary,
                modifier = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        if (label.isNotBlank()) {
            Text(
                text = label,
                color = IrisText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = OutfitFontFamily,
                modifier = Modifier.weight(1f),
            )
        }
        if (trailingBadge != null) {
            Text(
                text = trailingBadge,
                color = IrisPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = OutfitFontFamily,
                modifier = Modifier
                    .background(IrisPrimary.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = IrisIcons.ArrowRight,
                contentDescription = null,
                tint = IrisTextDisabled,
                modifier = Modifier.size(16.dp),
            )
        } else if (trailingText != null) {
            Text(
                text = trailingText,
                color = IrisTextSecondary,
                fontSize = 15.sp,
                fontFamily = OutfitFontFamily,
            )
            if (showTrailingIcon) {
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = IrisIcons.ArrowRight,
                    contentDescription = null,
                    tint = IrisTextDisabled,
                    modifier = Modifier.size(16.dp),
                )
            }
        } else if (showTrailingIcon) {
            Icon(
                imageVector = IrisIcons.ArrowRight,
                contentDescription = null,
                tint = IrisTextDisabled,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
