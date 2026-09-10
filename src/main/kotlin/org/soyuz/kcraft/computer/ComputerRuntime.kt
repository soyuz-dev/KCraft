package org.soyuz.kcraft.computer

import org.soyuz.kcraft.computer.ksh.KShEnvironment
import org.soyuz.kcraft.computer.ksh.KShInterpreter

class ComputerRuntime(
    val terminal: Terminal,
    val fileSystem: FileSystem
) {
    var workingDirectory: String = "/"
        private set

    var uptimeTicks = 0L
        private set

    private var booted = false

    val environment = KShEnvironment(this)

    val interpreter = KShInterpreter(this)

    fun tick() {
        if(!booted) return
        uptimeTicks++
    }

    fun boot() {
        if (booted) return

        booted = true

        if (fileSystem.exists("/etc/shell.kshrc")) {
            interpreter.executeLine("source /etc/shell.kshrc")
        }
    }


    fun changeDirectory(path: String): Boolean {
        val normalized = fileSystem.normalizePath(
            workingDirectory,
            path
        )

        if (!fileSystem.exists(normalized)) {
            return false
        }

        if (!fileSystem.isDirectory(normalized)) {
            return false
        }

        workingDirectory = normalized
        return true
    }

    fun submitCurrentCommand() {
        val command = terminal.commitInput().trim()

        if (command.isNotEmpty()) {
            interpreter.executeLine(command)
        }
    }

    fun openNano(path: String) {
        // later: switch runtime/screen mode or some other abstraction
    }
}