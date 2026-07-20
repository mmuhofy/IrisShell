package com.iris.irisshell.terminal

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import com.termux.terminal.TerminalSession
import com.termux.view.TerminalView
import com.termux.view.TerminalViewClient

class TerminalViewClientImpl(
    private val onScaleChange: ((Float) -> Float)? = null,
    val extraKeyState: ExtraKeyState? = null,
    private val context: android.content.Context? = null
) : TerminalViewClient {

    var scrollLocked: Boolean = false
    var terminalView: TerminalView? = null
    var onCopyModeChanged: ((Boolean) -> Unit)? = null

    private val urlPattern = Regex(
        "((https?|ftp|file)://|www\\.)[-A-Za-z0-9+&@#/%?=~_|!:,.;]*[-A-Za-z0-9+&@#/%=~_|]",
        RegexOption.IGNORE_CASE
    )

    override fun onScale(scale: Float): Float {
        return onScaleChange?.invoke(scale) ?: 1.0f
    }

    override fun onSingleTapUp(e: MotionEvent) {
        val view = terminalView ?: return
        if (view.mEmulator == null) return
        val colRow = view.getColumnAndRow(e, false)
        val col = colRow[0]
        val row = colRow[1]
        if (col < 0 || row < 0) return
        val screen = view.mEmulator.getScreen() ?: return
        val word = screen.getWordAtLocation(col, row)
        if (word.isNullOrBlank()) return
        if (urlPattern.matches(word)) {
            var url = word
            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("ftp://")) {
                url = "https://$url"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context?.startActivity(intent)
        }
    }

    override fun shouldBackButtonBeMappedToEscape(): Boolean = false
    override fun shouldEnforceCharBasedInput(): Boolean = false
    override fun shouldUseCtrlSpaceWorkaround(): Boolean = false
    override fun isTerminalViewSelected(): Boolean = true

    override fun copyModeChanged(copyMode: Boolean) {
        onCopyModeChanged?.invoke(copyMode)
    }

    override fun onKeyDown(keyCode: Int, e: KeyEvent, session: TerminalSession): Boolean = false
    override fun onKeyUp(keyCode: Int, e: KeyEvent): Boolean = false
    override fun onLongPress(event: MotionEvent): Boolean = false

    override fun readControlKey(): Boolean = extraKeyState?.readCtrl() ?: false
    override fun readAltKey(): Boolean = extraKeyState?.readAlt() ?: false
    override fun readShiftKey(): Boolean = false
    override fun readFnKey(): Boolean = false

    override fun onCodePoint(codePoint: Int, ctrlDown: Boolean, session: TerminalSession): Boolean = false
    override fun onEmulatorSet() { }

    override fun logError(tag: String, message: String) { Log.e(tag, message) }
    override fun logWarn(tag: String, message: String) { Log.w(tag, message) }
    override fun logInfo(tag: String, message: String) { Log.i(tag, message) }
    override fun logDebug(tag: String, message: String) { Log.d(tag, message) }
    override fun logVerbose(tag: String, message: String) { Log.v(tag, message) }
    override fun logStackTraceWithMessage(tag: String, message: String, e: Exception) { Log.e(tag, message, e) }
    override fun logStackTrace(tag: String, e: Exception) { Log.e(tag, "", e) }
}
