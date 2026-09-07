package com.iris.irisshell.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.iris.irisshell.design.system.OutfitFontFamily

/**
 * Iris Shell design tokens.
 *
 * Per MEMORYBANK.md §5 — Visual Identity:
 *  - Background: #000000 (OLED), Surface: #0A0A0A, SurfaceVariant: #121212
 *  - Primary (terminal blue): #3B82F6, OnPrimary: #050505
 *  - Text: #E8E8E8, secondary #A0A0A0, muted #787878, disabled #585858
 *  - Success: #22C55E, Error: #EF4444, Warning: #F59E0B
 *  - Build/Compile: #3B82F6 (matches accent)
 *
 * Iris Shell is dark-only in v1.0 — we ignore the system dark/light switch so
 * the blue accent (#3B82F6) and dark surfaces stay consistent.
 *
 * Typography is sourced from Outfit Regular (bundled TTF at
 * `res/font/outfit_regular.ttf`). Originally lifted from
 * `com.rk.terminal.ui.theme.OutfitFontFamily` in
 * https://github.com/RohitKushvaha01/ReTerminal so the entire app — top bar,
 * setup story, terminal chrome — shares the same letterforms as the host
 * shell.
 */
val IrisBackground: Color = Color(0xFF000000)
val IrisSurface: Color = Color(0xFF0A0A0A)
val IrisSurfaceVariant: Color = Color(0xFF121212)
val IrisOutline: Color = Color(0xFF252525)
val IrisBorderSubtle: Color = Color(0xFF1E1E1E)

val IrisPrimary: Color = Color(0xFF3B82F6)
val IrisOnPrimary: Color = Color(0xFF050505)

val IrisText: Color = Color(0xFFE8E8E8)
val IrisTextSecondary: Color = Color(0xFFA0A0A0)
val IrisTextMuted: Color = Color(0xFF787878)
val IrisTextDisabled: Color = Color(0xFF585858)

val IrisSuccess: Color = Color(0xFF22C55E)
val IrisError: Color = Color(0xFFEF4444)
val IrisWarning: Color = Color(0xFFF59E0B)
val IrisBuild: Color = Color(0xFF3B82F6)

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

/**
 * Compose theme for the entire app.
 *
 * Iris Shell is dark-only in v1.0 — we ignore the system dark/light switch so
 * the blue accent (#3B82F6) and dark surfaces stay consistent.
 */
@Composable
fun IrisTheme(content: @Composable () -> Unit) {
    // The system dark mode flag is intentionally ignored — Iris Shell mandate.
    @Suppress("UNUSED_VARIABLE") val isDark = isSystemInDarkTheme()
    MaterialTheme(
        colorScheme = IrisDarkColors,
        typography = IrisTypography,
        content = content,
    )
}
