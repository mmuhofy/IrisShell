package com.iris.irisshell.terminal

// Ported from: mmuhofy/IrisCode — app/src/main/kotlin/com/iris/iriscode/terminal/UbuntuBootstrap.kt
// Adapted for Iris Shell — com.iris.irisshell

import android.content.Context
import android.system.Os
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream

// Inspired by: github.com/Xed-Editor/Karbon-PackagesX (proot binary for Android NDK)
// and github.com/termux/termux-app (proot-loader)
class UbuntuBootstrap(private val context: Context) {

    private val baseDir: File get() = File(context.filesDir, "ubuntu")
    val prootFile: File get() = File(baseDir, "bin/proot")
    val libDir: File get() = File(baseDir, "lib")
    val rootfsDir: File get() = File(baseDir, "rootfs")
    private val tmpDir: File get() = File(baseDir, "tmp")

    val isInstalled: Boolean
        get() = prootFile.canExecute()
            && File(libDir, "libtalloc.so.2").canRead()
            && File(rootfsDir, "bin/zsh").canExecute()
            && File(rootfsDir, "etc/apt/sources.list").exists()

    suspend fun install(
        installPackages: Boolean = true,
        optimize: Boolean = true,
        onState: (UbuntuSetupState) -> Unit
    ) {
        if (isInstalled) {
            onState(UbuntuSetupState.Ready)
            return
        }
        runInstall(installPackages, optimize, onState)
    }

    private suspend fun runInstall(
        installPackages: Boolean,
        optimize: Boolean,
        onState: (UbuntuSetupState) -> Unit
    ) {
        try {
            withContext(Dispatchers.IO) {
                baseDir.mkdirs()
                File(baseDir, "bin").mkdirs()
                libDir.mkdirs()
                tmpDir.mkdirs()

                onState(UbuntuSetupState.Extracting)

                // proot binary
                context.assets.open("proot").use { input ->
                    prootFile.parentFile!!.mkdirs()
                    FileOutputStream(prootFile).use { out ->
                        input.copyTo(out)
                    }
                }
                prootFile.setExecutable(true, false)

                // libtalloc.so.2
                context.assets.open("libtalloc.so.2").use { input ->
                    libDir.mkdirs()
                    FileOutputStream(File(libDir, "libtalloc.so.2")).use { out ->
                        input.copyTo(out)
                    }
                }

                // Ubuntu rootfs (try multiple asset names, fallback to download)
                rootfsDir.mkdirs()
                val rootfsStream = try {
                    context.assets.open("ubuntu_rootfs")
                } catch (_: Exception) {
                    try {
                        context.assets.open("ubuntu-base.tar.gz")
                    } catch (_: Exception) {
                        Log.w("UbuntuBootstrap", "Rootfs not in assets, downloading...")
                        downloadRootfs()
                    }
                }
                rootfsStream.use { input ->
                    extractTarGz(GZIPInputStream(input), rootfsDir)
                }

                onState(UbuntuSetupState.Configuring)
                configureRootfs()

                if (installPackages) {
                    installBasePackages(onState)
                }

                installOhMyZsh(onState)

                if (optimize) {
                    onState(UbuntuSetupState.Optimizing)
                    optimizeRootfs()
                }

                onState(UbuntuSetupState.Ready)
            }
        } catch (e: Exception) {
            Log.e("UbuntuBootstrap", "Setup failed", e)
            onState(UbuntuSetupState.Failed("${e::class.simpleName}: ${e.message ?: "Unknown error"}"))
        }
    }

    private fun configureRootfs() {
        File(rootfsDir, "etc/resolv.conf").writeText(
            "nameserver 8.8.8.8\nnameserver 8.8.4.4\n"
        )
        File(rootfsDir, "etc/hostname").writeText("iriscode-ubuntu\n")
        File(rootfsDir, "etc/hosts").writeText(
            "127.0.0.1 localhost iriscode-ubuntu\n::1 localhost ip6-localhost ip6-loopback\n"
        )
        File(rootfsDir, "etc/apt/sources.list.d/ubuntu.sources").delete()
        File(rootfsDir, "etc/apt/sources.list").writeText(
            """
            deb http://ports.ubuntu.com/ubuntu-ports noble main restricted universe multiverse
            deb http://ports.ubuntu.com/ubuntu-ports noble-updates main restricted universe multiverse
            deb http://ports.ubuntu.com/ubuntu-ports noble-security main restricted universe multiverse
            """.trimIndent() + "\n"
        )
        File(rootfsDir, "home").mkdirs()
        File(rootfsDir, "root").mkdirs()
        File(rootfsDir, "tmp").mkdirs()

        // Minimal .bashrc for script compatibility
        File(rootfsDir, "home/.bashrc").writeText(
            """
            export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
            export HOME=/home
            export TERM=xterm-256color
            export LANG=C.UTF-8
            export TMPDIR=/tmp
            alias ll='ls -la'
            alias la='ls -A'
            alias l='ls -CF'
            """.trimIndent() + "\n"
        )

        // Minimal .bash_profile
        File(rootfsDir, "home/.bash_profile").writeText(
            """
            if [ -f ~/.bashrc ]; then
                . ~/.bashrc
            fi
            """.trimIndent() + "\n"
        )

        // Basic .zshrc template (will be overwritten by OMZ setup)
        writeBasicZshrc()
    }

