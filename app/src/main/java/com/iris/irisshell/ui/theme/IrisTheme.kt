package com.iris.irisshell.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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

private val IrisTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 12.sp,
    ),
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
