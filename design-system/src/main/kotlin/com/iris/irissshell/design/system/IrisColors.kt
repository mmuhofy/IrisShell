package com.iris.irissshell.design.system

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
 *   - Background #0C0C0C, Surface #141414, SurfaceVariant #1A1A1A
 *   - Outline #232323, BorderSubtle #1E1E1E
 *   - Primary (warm gold) #E8C547, OnPrimary #000000
 *   - Text #EEEEEE, TextSecondary #888888, TextMuted #666666, TextDisabled #444444
 *   - Success #27AE60, Error #C0392B, Warning #C9A84C, Build #4A90E2
 *
 * Iris Shell is dark-only on v1.0 — OLED mode (#000000) lives in settings
 * and toggles the runtime colors, not these constants.
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
