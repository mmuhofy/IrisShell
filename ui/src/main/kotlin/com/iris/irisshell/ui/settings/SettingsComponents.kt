package com.iris.irisshell.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisBackground
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisOutline
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurface
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.ui.R

// ── Section label ───────────────────────────────────────────────────────────────

@Composable
fun SettingsSectionLabel(text: String) {
    Text(
        text          = text.uppercase(),
        color         = IrisPrimary,
        fontSize      = 11.sp,
        fontWeight    = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        modifier      = Modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}

// ── Card container ──────────────────────────────────────────────────────────────
// Surface(border=...) kullanılır — LazyColumn 0-width'de Material3 Surface'ın
// kendi drawBehind'ını kullanır; Modifier.border() geçilmez.

@Composable
fun SettingsCategoryCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier  = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        shape     = RoundedCornerShape(14.dp),
        color     = IrisSurface,
        border    = BorderStroke(1.dp, IrisBorderSubtle),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp),
        ) { content() }
    }
}

// ── Divider ─────────────────────────────────────────────────────────────────────

@Composable
fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(IrisOutline.copy(alpha = 0.4f)),
    )
}

// ── Settings row ─────────────────────────────────────────────────────────────────

@Composable
fun SettingsRow(
    iconRes     : Int,
    label       : String,
    description : String,
    onClick     : () -> Unit,
    modifier    : Modifier = Modifier,
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(IrisPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter            = painterResource(iconRes),
                contentDescription = null,
                tint               = IrisPrimary,
                modifier           = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label,       color = IrisText,          fontSize = 15.sp)
            Text(text = description, color = IrisTextSecondary, fontSize = 12.sp,
                modifier = Modifier.padding(top = 1.dp))
        }
        Icon(
            painter            = painterResource(R.drawable.lucide_chevron_down),
            contentDescription = null,
            tint               = IrisTextMuted,
            modifier           = Modifier.size(14.dp),
        )
    }
}

// ── Toggle row ───────────────────────────────────────────────────────────────────

@Composable
fun SettingsToggleRow(
    iconRes         : Int,
    label           : String,
    description     : String,
    checked         : Boolean,
    onCheckedChange : (Boolean) -> Unit,
    modifier        : Modifier = Modifier,
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = { onCheckedChange(!checked) })
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier         = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(IrisPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter            = painterResource(iconRes),
                contentDescription = null,
                tint               = IrisPrimary,
                modifier           = Modifier.size(16.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label,       color = IrisText,          fontSize = 15.sp)
            Text(text = description, color = IrisTextSecondary, fontSize = 12.sp,
                modifier = Modifier.padding(top = 1.dp))
        }
        Switch(
            checked         = checked,
            onCheckedChange = onCheckedChange,
            colors          = SwitchDefaults.colors(
                checkedThumbColor    = IrisBackground,
                checkedTrackColor    = IrisPrimary,
                uncheckedThumbColor  = IrisTextMuted,
                uncheckedTrackColor  = IrisSurfaceVariant,
            ),
        )
    }
}

// ── Terminal Mode Card ──────────────────────────────────────────────────────────

@Composable
fun TerminalModeCard(
    useBlockEngine : Boolean,
    onSelect       : (Boolean) -> Unit,
    modifier       : Modifier = Modifier,
) {
    SettingsCategoryCard(modifier = modifier) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier         = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IrisPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter            = painterResource(R.drawable.lucide_terminal),
                    contentDescription = null,
                    tint               = IrisPrimary,
                    modifier           = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Terminal Modu", color = IrisText, fontSize = 15.sp)
                Text(
                    text     = if (useBlockEngine) "Block Engine" else "Classic",
                    color    = IrisPrimary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
        }

        SettingsDivider()

        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            TerminalModeOptionCard(
                label      = "Classic",
                isSelected = !useBlockEngine,
                onClick    = { onSelect(false) },
                modifier   = Modifier.weight(1f),
                preview    = { ClassicPreview() },
            )
            TerminalModeOptionCard(
                label      = "Block",
                isSelected = useBlockEngine,
                onClick    = { onSelect(true) },
                modifier   = Modifier.weight(1f),
                preview    = { BlockPreview() },
            )
        }
    }
}

// ── Terminal mode option card ────────────────────────────────────────────────────

@Composable
private fun TerminalModeOptionCard(
    label      : String,
    isSelected : Boolean,
    onClick    : () -> Unit,
    preview    : @Composable () -> Unit,
    modifier   : Modifier = Modifier,
) {
    val borderColor by animateColorAsState(
        targetValue   = if (isSelected) IrisPrimary else IrisOutline,
        animationSpec = tween(200),
        label         = "modeBorderColor",
    )
    val borderWidth by animateDpAsState(
        targetValue   = if (isSelected) 1.5.dp else 1.dp,
        animationSpec = tween(200),
        label         = "modeBorderWidth",
    )

    Surface(
        modifier     = modifier
            .widthIn(min = 1.dp)
            .heightIn(min = 1.dp)
            .clip(RoundedCornerShape(10.dp)),
        shape        = RoundedCornerShape(10.dp),
        color        = IrisSurfaceVariant,
        border       = BorderStroke(borderWidth, borderColor),
        onClick      = onClick,
    ) {
        Column(
            modifier            = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(IrisBackground)
                    .padding(8.dp),
            ) {
                preview()
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text       = label,
                color      = if (isSelected) IrisPrimary else IrisTextSecondary,
                fontSize   = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            )
        }
    }
}

