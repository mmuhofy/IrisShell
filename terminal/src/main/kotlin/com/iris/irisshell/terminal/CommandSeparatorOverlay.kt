// Inspired by: termux/termux-app — TerminalView rendering coordinate system
// Adapted for Iris Shell — com.iris.irisshell
//
// Overlay View that draws thin horizontal separator lines above prompt lines
// in block mode. Uses the TerminalRenderer's font metrics (mFontWidth,
// mFontLineSpacing, mFontLineSpacingAndAscent) to align lines with terminal
// cells, following the same coordinate system as SearchHighlightOverlay.
package com.iris.irisshell.terminal

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import com.termux.view.TerminalView

class CommandSeparatorOverlay(
    context: android.content.Context,
) : View(context, null) {

    var terminalView: TerminalView? = null

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = android.graphics.Color.parseColor("#30343A43")
        strokeWidth = 1f
    }

    private companion object {
        val PROMPT_SUFFIX_REGEX = Regex("""[#$❯➜]\s*$""")
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val view = terminalView ?: return
        val emulator = view.mEmulator ?: return

        val screen = emulator.getScreen()
        val columns = emulator.mColumns
        val rows = emulator.mRows
        val topRow = view.mTopRow

        val renderer = view.mRenderer ?: return
        val fontLineSpacing = renderer.mFontLineSpacing
        val fontAscent = renderer.mFontLineSpacingAndAscent - renderer.mFontLineSpacing

        val width = width.toFloat()

        for (visRow in 0 until rows) {
            val externalRow = topRow + visRow
            val internalRow = screen.externalToInternalRow(externalRow)

            val lineObject = screen.allocateFullLineIfNecessary(internalRow)
            val text = String(lineObject.mText, 0, lineObject.spaceUsed)
            if (text.isEmpty()) continue

            val trimmed = text.trimEnd('\r')
            if (trimmed.isEmpty()) continue

            // Detect prompt lines: must end with a prompt suffix character
            // ($ # ❯ ➜) and contain shell-like context (~ / @) to reduce
            // false positives from output text (e.g. `echo hi$`).
            val hasPromptSuffix = PROMPT_SUFFIX_REGEX.find(trimmed) != null
            val hasShellContext = trimmed.contains('@') || trimmed.contains('~') || trimmed.contains('/')
            if (!hasPromptSuffix || !hasShellContext) continue

            // Draw a thin line at the top of this prompt row.
            val y = visRow * fontLineSpacing

            canvas.drawLine(0f, y, width, y, linePaint)
        }
    }
}
