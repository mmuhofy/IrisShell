package com.iris.irissshell.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.iris.irissshell.design.system.IrisBackground
import com.iris.irissshell.design.system.IrisError
import com.iris.irissshell.design.system.IrisOnPrimary
import com.iris.irissshell.design.system.IrisOutline
import com.iris.irissshell.design.system.IrisPrimary
import com.iris.irissshell.design.system.IrisSurface
import com.iris.irissshell.design.system.IrisSurfaceVariant
import com.iris.irissshell.design.system.IrisTextSecondary
import com.iris.irissshell.design.system.OutfitFontFamily

/**
 * Compose theme wrapper. Color tokens live in `:design-system`
 * (see `com.iris.irissshell.design.system.IrisColors.kt`) so both `:app` and
 * `:ui` modules draw from the same source.
 *
 * Iris Shell is dark-only in v1.0 — we ignore the system dark/light switch so
 * the warm gold accent (#E8C547) and dark surfaces stay consistent.
 */
private val IrisDarkColors = darkColorScheme(
    primary = IrisPrimary,
    onPrimary = IrisOnPrimary,
    secondary = IrisTextSecondary,
    background = IrisBackground,
    surface = IrisSurface,
    surfaceVariant = IrisSurfaceVariant,
    outline = IrisOutline,
    error = IrisError,
    onError = IrisPrimary,
)

/**
 * Full Material 3 typography table bound to Outfit. The shape mirrors the
 * reference ReTerminal `Typography { ... }` block — same scale, weight
 * assignments, and sizes — so any title/body/label token feels at home in
 * a ReTerminal-style shell.
 */
private val IrisTypography = Typography(
    displayLarge = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp),
    displayMedium = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal, fontSize = 45.sp),
    displaySmall = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal, fontSize = 36.sp),
    headlineLarge = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp),
    headlineSmall = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    bodyLarge = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp),
    labelSmall = TextStyle(fontFamily = OutfitFontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp),
)

@Composable
fun IrisTheme(content: @Composable () -> Unit) {
    @Suppress("UNUSED_VARIABLE") val isDark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = IrisDarkColors,
        typography = IrisTypography,
        content = content,
    )
}
