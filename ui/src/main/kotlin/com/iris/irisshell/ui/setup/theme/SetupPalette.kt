package com.iris.irisshell.ui.setup.theme

import androidx.compose.ui.graphics.Color

/**
 * Setup-screen specific design tokens.
 *
 * Values mirror MEMORYBANK.md §5 — Visual Identity. We duplicate them here
 * (rather than depend on `:app`'s `IrisTheme`) so `:ui` stays free of
 * cross-module dependencies per AGENT.md §110-139.
 *
 * Once the `:design-system` consolidation lands, these tokens should move to
 * that module and the inlined duplicates here will be deleted.
 */
internal object SetupPalette {
    val Background = Color(0xFF0C0C0C)
    val Surface = Color(0xFF141414)
    val SurfaceVariant = Color(0xFF1A1A1A)
    val Outline = Color(0xFF232323)
    val BorderSubtle = Color(0xFF1E1E1E)

    val Primary = Color(0xFFE8C547)
    val OnPrimary = Color(0xFF000000)

    val Text = Color(0xFFEEEEEE)
    val TextSecondary = Color(0xFF888888)
    val TextMuted = Color(0xFF666666)
    val TextDisabled = Color(0xFF444444)

    val Success = Color(0xFF27AE60)
    val Error = Color(0xFFC0392B)
    val Warning = Color(0xFFC9A84C)
    val MonoLog = Color(0xFFB0B0B0)

    val PulseHalo = Color(0x40E8C547)
    val PulseHaloStrong = Color(0x80E8C547)
}