    private fun writeBasicZshrc() {
        File(rootfsDir, "home/.zshrc").writeText(
            """
            export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
            export HOME=/home
            export TERM=xterm-256color
            export LANG=C.UTF-8
            export TMPDIR=/tmp

            # History
            HISTSIZE=5000
            HISTFILESIZE=10000
            HISTTIMEFORMAT="%F %T "
            setopt SHARE_HISTORY HIST_IGNORE_DUPS HIST_IGNORE_SPACE

            # Aliases
            alias ll='ls -la'
            alias la='ls -A'
            alias l='ls -CF'
            alias ..='cd ..'
            alias ...='cd ../..'
            alias grep='grep --color=auto'
            alias df='df -h'
            alias du='du -h'

            # Prompt
            PROMPT='%F{yellow}%n@iris%f:%F{yellow}%~%f$ '
            RPROMPT='%F{yellow}%(?..✗ %?)%f'
            """.trimIndent() + "\n"
        )
    }

    // ─── Package installation ───────────────────────────────────────

    private fun installBasePackages(onState: (UbuntuSetupState) -> Unit) {
        onState(UbuntuSetupState.InstallingPackages("apt", "Updating package lists..."))
        val updateExit = runInProot("apt-get update -qq 2>&1")
        if (updateExit != 0) {
            throw RuntimeException("apt-get update failed (exit $updateExit)")
        }

        val packages = listOf("zsh", "git", "curl", "ca-certificates", "nano", "vim", "tree")
        onState(UbuntuSetupState.InstallingPackages("all", "Installing: ${packages.joinToString(", ")}..."))
        val installExit = runInProot(
            "DEBIAN_FRONTEND=noninteractive apt-get install -y ${packages.joinToString(" ")} 2>&1"
        )
        if (installExit != 0) {
            throw RuntimeException("Package installation failed (exit $installExit)")
        }
    }

    // ─── Oh My Zsh setup ────────────────────────────────────────────

    private fun installOhMyZsh(onState: (UbuntuSetupState) -> Unit) {
        onState(UbuntuSetupState.InstallingOhMyZsh("Downloading Oh My Zsh..."))
        val omzDir = "/home/.oh-my-zsh"
        val omzExit = runInProot(
            "git clone --depth=1 https://github.com/ohmyzsh/ohmyzsh.git $omzDir 2>&1"
        )

        if (omzExit != 0) {
            Log.w("UbuntuBootstrap", "Oh My Zsh install failed (exit $omzExit), using basic zsh config")
            writeBasicZshrc()
            return
        }

        onState(UbuntuSetupState.InstallingOhMyZsh("Installing plugins..."))

        // https://github.com/zsh-users/zsh-autosuggestions
        runInProot(
            "git clone --depth=1 https://github.com/zsh-users/zsh-autosuggestions $omzDir/custom/plugins/zsh-autosuggestions 2>&1"
        )

        // https://github.com/zsh-users/zsh-syntax-highlighting
        runInProot(
            "git clone --depth=1 https://github.com/zsh-users/zsh-syntax-highlighting.git $omzDir/custom/plugins/zsh-syntax-highlighting 2>&1"
        )

        onState(UbuntuSetupState.InstallingOhMyZsh("Creating .zshrc..."))
        createZshConfig()
    }

