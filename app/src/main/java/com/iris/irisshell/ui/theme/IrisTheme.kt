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
 *  - Background: #0C0C0C, Surface: #141414
 *  - Primary (warm gold): #E8C547
 *  - Text: #EEEEEE, secondary #888888, muted #666666, disabled #444444
 *  - Success: #27AE60, Error: #C0392B, Warning: #C9A84C
 *  - Build/Compile: #4A90E2
 *
 * Iris Shell is dark-only on v1.0 — OLED mode (full black #000000) is a Settings
 * toggle in Phase 1. The toggle lives in :data's Preferences.
 *
 * Typography is sourced from Outfit Regular (bundled TTF at
 * `res/font/outfit_regular.ttf`). Originally lifted from
 * `com.rk.terminal.ui.theme.OutfitFontFamily` in
 * https://github.com/RohitKushvaha01/ReTerminal so the entire app — top bar,
 * setup story, terminal chrome — shares the same letterforms as the host
 * shell.
 */
val IrisBackground: Color = Color(0xFF0C0C0C)
val IrisSurface: Color = Color(0xFF141414)
val IrisSurfaceVariant: Color = Color(0xFF1A1A1A)
val IrisOutline: Color = Color(0xFF232323)
val IrisBorderSubtle: Color = Color(0xFF1E1E1E)

val IrisPrimary: Color = Color(0xFFE8C547)
val IrisOnPrimary: Color = Color(0xFF000000)

val IrisText: Color = Color(0xFFEEEEEE)
val IrisTextSecondary: Color = Color(0xFF888888)
val IrisTextMuted: Color = Color(0xFF666666)
val IrisTextDisabled: Color = Color(0xFF444444)

val IrisSuccess: Color = Color(0xFF27AE60)
val IrisError: Color = Color(0xFFC0392B)
val IrisWarning: Color = Color(0xFFC9A84C)
val IrisBuild: Color = Color(0xFF4A90E2)

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
 * the warm gold accent (#E8C547) and dark surfaces stay consistent.
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
