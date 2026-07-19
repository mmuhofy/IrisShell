package com.iris.irisshell.terminal

// Ported from: mmuhofy/IrisCode — app/src/main/kotlin/com/iris/iriscode/terminal/TerminalManager.kt
// Adapted for Iris Shell — com.iris.irisshell

import android.app.Application
import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.iris.irisshell.domain.agent.ToolResult
import com.termux.terminal.TerminalSession
import com.termux.view.TerminalView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class TerminalManager(
    private val ubuntuBootstrap: UbuntuBootstrap,
    application: Application
) {
    private val _sessions = mutableStateListOf<TerminalSession>()
    private val _tabNames = mutableStateListOf<String>()
    private val _activeTabIndex = mutableStateOf(0)
    var activeTabIndex: Int
        get() = _activeTabIndex.value
        private set(value) { _activeTabIndex.value = value }

    val tabCount: Int get() = _sessions.size
    val tabNames: List<String> get() = _tabNames

    val currentSession: TerminalSession?
        get() = _sessions.getOrNull(activeTabIndex)

    val sessionClient: TerminalSessionClientImpl = TerminalSessionClientImpl()

    private var terminalViewRef: TerminalView? = null

    private val prootRunner: ProotRunner by lazy {
        ProotRunner(ubuntuBootstrap, application.applicationInfo.nativeLibraryDir)
    }

    var projectPath: String? = null

    var shellPath: String = "/bin/zsh"

    init {
        sessionClient.onSessionFinished = { session -> onSessionFinished(session) }
    }

    fun registerTerminalView(view: TerminalView, context: Context) {
        terminalViewRef = view
        sessionClient.onTextChanged = { session ->
            view.onScreenUpdated()
        }
        sessionClient.clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        sessionClient.terminalView = view
    }

    fun unregisterTerminalView() {
        sessionClient.onTextChanged = null
        terminalViewRef = null
    }

    fun addTab(): TerminalSession {
        val session = createNewSession()
        _sessions.add(session)
        _tabNames.add("")
        activeTabIndex = _sessions.size - 1
        terminalViewRef?.attachSession(session)
        return session
    }

    fun renameTab(index: Int, name: String) {
        if (index in _tabNames.indices) {
            _tabNames[index] = name
        }
    }

    fun moveTab(from: Int, to: Int) {
        if (from == to) return
        if (from !in _sessions.indices || to !in _sessions.indices) return
        val session = _sessions.removeAt(from)
        val name = _tabNames.removeAt(from)
        _sessions.add(to, session)
        _tabNames.add(to, name)
        if (activeTabIndex == from) {
            activeTabIndex = to
        } else {
            val moved = if (from < to) -1 else 1
            if (activeTabIndex in (minOf(from, to) + 1) until maxOf(from, to) + 1) {
                activeTabIndex += moved
            }
        }
    }

    fun closeTab(index: Int) {
        if (_sessions.size <= 1) return
        _sessions[index].finishIfRunning()
        _sessions.removeAt(index)
        _tabNames.removeAt(index)
        when {
            index < activeTabIndex -> activeTabIndex--
            index == activeTabIndex && activeTabIndex >= _sessions.size ->
                activeTabIndex = (_sessions.size - 1).coerceAtLeast(0)
        }
        currentSession?.let { terminalViewRef?.attachSession(it) }
    }

    fun switchTab(index: Int) {
        if (index < 0 || index >= _sessions.size || index == activeTabIndex) return
        activeTabIndex = index
        currentSession?.let { terminalViewRef?.attachSession(it) }
    }

    fun createSession(): TerminalSession {
        if (_sessions.isEmpty()) {
            return addTab()
        }
        return _sessions[activeTabIndex]
    }

    private fun createNewSession(): TerminalSession {
        if (ubuntuBootstrap.isInstalled) {
            ensureShellRc()

            val guestWd = if (projectPath != null) {
                "/sdcard/IrisCode/${File(projectPath!!).name}"
            } else null

            val cmd = prootRunner.build(guestWd, shell = shellPath)
            return TerminalSession(
                cmd.executable,
                cmd.cwd,
                cmd.argv.toTypedArray(),
                cmd.environment.toTypedArray(),
                3000,
                sessionClient
            )
        }

        return TerminalSession(
            "/system/bin/sh",
            "/",
            arrayOf("sh"),
            arrayOf("PATH=/system/bin:/system/xbin", "HOME=/", "TERM=vt100"),
            3000,
            sessionClient
        )
    }

    private fun ensureShellRc() {
        val d = "${'$'}"
        val zshrc = File(ubuntuBootstrap.rootfsDir, "home/.zshrc")
        if (!zshrc.exists()) {
            zshrc.writeText(
                """
                export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
                export HOME=/home
                export TERM=xterm-256color
                export LANG=C.UTF-8
                export TMPDIR=/tmp

                HISTSIZE=5000
                HISTFILESIZE=10000

                alias ll='ls -la'
                alias la='ls -A'
                alias l='ls -CF'
                alias ..='cd ..'
                alias grep='grep --color=auto'

                PROMPT='%n@iris:$ '

                if [[ -z "${d}IRIS_WELCOME_SHOWN" ]]; then
                    export IRIS_WELCOME_SHOWN=1
                    echo ""
                    echo "  ╔══════════════════════════════════════════╗"
                    echo "  ║        Welcome to Iris Code v1.0         ║"
                    echo "  ║     Your AI-powered coding terminal      ║"
                    echo "  ╚══════════════════════════════════════════╝"
                    echo ""
                fi
                """.trimIndent() + "\n"
            )
        }
    }

    fun onSessionFinished(finishedSession: TerminalSession) {
        val idx = _sessions.indexOf(finishedSession)
        if (idx >= 0) {
            _sessions.removeAt(idx)
            _tabNames.removeAt(idx)
            when {
                idx < activeTabIndex -> activeTabIndex--
                idx == activeTabIndex && activeTabIndex >= _sessions.size ->
                    activeTabIndex = (_sessions.size - 1).coerceAtLeast(0)
            }
            terminalViewRef?.let { view ->
                currentSession?.let { view.attachSession(it) }
            }
        }
    }

    fun destroy() {
        _sessions.forEach { it.finishIfRunning() }
        _sessions.clear()
    }

    suspend fun executeCommand(
        command: String,
        timeoutSec: Long = 30L,
        onOutput: (String) -> Unit = {}
    ): ToolResult = withContext(Dispatchers.IO) {
        if (!ubuntuBootstrap.isInstalled) {
            return@withContext ToolResult.Error("Ubuntu is not installed")
        }

        val guestWd = if (projectPath != null) {
            "/sdcard/com.iris.irisshell/${File(projectPath!!).name}"
        } else null

        val cmd = prootRunner.buildBashCommand(command, guestWd, shellPath)

        try {
            val process = ProcessBuilder(cmd.argv)
                .directory(File(cmd.cwd))
                .apply {
                    environment().clear()
                    cmd.environment.forEach { entry ->
                        val eqIdx = entry.indexOf('=')
                        if (eqIdx > 0) {
                            environment()[entry.substring(0, eqIdx)] = entry.substring(eqIdx + 1)
                        }
                    }
                }
                .redirectErrorStream(true)
                .start()

            val output = StringBuilder()
            process.inputStream.bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val l = line!!
                    output.appendLine(l)
                    onOutput(l)
                }
            }

            val finished = process.waitFor(timeoutSec, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                return@withContext ToolResult.Error(
                    "Command timed out after ${timeoutSec}s: $command"
                )
            }

            val exitCode = process.exitValue()
            val text = output.toString().trim()

            return@withContext if (exitCode == 0) {
                ToolResult.Success(
                    if (text.isNotEmpty()) text else "(no output)"
                )
            } else {
                ToolResult.Error(
                    if (text.isNotEmpty()) text else "(no output)"
                )
            }
        } catch (e: Exception) {
            ToolResult.Error("Command execution failed: ${e.message}")
        }
    }
}
