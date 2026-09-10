package org.soyuz.kcraft.computer

import org.soyuz.kcraft.computer.ksh.KShEnvironment

class ComputerRuntime(
    val terminal: Terminal,
    val fileSystem: FileSystem
) {
    var workingDirectory: String = "/"
        private set

    var uptimeTicks = 0L
        private set

    val environment = KShEnvironment(this)

    fun tick() {
        uptimeTicks++
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

    fun openNano(path: String) {
        // later: switch runtime/screen mode
    }
}