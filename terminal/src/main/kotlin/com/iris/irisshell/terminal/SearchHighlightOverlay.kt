// Inspired by: termux/termux-app — TerminalView rendering coordinate system
// Adapted for Iris Shell — com.iris.irisshell
//
// Overlay View that draws semi-transparent search-highlight rectangles on top
// of a classic TerminalView. Uses the TerminalRenderer's font metrics (mFontWidth,
// mFontLineSpacing, mFontAscent) and TerminalView's mTopRow to align highlights
// with terminal cells.
package com.iris.irisshell.terminal

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import com.termux.view.TerminalView

class SearchHighlightOverlay(
    context: android.content.Context,
) : View(context, null) {

    var terminalView: TerminalView? = null
    var searchQuery: String? = null

    private val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = android.graphics.Color.parseColor("#803B82F6")
    }

    private val rect = Rect()

    fun updateQuery(query: String?) {
        searchQuery = query
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val view = terminalView ?: return
        val emulator = view.mEmulator ?: return
        val renderer = view.mRenderer ?: return
        val query = searchQuery
        if (query.isNullOrEmpty()) return

        val screen = emulator.getScreen()
        val columns = emulator.mColumns
        val rows = emulator.mRows
        val topRow = view.mTopRow

        val fontWidth = renderer.mFontWidth
        val fontLineSpacing = renderer.mFontLineSpacing
        val fontAscent = renderer.mFontAscent

        val lowerQuery = query.lowercase()

        for (visRow in 0 until rows) {
            val externalRow = topRow + visRow
            val internalRow = screen.externalToInternalRow(externalRow)

            val lineObject = screen.allocateFullLineIfNecessary(internalRow)
            val text = String(lineObject.mText, 0, lineObject.spaceUsed)

            if (text.isEmpty()) continue

            val lowerText = text.lowercase()
            var start = 0
            while (true) {
                val idx = lowerText.indexOf(lowerQuery, start)
                if (idx == -1) break

                val matchEnd = idx + query.length

                val colStart = idx.coerceAtMost(columns - 1)
                val colEnd = matchEnd.coerceAtMost(columns)

                val x1 = (colStart * fontWidth).toInt()
                val x2 = (colEnd * fontWidth).toInt()
                rect.left = x1
                rect.right = x2
                rect.top = (visRow + 1) * fontLineSpacing + fontAscent
                rect.bottom = rect.top + fontLineSpacing

                canvas.drawRect(rect, highlightPaint)

                start = matchEnd
            }
        }
    }
}
