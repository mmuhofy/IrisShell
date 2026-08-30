package com.iris.irisshell.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.design.system.IrisTextSecondary

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        color = IrisTextMuted,
        fontSize = 11.sp,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
    )
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IrisSurfaceVariant, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = IrisText, fontSize = 15.sp)
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(text = subtitle, color = IrisTextSecondary, fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = IrisPrimary,
                checkedTrackColor = IrisPrimary.copy(alpha = 0.4f),
            ),
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = IrisTextSecondary, fontSize = 13.sp)
        Text(text = value, color = IrisText, fontSize = 13.sp)
    }
}

@Composable
fun SettingsSliderRow(
    title: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    unit: String = "",
) {
    import androidx.compose.material3.Slider
    import androidx.compose.material3.SliderDefaults
    import com.iris.irisshell.design.system.IrisPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IrisSurfaceVariant, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = IrisText, fontSize = 15.sp)
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(
                text = "$value$unit",
                color = IrisTextSecondary,
                fontSize = 12.sp,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = IrisPrimary,
                activeTrackColor = IrisPrimary,
                inactiveTrackColor = IrisPrimary.copy(alpha = 0.2f),
            ),
        )
    }
}

@Composable
fun SettingsDropdownRow(
    title: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
) {
    import androidx.compose.material3.DropdownMenu
    import androidx.compose.material3.DropdownMenuItem
    import androidx.compose.material3.Icon
    import androidx.compose.material3.icons.Icons
    import androidx.compose.material3.icons.filled.ArrowDropDown
    import androidx.compose.runtime.mutableStateOf
    import androidx.compose.runtime.remember
    import androidx.compose.runtime.setValue
    import androidx.compose.runtime.getValue

    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IrisSurfaceVariant, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = IrisText, fontSize = 15.sp)
            Spacer(modifier = Modifier.padding(top = 4.dp))
            Text(
                text = selectedValue,
                color = IrisTextSecondary,
                fontSize = 12.sp,
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Select",
                tint = IrisTextSecondary,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = IrisText) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
