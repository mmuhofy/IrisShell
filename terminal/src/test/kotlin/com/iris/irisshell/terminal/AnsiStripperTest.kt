package com.iris.irisshell.terminal

import org.junit.Assert.assertEquals
import org.junit.Test

class AnsiStripperTest {

    @Test fun `strips CSI SGR color sequence`() {
        val input = "\u001b[33mhello\u001b[0m"
        assertEquals("hello", AnsiStripper.strip(input))
    }

    @Test fun `strips OSC title sequence terminated by BEL`() {
        val input = "\u001b]0;mytitle\u0007rest"
        assertEquals("rest", AnsiStripper.strip(input))
    }

    @Test fun `strips simple two-byte escape sequence`() {
        val input = "\u001b=hello"
        assertEquals("hello", AnsiStripper.strip(input))
    }

    @Test fun `preserves plain text`() {
        val input = "muhofy@iris:~/IrisShell$ "
        assertEquals("muhofy@iris:~/IrisShell$ ", AnsiStripper.strip(input))
    }

    @Test fun `strips zsh prompt color codes leaving prompt text`() {
        val input = "\u001b[33mmuhofy@iris-shell\u001b[0m:\u001b[34m~/IrisShell\u001b[0m$ "
        assertEquals("muhofy@iris-shell:~/IrisShell$ ", AnsiStripper.strip(input))
    }
}
