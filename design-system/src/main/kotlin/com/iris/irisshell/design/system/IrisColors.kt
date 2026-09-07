package com.iris.irisshell.design.system

import androidx.compose.ui.graphics.Color

/**
 * Design tokens — the Iris Shell palette.
 *
 * Originally defined inside `:app/.../ui/theme/IrisTheme.kt` (Phase 2), the
 * tokens are promoted to `:design-system` so every module that renders
 * Compose — `:app`, `:ui`, and future `:agent` HUDs — sees the exact same
 * hex values without duplicating them. Per MEMORYBANK.md §5 — Visual
 * Identity:
 *
 *   - Background #000000 (OLED), Surface #0A0A0A, SurfaceVariant #121212
 *   - Outline #252525, BorderSubtle #1E1E1E
 *   - Primary (terminal blue) #3B82F6, OnPrimary #050505
 *   - Text #E8E8E8, TextSecondary #A0A0A0, TextMuted #787878, TextDisabled #585858
 *   - Success #22C55E, Error #EF4444, Warning #F59E0B, Build #3B82F6
 *
 * Iris Shell is dark-only on v1.0 — OLED mode (#000000) is the default
 * background. Surface levels use 10-nit increments for clear visual hierarchy.
 * The blue accent (#3B82F6) replaces the previous gold, evoking terminal cursor cyan
 * and VS Code's professional dark theme.
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