// ── BlockPreview içinde border ──────────────────────────────────────────────────
// BlockPreview ve ClassicPreview içindeki Box'lar fixed-height (80.dp) içinde
// olduğundan 0-width olmaz. ama yine de BorderStroke → Surface çevirisiz
// border bırakıyorum (drawBehind risk yok çünkü height sabit).

@Composable
private fun BlockPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(min = 1.dp)
                .heightIn(min = 1.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(IrisSurfaceVariant)
                .border(1.dp, IrisOutline, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
        ) {
            TerminalPreviewLine("$ git status", IrisPrimary)
            TerminalPreviewLine("On branch main", IrisTextSecondary)
            Row(
                modifier              = Modifier.padding(top = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF27AE60).copy(alpha = 0.2f))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                ) {
                    Text("✓ 0", color = Color(0xFF27AE60), fontSize = 8.sp)
                }
                Text("12ms", color = IrisTextMuted, fontSize = 8.sp)
            }
        }
    }
}

@Composable
private fun ClassicPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        TerminalPreviewLine("$ ls -la",          IrisPrimary)
        TerminalPreviewLine("drwxr-xr-x  usr",   IrisTextSecondary)
        TerminalPreviewLine("-rw-r--r--  file",  IrisTextSecondary)
        TerminalPreviewLine("$ git status",      IrisPrimary)
        TerminalPreviewLine("On branch main",    IrisTextSecondary)
    }
}

@Composable
private fun TerminalPreviewLine(text: String, color: Color) {
    Text(
        text       = text,
        color      = color,
        fontSize   = 8.sp,
        fontFamily = FontFamily.Monospace,
        maxLines   = 1,
    )
}

// ── Font size slider row ────────────────────────────────────────────────────────

@Composable
fun FontSizeSliderRow(
    fontSizeSp   : Int,
    onSizeChange : (Int) -> Unit,
    modifier     : Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier         = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IrisPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter            = painterResource(R.drawable.lucide_a_large_small),
                    contentDescription = null,
                    tint               = IrisPrimary,
                    modifier           = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Font Boyutu", color = IrisText, fontSize = 15.sp)
                Text("Terminal metin büyüklüğü", color = IrisTextSecondary,
                    fontSize = 12.sp, modifier = Modifier.padding(top = 1.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text       = "${fontSizeSp}sp",
                color      = IrisPrimary,
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(Modifier.height(4.dp))
        Slider(
            value         = fontSizeSp.toFloat(),
            onValueChange = { onSizeChange(it.toInt()) },
            valueRange    = 10f..24f,
            steps         = 13,
            colors        = SliderDefaults.colors(
                thumbColor          = IrisPrimary,
                activeTrackColor    = IrisPrimary,
                inactiveTrackColor  = IrisOutline,
                activeTickColor     = Color.Transparent,
                inactiveTickColor   = Color.Transparent,
            ),
        )
    }
}

// ── Color picker row ────────────────────────────────────────────────────────────

@Composable
fun ColorPickerRow(
    iconRes     : Int,
    label       : String,
    description : String,
    options     : List<Color>,
    selectedHex : String,
    onSelect    : (String) -> Unit,
    modifier    : Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier         = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IrisPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter            = painterResource(iconRes),
                    contentDescription = null,
                    tint               = IrisPrimary,
                    modifier           = Modifier.size(16.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(text = label,       color = IrisText,          fontSize = 15.sp)
                Text(text = description, color = IrisTextSecondary, fontSize = 12.sp,
                    modifier = Modifier.padding(top = 1.dp))
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            options.forEach { color ->
                val hex        = colorToHex(color)
                val isSelected = hex.equals(selectedHex, ignoreCase = true)

                val ringColor by animateColorAsState(
                    targetValue   = if (isSelected) IrisPrimary else Color.Transparent,
                    animationSpec = tween(180),
                    label         = "colorRing",
                )

                Surface(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .padding(3.dp)
                        .clip(CircleShape),
                    shape   = CircleShape,
                    color   = color,
                    border  = BorderStroke(2.dp, ringColor),
                    onClick = { onSelect(hex) },
                ) {}
            }
        }
    }
}

// ── Info row ────────────────────────────────────────────────────────────────────

@Composable
fun InfoRow(
    label    : String,
    value    : String,
    modifier : Modifier = Modifier,
) {
    Row(
        modifier          = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = IrisTextSecondary, fontSize = 14.sp)
        Text(text = value, color = IrisText,          fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

fun colorToHex(color: Color): String {
    val r = (color.red   * 255).toInt()
    val g = (color.green * 255).toInt()
    val b = (color.blue  * 255).toInt()
    return "#%02X%02X%02X".format(r, g, b)
}
