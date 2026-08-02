package com.iris.irisshell.terminal

import android.app.Application
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.iris.irisshell.domain.agent.ToolResult
import com.termux.terminal.TerminalSession
import com.termux.view.TerminalView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class TerminalManager(
    private val ubuntuBootstrap: UbuntuBootstrap,
    application: Application,
    private val blockEngineWire: BlockEngineWire? = null,
) {
    private val _sessions: MutableList<TerminalSession> = mutableListOf()
    private val _tabNames: MutableList<String> = mutableListOf()
    val tabNames: List<String> get() = _tabNames

    /**
     * Reverse map: persistent session id (UUID, stored in Room) →
     * positional index into [_sessions]. Phase 2 Session System uses
     * ids so that the UI can refer to a session stably across app
     * restarts. Inspired by ReTerminal's SessionService id-keyed
     * HashMap (github.com/RohitKushvaha01/ReTerminal, file
     * core/main/src/main/java/com/rk/terminal/service/SessionService.kt).
     */
    private val _idToIndex: MutableMap<String, Int> = mutableMapOf()
    private val _indexToId: MutableMap<Int, String> = mutableMapOf()

    private val _activeTabIndex = MutableStateFlow(0)
    val activeTabIndex: StateFlow<Int> = _activeTabIndex.asStateFlow()

    /**
     * Synchronous snapshot of the active tab index, intended for UI scaffolds
     * (e.g. the topbar's "1 / N" indicator) that do not need a Flow<T>.
     */
    fun getActiveTabIndexSnapshot(): Int = _activeTabIndex.value

    val tabCount: Int get() = _sessions.size

    val currentSession: TerminalSession?
        get() = _sessions.getOrNull(_activeTabIndex.value)

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
            blockEngineWire?.onSessionTextChanged(session)
        }
        sessionClient.clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        sessionClient.terminalView = view
    }

    fun unregisterTerminalView() {
        sessionClient.onTextChanged = null
        terminalViewRef = null
    }

    fun addTab(): TerminalSession = addTabWithId(null, "")

    /**
     * Id-aware spawn. When [persistentId] is non-null, the new session
     * is recorded in [_idToIndex] so that the Session System can
     * refer to it across app restarts. Returns the spawned
     * [TerminalSession] just like [addTab] does.
     */
    fun addTabWithId(persistentId: String?, name: String): TerminalSession {
        val session = createNewSession()
        _sessions.add(session)
        _tabNames.add(name)
        val newIndex = _sessions.size - 1
        if (persistentId != null) {
            _idToIndex[persistentId] = newIndex
            _indexToId[newIndex] = persistentId
        }
        _activeTabIndex.value = newIndex
        terminalViewRef?.attachSession(session)
        return session
    }

    /**
     * Look up the positional tab index for a persistent session id, or
     * `-1` if the id is unknown / the session was closed.
     */
    fun getIndexForId(persistentId: String): Int =
        _idToIndex[persistentId] ?: -1

    /** Reverse lookup: positional index → persistent id. */
    fun getIdForIndex(index: Int): String? = _indexToId[index]

    /**
     * Switch to the session identified by [persistentId]. No-op when
     * the id is unknown. Used by [SessionManagerAdapter] when the UI
     * asks to change the active session.
     */
    fun switchSessionById(persistentId: String) {
        val idx = getIndexForId(persistentId)
        if (idx >= 0) switchTab(idx)
    }

    /** Currently-active session's persistent id, or null if unknown. */
    fun activePersistentId(): String? = _indexToId[_activeTabIndex.value]

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
        if (_activeTabIndex.value == from) {
            _activeTabIndex.value = to
        } else {
            val moved = if (from < to) -1 else 1
            if (_activeTabIndex.value in (minOf(from, to) + 1) until maxOf(from, to) + 1) {
                _activeTabIndex.value += moved
            }
        }
    }

    fun closeTab(index: Int) {
        if (_sessions.size <= 1) return
        _sessions[index].finishIfRunning()
        _sessions.removeAt(index)
        _tabNames.removeAt(index)
        // Strip the id mapping for the closed index and rebase later
        // indices down by one.
        val closedId = _indexToId.remove(index)
        if (closedId != null) _idToIndex.remove(closedId)
        val rebased = _indexToId.toMap()
        _indexToId.clear()
        _idToIndex.clear()
        rebased.forEach { (oldIdx, id) ->
            val newIdx = if (oldIdx > index) oldIdx - 1 else oldIdx
            _indexToId[newIdx] = id
            _idToIndex[id] = newIdx
        }
        when {
            index < _activeTabIndex.value -> _activeTabIndex.value--
            index == _activeTabIndex.value && _activeTabIndex.value >= _sessions.size ->
                _activeTabIndex.value = (_sessions.size - 1).coerceAtLeast(0)
        }
        currentSession?.let { terminalViewRef?.attachSession(it) }
    }

    fun switchTab(index: Int) {
        if (index < 0 || index >= _sessions.size || index == _activeTabIndex.value) return
        _activeTabIndex.value = index
        currentSession?.let { terminalViewRef?.attachSession(it) }
    }

    fun createSession(): TerminalSession {
        if (_sessions.isEmpty()) {
            return addTab()
        }
        return _sessions[_activeTabIndex.value]
    }

    private fun createNewSession(): TerminalSession {
        if (ubuntuBootstrap.isInstalled) {
            ensureShellRc()

            val guestWd = if (projectPath != null) {
                "/sdcard/com.iris.irisshell/${File(projectPath!!).name}"
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

                PROMPT='%F{yellow}%n@iris-shell%f:%F{blue}%~%f$ '

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
                idx < _activeTabIndex.value -> _activeTabIndex.value--
                idx == _activeTabIndex.value && _activeTabIndex.value >= _sessions.size ->
                    _activeTabIndex.value = (_sessions.size - 1).coerceAtLeast(0)
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