    private fun createZshConfig() {
        val d = "${'$'}"
        File(rootfsDir, "home/.zshrc").writeText(
            """
            export PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin
            export HOME=/home
            export TERM=xterm-256color
            export LANG=C.UTF-8
            export TMPDIR=/tmp

            # ─── Oh My Zsh ──────────────────────────────────────────
            export ZSH="${d}HOME/.oh-my-zsh"
            ZSH_THEME="agnoster"

            # ─── Plugins ────────────────────────────────────────────
            plugins=(
                git
                zsh-autosuggestions
                zsh-syntax-highlighting
                history
                aliases
            )

            source ${d}ZSH/oh-my-zsh.sh

            # ─── History ────────────────────────────────────────────
            HISTSIZE=5000
            HISTFILESIZE=10000
            HISTTIMEFORMAT="%F %T "
            setopt SHARE_HISTORY HIST_IGNORE_DUPS HIST_IGNORE_SPACE

            # ─── Aliases ────────────────────────────────────────────
            alias ll='ls -la'
            alias la='ls -A'
            alias l='ls -CF'
            alias ..='cd ..'
            alias ...='cd ../..'
            alias grep='grep --color=auto'
            alias df='df -h'
            alias du='du -h'

            # ─── Welcome ────────────────────────────────────────────
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

    // ─── Rootfs optimization ───────────────────────────────────────

    private fun optimizeRootfs() {
        runInProot(
            "apt-get clean -qq 2>&1 && rm -rf /var/lib/apt/lists/* && rm -rf /var/cache/apt/archives/*.deb && rm -rf /tmp/*"
        )
    }

    // ─── Proot command execution (for setup steps) ─────────────────

    private val linkerPath: String
        get() = if (File("/system/bin/linker64").exists()) "/system/bin/linker64" else "/system/bin/linker"

    private fun runInProot(command: String): Int {
        val prootExe = prootFile.absolutePath
        val rootfs = rootfsDir.absolutePath
        val lib = libDir.absolutePath
        val tmp = tmpDir.absolutePath

        val argv = listOf(
            linkerPath, prootExe,
            "--kill-on-exit", "-0", "--link2symlink",
            "-r", rootfs,
            "-w", "/home",
            "-b", "/dev", "-b", "/proc", "-b", "/sys",
            "-b", "/system", "-b", "/data",
            "-b", "${lib}:/hostlib",
            "/bin/bash", "-c", command
        )

        val env = mapOf(
            "PATH" to "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
            "HOME" to "/home",
            "TERM" to "xterm-256color",
            "LANG" to "C.UTF-8",
            "TMPDIR" to "/tmp",
            "LD_LIBRARY_PATH" to lib,
            "PROOT_TMP_DIR" to tmp
        )

        val pb = ProcessBuilder(*argv.toTypedArray())
        pb.environment().clear()
        pb.environment().putAll(env)
        pb.directory(File(rootfs))
        pb.redirectErrorStream(true)

        val process = pb.start()
        // Consume output to prevent buffer deadlock
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        while (reader.readLine() != null) { }
        return process.waitFor()
    }

    // ─── Download ──────────────────────────────────────────────────

    private fun downloadRootfs(): InputStream {
        val arch = android.os.Build.SUPPORTED_ABIS[0]
        val rootfsArch = ROOTFS_ARCH_MAP[arch] ?: "arm64"
        val url = "https://cdimage.ubuntu.com/ubuntu-base/releases/$UBUNTU_VERSION/release/ubuntu-base-$UBUNTU_VERSION-base-$rootfsArch.tar.gz"
        val request = okhttp3.Request.Builder().url(url).addHeader("User-Agent", "IrisCode/1.0").build()
        val response = okhttp3.OkHttpClient.Builder().followRedirects(true).build().newCall(request).execute()
        if (!response.isSuccessful) throw RuntimeException("Download failed: ${response.code} for $url")
        return response.body!!.byteStream()
    }

    fun retry() {
        baseDir.deleteRecursively()
    }

    companion object {
        private const val UBUNTU_VERSION = "24.04.4"
        private val ROOTFS_ARCH_MAP = mapOf(
            "arm64-v8a" to "arm64",
            "armeabi-v7a" to "armhf",
            "x86_64" to "amd64",
            "x86" to "i386"
        )
    }

    // ─── tar.gz extraction (no system binary dependency) ─────────────────────

    private fun extractTarGz(gz: GZIPInputStream, destDir: File) {
        var pendingLongName: String? = null
        var pendingLongLink: String? = null

        val headerBuf = ByteArray(512)
        val dataBuf = ByteArray(32768)

        while (true) {
            if (readFully(gz, headerBuf) < 0) return
            if (headerBuf.all { it == 0.toByte() }) return

            val name = parseString(headerBuf, 0, 100)
            val size = parseOctal(headerBuf, 124, 12)
            val type = headerBuf[156].toInt().toChar()
            val linkName = parseString(headerBuf, 157, 100)

            if (type == 'L') {
                pendingLongName = readStringData(gz, size, dataBuf)
                skipPadding(gz, size)
                continue
            }
            if (type == 'K') {
                pendingLongLink = readStringData(gz, size, dataBuf)
                skipPadding(gz, size)
                continue
            }

            val finalName = pendingLongName ?: name
            pendingLongName = null
            val finalLink = pendingLongLink ?: linkName
            pendingLongLink = null

            if (type == '5') {
                File(destDir, finalName).mkdirs()
                skipPadding(gz, size)
            } else if (type == '2') {
                val entry = File(destDir, finalName)
                entry.parentFile?.mkdirs()
                entry.delete()
                Os.symlink(finalLink, entry.absolutePath)
                skipPadding(gz, size)
            } else if (type == 'x' || type == 'g') {
                skipData(gz, size)
                skipPadding(gz, size)
            } else if (finalName.isEmpty() || finalName == "." || finalName == ".." || finalName.endsWith("/")) {
                skipPadding(gz, size)
            } else {
                val entry = File(destDir, finalName)
                entry.parentFile?.mkdirs()
                var remaining = size
                FileOutputStream(entry).use { out ->
                    while (remaining > 0) {
                        val toRead = minOf(dataBuf.size.toLong(), remaining).toInt()
                        val read = readFully(gz, dataBuf, toRead)
                        if (read < 0) throw RuntimeException("Unexpected EOF in $finalName")
                        out.write(dataBuf, 0, read)
                        remaining -= read
                    }
                }
                skipPadding(gz, size)

                val mode = parseOctal(headerBuf, 100, 8).toInt()
                if (mode and 64 != 0) entry.setExecutable(true, false)
            }
        }
    }

    private fun readFully(input: java.io.InputStream, buf: ByteArray, length: Int = buf.size): Int {
        var offset = 0
        while (offset < length) {
            val read = input.read(buf, offset, length - offset)
            if (read < 0) return if (offset == 0) -1 else offset
            offset += read
        }
        return offset
    }

    private fun skipData(input: java.io.InputStream, size: Long) {
        var remaining = size
        val buf = ByteArray(4096)
        while (remaining > 0) {
            val toRead = minOf(buf.size.toLong(), remaining).toInt()
            readFully(input, buf, toRead)
            remaining -= toRead
        }
    }

    private fun skipPadding(input: java.io.InputStream, dataSize: Long) {
        val padding = (512 - (dataSize % 512)) % 512
        var skipped = 0L
        while (skipped < padding) {
            val toSkip = minOf(padding - skipped, 4096L)
            val n = input.skip(toSkip)
            if (n <= 0) {
                val buf = ByteArray(minOf(padding - skipped, 4096L).toInt())
                readFully(input, buf, buf.size)
                skipped += buf.size
            } else {
                skipped += n
            }
        }
    }

    private fun readStringData(input: java.io.InputStream, size: Long, buf: ByteArray): String {
        val toRead = minOf(buf.size.toLong(), size).toInt()
        readFully(input, buf, toRead)
        if (size > buf.size) {
            var remaining = size - buf.size
            val discardBuf = ByteArray(4096)
            while (remaining > 0) {
                val chunk = minOf(discardBuf.size.toLong(), remaining).toInt()
                readFully(input, discardBuf, chunk)
                remaining -= chunk
            }
        }
        val end = buf.indexOfFirst { it == 0.toByte() }.let { if (it < 0) toRead else it }
        return buf.copyOfRange(0, end).decodeToString()
    }

    private fun parseOctal(data: ByteArray, offset: Int, length: Int): Long {
        val end = minOf(offset + length, data.size)
        var i = offset
        while (i < end && (data[i] == 0x20.toByte() || data[i] == 0x30.toByte() || data[i] == 0.toByte())) i++
        if (i >= end) return 0
        var j = i
        while (j < end && data[j] != 0x20.toByte() && data[j] != 0.toByte()) j++
        if (i == j) return 0
        val str = data.copyOfRange(i, j).decodeToString()
        return str.toLong(8)
    }

    private fun parseString(data: ByteArray, offset: Int, length: Int): String {
        val end = minOf(offset + length, data.size)
        return data.copyOfRange(offset, end)
            .takeWhile { it != 0.toByte() }
            .toByteArray()
            .decodeToString()
    }
}
