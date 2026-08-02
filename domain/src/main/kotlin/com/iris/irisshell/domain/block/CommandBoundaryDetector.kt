package com.iris.irisshell.domain.block

/**
 * Command-boundary detector for the block engine.
 *
 * Layered detection strategy (per `docs/block-engine/PLAN.md` §7):
 *   1. **OSC 133**  — primary, requires the shell prompt to emit
 *      `ESC ] 133 ; A/B/C ST` markers (Starship, Powerlevel10k, or any
 *      prompt using `set_prompt`). Not wired in v1 because Iris Shell's
 *      default zsh prompt does not emit OSC 133 yet — added in v2.
 *   2. **ANSI cursor tracking** — secondary, fires when the cursor
 *      jumps to the first column of a fresh line. Useful but noisy
 *      with line-wrapped output. Not wired in v1.
 *   3. **Prompt regex** — fallback, always available. Scans the last
 *      line(s) of the terminal buffer for known prompt shapes:
 *      `user@host:path$`, `> `, `# `, `% `, `❯ `.
 *
 * UNTESTED — verify before use.
 */
class CommandBoundaryDetector {

    /**
     * Scan the provided lines (top-to-bottom order) for a prompt
     * marker. Returns the most recent match — the one that closes the
     * currently-running block.
     *
     * `lines` is expected to be the last N rows of the visible terminal
     * buffer, in display order. ANSI sequences should be stripped before
     * calling this.
     */
    fun detectPromptReady(lines: List<String>): CommandBoundary {
        if (lines.isEmpty()) return CommandBoundary.None

        for (i in lines.indices.reversed()) {
            val line = lines[i].trimEnd()
            if (matchesPrompt(line)) {
                return CommandBoundary.PromptReady(atLine = i)
            }
        }
        return CommandBoundary.None
    }

    private fun matchesPrompt(line: String): Boolean {
        if (line.isEmpty()) return false
        val tail = line.last()
        if (tail !in PROMPT_TERMINATORS) return false

        val stripped = line.trim()
        if (stripped.isEmpty()) return false

        // Family 1: `user@host:path$` — at least one `@`, colon present.
        if ('@' in stripped && ':' in stripped) {
            val atIdx = stripped.indexOf('@')
            val colonIdx = stripped.indexOf(':', atIdx)
            if (colonIdx > atIdx) return true
        }

        // Family 2: minimal prompt ending in marker (`$ `, `> `, `❯ `).
        // Match short lines that end with a single marker char.
        if (stripped.length <= 3) return true

        // Family 3: spaceship / oh-my-zsh style — ends with `❯ ` or `➜ `.
        if (stripped.endsWith("❯") || stripped.endsWith("➜")) return true

        return false
    }

    private companion object {
        val PROMPT_TERMINATORS = setOf('$', '#', '%', '❯', '➜', '>')
    }
}
