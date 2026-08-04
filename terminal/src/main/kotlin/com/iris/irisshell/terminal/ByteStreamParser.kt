package com.iris.irisshell.terminal

import com.iris.irisshell.domain.terminal.ByteStreamEvent

/**
 * Stateful parser that consumes the raw PTY byte stream one byte at a
 * time and emits high-level [ByteStreamEvent]s.
 *
 * Tracks three substates of terminal activity:
 *
 *  - **Normal** — printable text accumulates into the current output
 *    line. A `\r`/`\n` flushes the buffered line.
 *  - **Escape** — last byte was `0x1B` (ESC). Either a CSI/DCS/OSC
 *    sequence follows (handled by sub-states) or it is a standalone
 *    two-byte escape.
 *  - **CSI / OSC / DCS** — within an escape sequence, parameter and
 *    intermediate bytes are swallowed. Final bytes (`@`..`~`) terminate
 *    the sequence and we return to Normal.
 *
 * Lines that arrive while the parser is inside a `TUI alternate-screen`
 * session (the shell emitted CSI ?1049h) are dropped — the entire
 * alternate screen is treated as a single TUI block. When the
 * alternate screen exits (CSI ?1049l) we emit [ByteStreamEvent.TuiExited]
 * and resume capturing lines.
 *
 * Prompt detection: a [ByteStreamEvent.PromptReady] is emitted every
 * time a line ends in a recognised shell prompt suffix (`$`, `#`,
 * `❯`, `➜`) **and** the parser is not currently in a TUI session.
 * The captured prompt text is the entire visible line minus the
 * trailing suffix.
 *
 * Why pure Kotlin / no Android imports:
 * The parser must be unit-testable without an emulator or Android
 * runtime — see [com.iris.irisshell.terminal.ByteStreamParserTest].
 */
class ByteStreamParser {

    /** Buffer holding the in-progress output line, in bytes. */
    private var lineBuffer = StringBuilder()

    /** Set while CSI ?1049h is active. */
    private var tuiActive = false

    /** Substate of the parser. */
    private var state: State = State.Normal
    private var escapeBuffer: StringBuilder = StringBuilder()
    private var csiParamBytes: StringBuilder = StringBuilder()

    /** Emitted events, drained by [drainEvents]. */
    private val pendingEvents = ArrayDeque<ByteStreamEvent>()

    fun feed(byte: Byte) {
        val b = byte.toInt() and 0xFF
        when (state) {
            State.Normal -> handleNormal(b)
            State.Escape -> handleEscape(b)
            State.Csi -> handleCsi(b)
            State.Osc -> handleOsc(b)
            State.Dcs -> handleDcs(b)
        }
    }

    /** All events emitted since the last call. Consuming them clears the queue. */
    fun drainEvents(): List<ByteStreamEvent> {
        if (pendingEvents.isEmpty()) return emptyList()
        val out = ArrayList<ByteStreamEvent>(pendingEvents.size)
        while (pendingEvents.isNotEmpty()) out.add(pendingEvents.removeFirst())
        return out
    }

    /** Reset all internal state — call when switching sessions. */
    fun reset() {
        lineBuffer.clear()
        escapeBuffer.clear()
        csiParamBytes.clear()
        tuiActive = false
        state = State.Normal
        pendingEvents.clear()
    }

    private fun handleNormal(b: Int) {
        when {
            b == 0x1B -> {
                state = State.Escape
                escapeBuffer.setLength(0)
            }
            b == 0x0A -> flushLineBuffer()
            b == 0x0D -> {
                // Carriage return — drop, the next \n will flush the line.
            }
            b == 0x07 -> {
                // BEL — drop, used by shells to ring the bell.
            }
            else -> lineBuffer.append(b.toChar())
        }
    }

    private fun handleEscape(b: Int) {
        when (b) {
            '['.code -> {
                state = State.Csi
                csiParamBytes.setLength(0)
            }
            ']'.code -> {
                state = State.Osc
                escapeBuffer.setLength(0)
            }
            'P'.code -> {
                state = State.Dcs
                escapeBuffer.setLength(0)
            }
            // Two-byte escapes (ESC <char>): just consume.
            else -> state = State.Normal
        }
    }

    private fun handleCsi(b: Int) {
        when {
            b in 0x30..0x3F -> {
                // Parameter bytes 0..9 : ; < = > ?
                csiParamBytes.append(b.toChar())
            }
            b in 0x20..0x2F -> {
                // Intermediate bytes — swallow.
            }
            b in 0x40..0x7E -> {
                // Final byte — sequence ends here.
                val params = csiParamBytes.toString()
                checkTuiTransition(b, params)
                state = State.Normal
            }
            else -> state = State.Normal
        }
    }

    private fun handleOsc(b: Int) {
        when (b) {
            0x07 -> {
                // BEL terminates the OSC string without a ST.
                state = State.Normal
            }
            0x1B -> {
                // ESC — expect ST (ESC \) next; swallow the ESC, go to
                // a tiny substates that just waits for '\\'.
                escapeBuffer.append('')
                // For simplicity: a lone ESC after OSC content resets.
                state = State.Normal
            }
            else -> escapeBuffer.append(b.toChar())
        }
    }

    private fun handleDcs(b: Int) {
        when (b) {
            0x1B -> state = State.Normal
            else -> escapeBuffer.append(b.toChar())
        }
    }

    /**
     * CSI ?1049 h/l alternates between the normal and the alternate
     * screen buffer. We use it as a TUI on/off signal.
     */
    private fun checkTuiTransition(finalByte: Int, params: String) {
        // We only care about sequences of the form CSI ? <num> h or CSI ? <num> l.
        if (finalByte != 'h'.code && finalByte != 'l'.code) return
        if (!params.startsWith("?")) return
        val body = params.substring(1)
        val codes = body.split(';')
        for (code in codes) {
            if (code == "1049") {
                val newState = finalByte == 'h'.code
                if (newState && !tuiActive) {
                    tuiActive = true
                    flushLineBuffer()
                    pendingEvents.addLast(ByteStreamEvent.TuiEntered)
                } else if (!newState && tuiActive) {
                    tuiActive = false
                    pendingEvents.addLast(ByteStreamEvent.TuiExited)
                }
            }
        }
    }

    private fun flushLineBuffer() {
        if (tuiActive) {
            lineBuffer.setLength(0)
            return
        }
        if (lineBuffer.isEmpty()) return
        val line = lineBuffer.toString()
        lineBuffer.setLength(0)
        val promptMatch = PROMPT_SUFFIX_REGEX.find(line)
        if (promptMatch != null) {
            val promptText = line.substring(0, promptMatch.range.first)
            pendingEvents.addLast(ByteStreamEvent.OutputLine(line))
            pendingEvents.addLast(ByteStreamEvent.PromptReady(promptText))
        } else {
            pendingEvents.addLast(ByteStreamEvent.OutputLine(line))
        }
    }

    private enum class State { Normal, Escape, Csi, Osc, Dcs }

    private companion object {
        // Common shell prompt terminators: `$`, `#`, `❯`, `➜`.
        val PROMPT_SUFFIX_REGEX = Regex("""[#$❯➜]\s*$""")
    }
}
